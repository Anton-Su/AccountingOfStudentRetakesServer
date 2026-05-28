package controller


import data.dto.LoginRequestDto
import data.dto.LoginResponseDto
import domain.usecases.LoginUseCase
import io.github.smiley4.ktoropenapi.post
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


class AuthController(
    private val loginUseCase: LoginUseCase
) {
    fun configure(application: Application) {
        application.routing {
            post("auth/login") {
                val request = call.receive<LoginRequestDto>()
                val token = loginUseCase.login(request.email, request.password, request.role)
                if (token != null) {
                    call.respond(LoginResponseDto(token))
                } else {
                    println("[AUTH] Login failed for email: ${request.email}")
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid email or password"))
                }
            }
        }
    }
}