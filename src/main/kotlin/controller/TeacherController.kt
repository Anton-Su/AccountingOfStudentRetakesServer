package controller

import data.dto.GradeRequestDto
import data.dto.RetakeDetailsDto
import data.dto.toEnrollmentDto
import data.dto.toRetakeDto
import domain.repository.StudentRepository
import domain.repository.UserRepository
import domain.usecases.GetRetakeDetailsUseCase
import domain.usecases.GetTeacherRetakesUseCase
import domain.usecases.GradeStudentUseCase
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import security.currentEmail

class TeacherController(
    private val userRepository: UserRepository,
    private val studentRepository: StudentRepository,
    private val getTeacherRetakesUseCase: GetTeacherRetakesUseCase,
    private val getRetakeDetailsUseCase: GetRetakeDetailsUseCase,
    private val gradeStudentUseCase: GradeStudentUseCase
) {
    fun configure(route: Route) {
        route.get("/retakes") {
            val email = call.currentEmail() ?: return@get call.respond(
                HttpStatusCode.Unauthorized,
                mapOf("error" to "Invalid token")
            )
            val user = userRepository.findByEmail(email) ?: return@get call.respond(
                HttpStatusCode.Unauthorized,
                mapOf("error" to "User not found")
            )
            val teacherId = user.id
            val retakes = getTeacherRetakesUseCase(teacherId)
            call.respond(retakes.map { it.toRetakeDto() })
        }
        route.get("/retake/{retakeId}") {
            val email = call.currentEmail() ?: return@get call.respond(
                HttpStatusCode.Unauthorized,
                mapOf("error" to "Invalid token")
            )
            val user = userRepository.findByEmail(email) ?: return@get call.respond(
                HttpStatusCode.Unauthorized,
                mapOf("error" to "User not found")
            )
            val teacherId = user.id
            val retakeId = call.pathRetakeId() ?: return@get
            val details = getRetakeDetailsUseCase(retakeId)
            if (!details.retake.teacherIds.contains(teacherId)) {
                return@get call.respond(HttpStatusCode.Forbidden, mapOf("error" to "You don't have access to this retake"))
            }
            call.respond(
                RetakeDetailsDto(
                    retake = details.retake.toRetakeDto(),
                    enrollments = details.enrollments.map { it.toEnrollmentDto() }
                )
            )
        }
        route.post("/retake/{retakeId}/student/{studentId}/grade") {
            val email = call.currentEmail() ?: return@post call.respond(
                HttpStatusCode.Unauthorized,
                mapOf("error" to "Invalid token")
            )
            val user = userRepository.findByEmail(email) ?: return@post call.respond(
                HttpStatusCode.Unauthorized,
                mapOf("error" to "User not found")
            )
            val teacherId = user.id
            val retakeId = call.pathRetakeId() ?: return@post
            val studentId = call.pathStudentId() ?: return@post
            val request = call.receive<GradeRequestDto>()
            val retake = studentRepository.findRetakeById(retakeId) ?: return@post call.respond(
                HttpStatusCode.NotFound,
                mapOf("error" to "Retake not found")
            )
            if (!retake.teacherIds.contains(teacherId)) {
                return@post call.respond(HttpStatusCode.Forbidden, mapOf("error" to "You don't have access to this retake"))
            }
            val enrollment = gradeStudentUseCase(retakeId, studentId,        retake.type, request.score)
            call.respond(HttpStatusCode.OK, enrollment.toEnrollmentDto())
        }
    }

    private suspend fun ApplicationCall.pathRetakeId(): Long? {
        val raw = parameters["retakeId"] ?: return null
        return raw.toLongOrNull() ?: run {
            respond(HttpStatusCode.BadRequest, mapOf("error" to "Path parameter 'retakeId' must be a number"))
            null
        }
    }

    private suspend fun ApplicationCall.pathStudentId(): Long? {
        val raw = parameters["studentId"] ?: return null
        return raw.toLongOrNull() ?: run {
            respond(HttpStatusCode.BadRequest, mapOf("error" to "Path parameter 'studentId' must be a number"))
            null
        }
    }
}