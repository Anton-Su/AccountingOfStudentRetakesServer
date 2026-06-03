package controller

import data.dto.*
import domain.model.mappers.toCommentDto
import domain.model.mappers.toRetakeDto
import domain.model.mappers.toSubjectDto
import domain.model.mappers.toTeacherDto
import domain.usecases.*
import helpers.longPathParam
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class AdminController(
    private val getTeachersByDisciplineUseCase: GetTeachersByDisciplineUseCase,
    private val getSubjectsUseCase: GetSubjectsUseCase,
    private val createRetakeUseCase: CreateRetakeUseCase,
    private val redactRetakeUseCase: RedactRetakeUseCase,
    private val getAllCommentsUseCase: GetAllCommentsUseCase,
    private val getAllRetakesUseCase: GetAllRetakesUseCase,
    private val deleteRetakeUseCase: DeleteRetakeUseCase,
) {
    fun configure(route: Route) {
        route.get("/teachers") {
            val discipline = call.request.queryParameters["discipline"] ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Query parameter 'discipline' is required"))
            println(discipline)
            val teachers = getTeachersByDisciplineUseCase(discipline)
            call.respond(teachers.map { it.toTeacherDto() })
        }
        route.get("/subjects") {
            val subjects = getSubjectsUseCase()
            call.respond(subjects.map { it.toSubjectDto() })
        }
        route.post("/create_retake") {
            val request = call.receive<CreateRetakeRequest>()
            val retake = createRetakeUseCase(startAtIso = request.startAt, endAtIso = request.endAt,
                teacherIds = request.teacherIds, subjectId = request.subjectId, type = request.type,
                place = request.place, admission = request.admission
            )
            call.respond(HttpStatusCode.Created, retake.toRetakeDto())
        }
        route.put("/retakes/{id}") {
            val id = call.parameters["id"]?.toLongOrNull() ?: return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid id"))
            val request = call.receive<CreateRetakeRequest>()
            val updated = redactRetakeUseCase(id = id, startAtIso = request.startAt, endAtIso = request.endAt,
                teacherIds = request.teacherIds, type = request.type, place = request.place,
                admission = request.admission, subjectId = request.subjectId,)
            call.respond(HttpStatusCode.OK, updated.toRetakeDto())
        }
        route.delete("/retakes/{id}") {
            val id = call.longPathParam("id") ?: return@delete
            deleteRetakeUseCase(id)
            call.respond(HttpStatusCode.OK, mapOf("message" to "Retake deleted"))
        }
        route.get("/retakes") {
            val retakes = getAllRetakesUseCase()
            call.respond(retakes.map { it.toRetakeDto() })
        }
        route.get("/comments") {
            val comments = getAllCommentsUseCase()
            call.respond(comments.map { it.toCommentDto() })
        }
    }
}