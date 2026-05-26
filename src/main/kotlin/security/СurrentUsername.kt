package security

import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal

fun ApplicationCall.currentEmail(): String? {
    val principal = principal<JWTPrincipal>()
    if (principal != null) {
        val claim = principal.payload.getClaim("email")
        val email = claim.asString()
        if (email != null) {
            val trimmed = email.trim()
            if (trimmed.isNotBlank()) {
                return trimmed
            }
        }
    }
    return null
}