package plugins

import helpers.requireOwnStudent
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.auth.AuthenticationChecked

fun ownStudentPlugin() = createRouteScopedPlugin("OwnStudentPlugin") {
    on(AuthenticationChecked) { call ->
        val studentId = call.parameters["studentId"]?.toLongOrNull() ?: return@on
        call.requireOwnStudent(studentId)
    }
}