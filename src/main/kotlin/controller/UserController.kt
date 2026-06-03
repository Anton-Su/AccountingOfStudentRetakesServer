package controller

import data.dto.toUserDto
import domain.repository.UserRepository
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class UserController(
    private val userRepository: UserRepository
) {
    fun configure(route: Route) {
        route.get("/me") {
            val user = userRepository.getUser(call)
                ?: return@get call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "User not found"))
            call.respond(user.toUserDto())
        }
    }
}


