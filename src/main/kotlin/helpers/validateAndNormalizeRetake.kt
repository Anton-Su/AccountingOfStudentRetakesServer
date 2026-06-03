package helpers

import java.time.Instant

fun validateAndNormalizeRetake(startAtIso: String, endAtIso: String, teacherIds: List<Long>, type: String, place: String): Triple<Instant, Instant, List<Long>> {
    val startAt = parseIsoInstant(startAtIso, "startAt")
    val endAt = parseIsoInstant(endAtIso, "endAt")
    require(startAt.isBefore(endAt)) { "startAt must be before endAt" }
    val normalizedTeacherIds = teacherIds.distinct().filter { it > 0 }
    require(normalizedTeacherIds.isNotEmpty()) { "teacherIds must contain at least one positive id" }
    require(type.trim().isNotBlank()) { "type is required" }
    require(place.trim().isNotBlank()) { "place is required" }
    return Triple(startAt, endAt, normalizedTeacherIds)
}