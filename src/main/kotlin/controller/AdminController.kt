package controller

import data.dto.CreateRetakeRequestDto
import data.dto.CreateRetakeResponseDto
import data.dto.TeacherDto
import domain.model.UserRole
import domain.usecases.CreateRetakeUseCase
import domain.usecases.GetTeachersByDisciplineUseCase
import domain.usecases.RedactRetakeUseCase
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import security.requireRole

class AdminController(
    private val getTeachersByDisciplineUseCase: GetTeachersByDisciplineUseCase,
    private val createRetakeUseCase: CreateRetakeUseCase,
    private val redactRetakeUseCase: RedactRetakeUseCase
) {
    fun configure(application: Application) {
        application.routing {
            authenticate("auth-jwt") {
                get("admin/teachers") {
                    call.requireRole(UserRole.ADMIN)
                    val discipline = call.request.queryParameters["discipline"]
                        ?: return@get call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Query parameter 'discipline' is required")
                        )
                    val teachers = try {
                        getTeachersByDisciplineUseCase(discipline)
                    } catch (e: IllegalArgumentException) {
                        return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Bad request")))
                    }
                    call.respond(teachers.map { TeacherDto(it.userId, it.disciplines) })
                }
                post("admin/create_retake") {
                    call.requireRole(UserRole.ADMIN)
                    val request = call.receive<CreateRetakeRequestDto>()
                    val retake = try {
                        createRetakeUseCase(
                            startAtIso = request.startAt,
                            endAtIso = request.endAt,
                            teacherIds = request.teacherIds,
                            type = request.type,
                            admission = request.admission
                        )
                    } catch (e: IllegalArgumentException) {
                        return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Bad request")))
                    }
                    call.respond(
                        HttpStatusCode.Created,
                        CreateRetakeResponseDto(
                            id = retake.id,
                            startAt = retake.startAt.toString(),
                            endAt = retake.endAt.toString(),
                            teacherIds = retake.teacherIds,
                            type = retake.type,
                            admission = retake.admission
                        )
                    )
                }
                put("admin/redact_retake") {
                    call.requireRole(UserRole.ADMIN)
                    val request = call.receive<data.dto.CreateRetakeRequestDto>()
                    // expecting id in query param or body? We'll expect id in query param 'id'
                    val idParam = call.request.queryParameters["id"]
                        ?: return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Query parameter 'id' is required"))
                    val id = try { idParam.toLong() } catch (_: Exception) {
                        return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Query parameter 'id' must be a number"))
                    }
                    val updated = try {
                        redactRetakeUseCase(
                            id = id,
                            startAtIso = request.startAt,
                            endAtIso = request.endAt,
                            teacherIds = request.teacherIds,
                            type = request.type,
                            admission = request.admission
                        )
                    } catch (e: IllegalArgumentException) {
                        return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Bad request")))
                    }
                    call.respond(HttpStatusCode.OK, CreateRetakeResponseDto(
                        id = updated.id,
                        startAt = updated.startAt.toString(),
                        endAt = updated.endAt.toString(),
                        teacherIds = updated.teacherIds,
                        type = updated.type,
                        admission = updated.admission
                    ))
                }
            }
        }
    }
}