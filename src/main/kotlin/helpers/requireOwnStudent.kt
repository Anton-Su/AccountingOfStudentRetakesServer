package helpers

import dI.AppContainer.userRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond

suspend fun ApplicationCall.requireOwnStudent(studentId: Long): Boolean {
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