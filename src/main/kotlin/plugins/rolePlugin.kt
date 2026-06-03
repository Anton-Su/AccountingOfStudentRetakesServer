package plugins

import domain.model.UserRole
import helpers.requireRole
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.auth.AuthenticationChecked

fun rolePlugin(vararg roles: UserRole) = createRouteScopedPlugin("RolePlugin") {
    on(AuthenticationChecked) { call ->
        call.requireRole(*roles)
    }
}