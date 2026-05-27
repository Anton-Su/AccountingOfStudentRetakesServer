package controller

import data.dto.*
import domain.model.UserRole
import domain.usecases.CreateRetakeUseCase
import domain.usecases.GetSubjectsUseCase
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
    private val getSubjectsUseCase: GetSubjectsUseCase,
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
                    val teachers = getTeachersByDisciplineUseCase(discipline)
                    call.respond(teachers.map { it.toTeacherDto() })
                }
                get("admin/subjects") {
                    call.requireRole(UserRole.ADMIN)
                    val subjects = getSubjectsUseCase()
                    call.respond(subjects.map { it.toSubjectDto() })
                }
                post("admin/create_retake") {
                    call.requireRole(UserRole.ADMIN)
                    val request = call.receive<CreateRetakeRequestDto>()
                    val retake = createRetakeUseCase(
                            startAtIso = request.startAt,
                            endAtIso = request.endAt,
                            teacherIds = request.teacherIds,
                            type = request.type,
                            place = request.place,
                            admission = request.admission
                    )
                    call.respond(HttpStatusCode.Created, retake.toCreateRetakeResponseDto())
                }
                put("admin/redact_retake") {
                    call.requireRole(UserRole.ADMIN)
                    val request = call.receive<CreateRetakeRequestDto>()
                    val idParam = call.request.queryParameters["id"]
                        ?: return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Query parameter 'id' is required"))
                    val id = try { idParam.toLong() }
                    catch (_: Exception) {
                        return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Query parameter 'id' must be a number"))
                    }
                    val updated = redactRetakeUseCase(
                            id = id,
                            startAtIso = request.startAt,
                            endAtIso = request.endAt,
                            teacherIds = request.teacherIds,
                            type = request.type,
                            place = request.place,
                            admission = request.admission
                    )
                    call.respond(HttpStatusCode.OK, updated.toCreateRetakeResponseDto())
                }
            }
        }
    }
}