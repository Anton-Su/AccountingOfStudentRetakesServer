package Security

import domain.model.UserRole
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.principal
import io.ktor.server.auth.jwt.JWTPrincipal


fun ApplicationCall.requireRole(vararg allowedRoles: UserRole): UserRole {
    val principal = principal<JWTPrincipal>() ?: throw ForbiddenException("Failed to read user data from token")
    val roleClaim = principal.payload.getClaim("role").asString()?.trim().orEmpty()
    val role = runCatching { UserRole.valueOf(roleClaim) }.getOrElse { throw ForbiddenException("Role claim is invalid") }
    if (role !in allowedRoles)
        throw ForbiddenException("Insufficient permissions for this resource")
    return role
}

