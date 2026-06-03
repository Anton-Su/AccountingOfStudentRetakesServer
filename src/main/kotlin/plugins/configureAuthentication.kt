package plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import security.JwtConfig

fun Application.configureAuthentication() {
    install(Authentication) {
        jwt("auth-jwt") {
            realm = "ktor-app"
            verifier(JwtConfig.verifier)
            validate { credential ->
                val email = credential.payload.getClaim("email").asString()
                val roleClaim = credential.payload.getClaim("role").asString()
                val exp = credential.payload.expiresAt?.time ?: 0
                val now = System.currentTimeMillis()
                val expired = exp < now
                val validRole = roleClaim?.isNotBlank() == true
                println("email: $email")
                println("role: $roleClaim")
                println("expired: $expired")
                println("validRole: $validRole")
                if (email != null && validRole && !expired) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid or expired token"))
            }
        }
    }
}