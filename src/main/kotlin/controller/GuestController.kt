package controller

import domain.model.mappers.toSubjectDto
import domain.usecases.GetSubjectsUseCase
import io.ktor.server.response.*
import io.ktor.server.routing.*

class GuestController(
    private val getSubjectsUseCase: GetSubjectsUseCase
){
    fun configure(route: Route) {
        route.get("/subjects") {
            val subjects = getSubjectsUseCase()
            call.respond(subjects.map { it.toSubjectDto() })
        }
    }
}