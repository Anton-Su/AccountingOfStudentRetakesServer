package controller

import domain.model.mappers.toUserDto
import domain.repository.UserRepository
import helpers.currentEmail
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class UserController(
    private val userRepository: UserRepository
) {
    fun configure(route: Route) {
        route.get("/users/me") {
            val email = call.currentEmail() ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val user = userRepository.findByEmail(email) ?: return@get call.respond(HttpStatusCode.NotFound)
            call.respond(user.toUserDto())
        }
    }
}


