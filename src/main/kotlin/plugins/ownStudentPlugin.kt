package plugins

import helpers.requireOwnStudent
import io.ktor.server.application.createRouteScopedPlugin

fun ownStudentPlugin() = createRouteScopedPlugin("OwnStudentPlugin") {
    onCall { call ->
        val studentId = call.parameters["studentId"]?.toLongOrNull() ?: return@onCall
        call.requireOwnStudent(studentId)
    }
}