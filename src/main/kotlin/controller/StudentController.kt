package controller

import data.dto.*
import domain.model.UserRole
import domain.repository.UserRepository
import domain.usecases.CancelRetakeEnrollmentUseCase
import domain.usecases.EnrollToRetakeUseCase
import domain.usecases.GetStudentDebtsUseCase
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import security.currentEmail
import security.requireRole

class StudentController(
    private val userRepository: UserRepository,
    private val getStudentDebtsUseCase: GetStudentDebtsUseCase,
    private val enrollToRetakeUseCase: EnrollToRetakeUseCase,
    private val cancelRetakeEnrollmentUseCase: CancelRetakeEnrollmentUseCase
) {
    fun configure(application: Application) {
        application.routing {
            authenticate("auth-jwt") {
                get("api/student/{studentId}/debts") {
                    call.requireRole(UserRole.STUDENT)
                    val studentId = call.pathStudentId() ?: return@get
                    if (!call.requireOwnStudent(studentId)) return@get
                    val debts = try {
                        getStudentDebtsUseCase(studentId)
                    } catch (e: IllegalArgumentException) {
                        return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Bad request")))
                    }
                    call.respond(debts.map { it.toDto() })
                }

                put("api/student/{studentId}/debts/{debtId}/retakes/{retakeId}") {
                    call.requireRole(UserRole.STUDENT)
                    val studentId = call.pathStudentId() ?: return@put
                    if (!call.requireOwnStudent(studentId)) return@put
                    val debtId = call.longPathParam("debtId") ?: return@put
                    val retakeId = call.longPathParam("retakeId") ?: return@put
                    val updated = try {
                        enrollToRetakeUseCase(studentId, debtId, retakeId)
                    } catch (e: IllegalArgumentException) {
                        return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Bad request")))
                    }
                    call.respond(updated.toDto())
                }
                delete("api/student/{studentId}/debts/{debtId}/retakes/{retakeId}") {
                    call.requireRole(UserRole.STUDENT)
                    val studentId = call.pathStudentId() ?: return@delete
                    if (!call.requireOwnStudent(studentId)) return@delete
                    val debtId = call.longPathParam("debtId") ?: return@delete
                    val retakeId = call.longPathParam("retakeId") ?: return@delete
                    val updated = try {
                        cancelRetakeEnrollmentUseCase(studentId, debtId, retakeId)
                    } catch (e: IllegalArgumentException) {
                        return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Bad request")))
                    }
                    call.respond(updated.toDto())
                }
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
}

