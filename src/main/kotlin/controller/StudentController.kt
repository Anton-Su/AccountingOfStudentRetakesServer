package controller

import dI.AppContainer.userRepository
import data.dto.CreateCommentRequestDto
import data.dto.toDto
import data.dto.toRetakeDto
import domain.repository.UserRepository
import domain.usecases.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import security.currentEmail

class StudentController(
    private val userRepository: UserRepository,
    private val getStudentDebtsUseCase: GetStudentDebtsUseCase,
    private val enrollToRetakeUseCase: EnrollToRetakeUseCase,
    private val cancelRetakeEnrollmentUseCase: CancelRetakeEnrollmentUseCase,
    private val createCommentUseCase: CreateCommentUseCase,
    private val getStudentDebtRankUseCase: GetStudentDebtRankUseCase,
    private val getAvailableRetakesUseCase: GetAvailableRetakesUseCase,
    private val getEnrolledRetakesUseCase: GetEnrolledRetakesUseCase
) {
    fun configure(route: Route) {
        route.get("/{studentId}/debts") {
            val studentId = call.pathStudentId() ?: return@get
            if (!call.requireOwnStudent(studentId)) return@get
            val debts = getStudentDebtsUseCase(studentId)
            call.respond(debts.map { it.toDto() })
        }
        route.post("/{studentId}/debts/{debtId}/retakes/{retakeId}") {
            val studentId = call.pathStudentId() ?: return@post
            if (!call.requireOwnStudent(studentId)) return@post
            val debtId = call.longPathParam("debtId") ?: return@post
            val retakeId = call.longPathParam("retakeId") ?: return@post
            call.respond(enrollToRetakeUseCase(studentId, debtId, retakeId))
        }
        // val rawBody = call.receiveText()
        // Raw request body: {"startAt":"2026-05-06T05:45:00","endAt":"2026-05-28T02:12:00","teacherIds":[3],"subjectId":3,"type":"Зачёт","place":"HI","admission":"Testfffff"}
        // println("Raw request body: $rawBody") // Логируем
        route.delete("/{studentId}/debts/{debtId}/retakes/{retakeId}") {
            val studentId = call.pathStudentId() ?: return@delete
            if (!call.requireOwnStudent(studentId)) return@delete
            val debtId = call.longPathParam("debtId") ?: return@delete
            val retakeId = call.longPathParam("retakeId") ?: return@delete
            call.respond(cancelRetakeEnrollmentUseCase(studentId, debtId, retakeId))
        }
        route.post("/{studentId}/comments") {
            val studentId = call.pathStudentId() ?: return@post
            if (!call.requireOwnStudent(studentId)) return@post
            val request = call.receive<CreateCommentRequestDto>()
            val created = createCommentUseCase(
                studentId = studentId,
                gradeplace = request.gradeplace,
                gradeteacher = request.gradeteacher,
                gradeoverall = request.gradeoverall,
                comment = request.comment,
                retakeId = request.retakeId,
            )
            call.respond(HttpStatusCode.Created, created.toDto())
        }
        route.get("/{studentId}/debts/rank") {
            val studentId = call.pathStudentId() ?: return@get
            if (!call.requireOwnStudent(studentId)) {
                return@get
            }
            val result = getStudentDebtRankUseCase(studentId)
            call.respond(result.toDto())
        }
        route.get("/{studentId}/retakes/available") {
            val studentId = call.pathStudentId() ?: return@get
            if (!call.requireOwnStudent(studentId)) return@get
            val retakes = getAvailableRetakesUseCase(studentId)
            call.respond(retakes.map { it.toRetakeDto() })
        }
        route.get("/{studentId}/retakes/enrolled") {
            val studentId = call.pathStudentId() ?: return@get
            if (!call.requireOwnStudent(studentId)) return@get
            val retakes = getEnrolledRetakesUseCase(studentId)
            call.respond(retakes.map { it.toRetakeDto() })
        }
    }
}


private suspend fun ApplicationCall.pathStudentId(): Long? = longPathParam("studentId")

private suspend fun ApplicationCall.longPathParam(name: String): Long? {
    val raw = parameters[name] ?: return null
    return raw.toLongOrNull() ?: run {
        respond(HttpStatusCode.BadRequest, mapOf("error" to "Path parameter '$name' must be a number"))
        null
    }
}

private suspend fun ApplicationCall.requireOwnStudent(studentId: Long): Boolean {
    val email = currentEmail() ?: run {
        respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
        return false
    }
    val user = userRepository.findByEmail(email) ?: run {
        respond(HttpStatusCode.Unauthorized, mapOf("error" to "User not found"))
        return false
    }
    if (user.id != studentId) {
        respond(HttpStatusCode.Forbidden, mapOf("error" to "You can access only your own debts"))
        return false
    }
    return true
}

