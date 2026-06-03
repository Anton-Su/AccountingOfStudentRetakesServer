import dI.AppContainer
import domain.model.UserRole
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.*
import plugins.ownStudentPlugin
import plugins.rolePlugin

fun Application.configureRouting() {
    routing {
        route("/auth") {
            AppContainer.authController.configure(this)
        }
        authenticate("auth-jwt") {
            route("/api") {
                route("/users"){
                    install(rolePlugin(UserRole.ADMIN, UserRole.TEACHER, UserRole.STUDENT))
                    AppContainer.userController.configure(this)
                }
                route("/admin"){
                    install(rolePlugin(UserRole.ADMIN))
                    AppContainer.adminController.configure(this)
                }
                route("/student"){
                    install(rolePlugin(UserRole.STUDENT))
                    install(ownStudentPlugin())
                    AppContainer.studentController.configure(this)
                }
                route("/teacher"){
                    install(rolePlugin(UserRole.TEACHER))
                    AppContainer.teacherController.configure(this)
                }
            }
        }
    }
}
