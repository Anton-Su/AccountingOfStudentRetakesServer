package plugins

import domain.model.UserRole
import io.ktor.server.application.createRouteScopedPlugin
import security.requireRole

fun rolePlugin(vararg roles: UserRole) = createRouteScopedPlugin("RolePlugin") {
    onCall { call ->
        call.requireRole(*roles)
    }
}