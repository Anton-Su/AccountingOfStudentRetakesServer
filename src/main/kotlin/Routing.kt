package com.example

import security.currentEmail
import security.requireRole
import com.example.dI.AppContainer
import domain.model.UserRole
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("This is a start page!")
        }
        authenticate("auth-jwt") {
            get("/api/me") {
                val role = call.requireRole(UserRole.ADMIN, UserRole.TEACHER, UserRole.STUDENT)
                val email = call.currentEmail() ?: "unknown"
                call.respondText("Authenticated as $email with role $role")
            }
            get("/api/admin") {
                call.requireRole(UserRole.ADMIN)
                call.respondText("Admin resource")
            }
            get("/api/teacher") {
                call.requireRole(UserRole.ADMIN, UserRole.TEACHER)
                call.respondText("Teacher resource")
            }
            get("/api/student") {
                call.requireRole(UserRole.STUDENT)
                call.respondText("Student resource")
            }
        }
    }
    AppContainer.authController.configure(this)
    AppContainer.adminController.configure(this)
}
