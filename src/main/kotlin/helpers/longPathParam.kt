package helpers

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond

suspend fun ApplicationCall.longPathParam(name: String): Long? {
    val raw = parameters[name] ?: return null
    return raw.toLongOrNull() ?: run {
        respond(HttpStatusCode.BadRequest, mapOf("error" to "Path parameter '$name' must be a number"))
        null
    }
}
