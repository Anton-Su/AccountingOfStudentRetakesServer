package controller

import data.dto.*
import domain.model.UserRole
import domain.usecases.CreateRetakeUseCase
import domain.usecases.DeleteRetakeUseCase
import domain.usecases.GetAllCommentsUseCase
import domain.usecases.GetAllRetakesUseCase
import domain.usecases.GetSubjectsUseCase
import domain.usecases.GetTeachersByDisciplineUseCase
import domain.usecases.RedactRetakeUseCase
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import security.requireRole

class AdminController(
    private val getTeachersByDisciplineUseCase: GetTeachersByDisciplineUseCase,
    private val getSubjectsUseCase: GetSubjectsUseCase,
    private val createRetakeUseCase: CreateRetakeUseCase,
    private val redactRetakeUseCase: RedactRetakeUseCase,
    private val getAllCommentsUseCase: GetAllCommentsUseCase,
    private val getAllRetakesUseCase: GetAllRetakesUseCase,
    private val deleteRetakeUseCase: DeleteRetakeUseCase,
) {
    fun configure(application: Application) {
        application.routing {
            authenticate("auth-jwt") {
                get("api/admin/teachers") {
                    call.requireRole(UserRole.ADMIN)
                    val discipline = call.request.queryParameters["discipline"]
                        ?: return@get call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Query parameter 'discipline' is required")
                        )
                    val teachers = getTeachersByDisciplineUseCase(discipline)
                    call.respond(teachers.map { it.toTeacherDto() })
                }
                get("api/admin/subjects") {
                    call.requireRole(UserRole.ADMIN)
                    val subjects = getSubjectsUseCase()
                    call.respond(subjects.map { it.toSubjectDto() })
                }
                post("api/admin/create_retake") {
                    call.requireRole(UserRole.ADMIN)
                    // val rawBody = call.receiveText()
                    // Raw request body: {"startAt":"2026-05-06T05:45:00","endAt":"2026-05-28T02:12:00","teacherIds":[3],"subjectId":3,"type":"Зачёт","place":"HI","admission":"Testfffff"}
                    // println("Raw request body: $rawBody") // Логируем
                    val request = call.receive<CreateRetakeRequestDto>()
                    println(request)
                    val retake = createRetakeUseCase(
                            startAtIso = request.startAt,
                            endAtIso = request.endAt,
                            teacherIds = request.teacherIds,
                            subjectId = request.subjectId,
                            type = request.type,
                            place = request.place,
                            admission = request.admission
                    )
                    call.respond(HttpStatusCode.Created, retake.toCreateRetakeResponseDto())
                }
                put("/api/admin/retakes/{id}") {
                    call.requireRole(UserRole.ADMIN)
                    val id = call.parameters["id"]?.toLongOrNull()
                        ?: return@put call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Invalid id")
                        )
                    val request = call.receive<CreateRetakeRequestDto>()
                    val updated = redactRetakeUseCase(
                        id = id,
                        startAtIso = request.startAt,
                        endAtIso = request.endAt,
                        teacherIds = request.teacherIds,
                        type = request.type,
                        place = request.place,
                        admission = request.admission,
                        subjectId = request.subjectId,
                    )
                    call.respond(HttpStatusCode.OK, updated.toCreateRetakeResponseDto())
                }
                delete("/api/admin/retakes/{id}") {
                    call.requireRole(UserRole.ADMIN)
                    val id = call.parameters["id"]?.toLongOrNull()
                        ?: return@delete call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Invalid id")
                        )
                    deleteRetakeUseCase(id)
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Retake deleted"))
                }

                get("api/admin/retakes") {
                    call.requireRole(UserRole.ADMIN)
                    val retakes = getAllRetakesUseCase()
                    call.respond(retakes.map { it.toRetakeDto() })
                }

                get("api/admin/comments") {
                    call.requireRole(UserRole.ADMIN)
                    val comments = getAllCommentsUseCase()
                    call.respond(comments.map { it.toDto() })
                }
            }
        }
    }
}