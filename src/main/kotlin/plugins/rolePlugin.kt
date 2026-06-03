package plugins

import domain.model.UserRole
import helpers.requireRole
import io.ktor.server.application.createRouteScopedPlugin

fun rolePlugin(vararg roles: UserRole) = createRouteScopedPlugin("RolePlugin") {
    onCall { call ->
        call.requireRole(*roles)
    }
}