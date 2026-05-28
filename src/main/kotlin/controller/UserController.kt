package controller

import data.dto.toUserDto
import domain.model.UserRole
import domain.repository.UserRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.auth.authenticate
import security.requireRole

class UserController(
    private val userRepository: UserRepository
) {
    fun configure(application: Application) {
        application.routing {
            authenticate("auth-jwt") {
                get("api/users/me") {
                    call.requireRole(UserRole.ADMIN, UserRole.TEACHER, UserRole.STUDENT)
                    val user = userRepository.getUser(call) ?: return@get call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "User not found"))
                    call.respond(user.toUserDto())
                }
            }
        }
    }
}


