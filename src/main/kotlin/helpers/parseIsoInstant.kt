package helpers

import java.time.Instant

fun parseIsoInstant(value: String, fieldName: String): Instant {
    val normalized = value.trim()
    require(normalized.isNotBlank()) { "$fieldName is required" }
    return try {
        Instant.parse(normalized)
    } catch (_: Exception) {
        throw IllegalArgumentException("$fieldName must be ISO-8601 instant, e.g. 2026-05-26T10:00:00Z")
    }
}