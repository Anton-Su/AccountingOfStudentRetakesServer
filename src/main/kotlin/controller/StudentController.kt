package controller

import data.dto.CreateCommentRequestDto
import data.dto.toDto
import data.dto.toRetakeDto
import domain.usecases.*
import helpers.longPathParam
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class StudentController(
    private val getStudentDebtsUseCase: GetStudentDebtsUseCase,
    private val enrollToRetakeUseCase: EnrollToRetakeUseCase,
    private val cancelRetakeEnrollmentUseCase: CancelRetakeEnrollmentUseCase,
    private val createCommentUseCase: CreateCommentUseCase,
    private val getStudentDebtRankUseCase: GetStudentDebtRankUseCase,
    private val getAvailableRetakesUseCase: GetAvailableRetakesUseCase,
    private val getEnrolledRetakesUseCase: GetEnrolledRetakesUseCase
) {
    fun configure(route: Route) {
        route.route("/{studentId}") {
            get("/debts") {
                val studentId = call.parameters["studentId"]!!.toLong()
                val debts = getStudentDebtsUseCase(studentId)
                call.respond(debts.map { it.toDto() })
            }
            post("/debts/{debtId}/retakes/{retakeId}") {
                val studentId = call.parameters["studentId"]!!.toLong()
                val debtId = call.longPathParam("debtId") ?: return@post
                val retakeId = call.longPathParam("retakeId") ?: return@post
                call.respond(enrollToRetakeUseCase(studentId, debtId, retakeId))
            }
            // val rawBody = call.receiveText()
            // Raw request body: {"startAt":"2026-05-06T05:45:00","endAt":"2026-05-28T02:12:00","teacherIds":[3],"subjectId":3,"type":"Зачёт","place":"HI","admission":"Testfffff"}
            // println("Raw request body: $rawBody") // Логируем
            delete("/debts/{debtId}/retakes/{retakeId}") {
                val studentId = call.parameters["studentId"]!!.toLong()
                val debtId = call.longPathParam("debtId") ?: return@delete
                val retakeId = call.longPathParam("retakeId") ?: return@delete
                call.respond(cancelRetakeEnrollmentUseCase(studentId, debtId, retakeId))
            }
            post("/comments") {
                val studentId = call.parameters["studentId"]!!.toLong()
                val request = call.receive<CreateCommentRequestDto>()
                val created = createCommentUseCase(studentId = studentId, gradeplace = request.gradeplace, gradeteacher = request.gradeteacher, gradeoverall = request.gradeoverall, comment = request.comment, retakeId = request.retakeId,)
                call.respond(HttpStatusCode.Created, created.toDto())
            }
            get("/debts/rank") {
                val studentId = call.parameters["studentId"]!!.toLong()
                val result = getStudentDebtRankUseCase(studentId)
                call.respond(result.toDto())
            }
            get("/retakes/available") {
                val studentId = call.parameters["studentId"]!!.toLong()
                val retakes = getAvailableRetakesUseCase(studentId)
                call.respond(retakes.map { it.toRetakeDto() })
            }
            get("/retakes/enrolled") {
                val studentId = call.parameters["studentId"]!!.toLong()
                val retakes = getEnrolledRetakesUseCase(studentId)
                call.respond(retakes.map { it.toRetakeDto() })
            }
        }
    }
}
