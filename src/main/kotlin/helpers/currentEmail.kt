package helpers

import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal

fun ApplicationCall.currentEmail(): String? {
    val principal = principal<JWTPrincipal>()
    if (principal != null) {
        val emailClaim = principal.payload.getClaim("email").asString()
        if (emailClaim != null) {
            val trimmed = emailClaim.trim()
            if (trimmed.isNotBlank()) {
                return trimmed
            }
        }
    }
    return null
}