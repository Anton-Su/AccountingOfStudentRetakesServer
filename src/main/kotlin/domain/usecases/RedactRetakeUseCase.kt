package domain.usecases

import domain.model.Retake
import domain.repository.AdminRepository
import java.time.Instant

class RedactRetakeUseCase(
    private val adminRepository: AdminRepository
) {
    suspend operator fun invoke(id: Long, startAtIso: String, endAtIso: String, teacherIds: List<Long>, type: String, place: String, admission: String?): Retake {
        val startAt = parseIsoInstant(startAtIso, "startAt")
        val endAt = parseIsoInstant(endAtIso, "endAt")
        require(startAt.isBefore(endAt)) { "startAt must be before endAt" }
        val normalizedTeacherIds = teacherIds
            .map { it }
            .distinct()
            .filter { it > 0 }
        require(normalizedTeacherIds.isNotEmpty()) { "teacherIds must contain at least one positive id" }
        require(type.trim().isNotBlank()) { "type is required" }
        require(place.trim().isNotBlank()) { "place is required" }
        return adminRepository.updateRetake(id, startAt, endAt, normalizedTeacherIds, type.trim(), place.trim(), admission?.trim())
    }
}
private fun parseIsoInstant(value: String, fieldName: String): Instant {
    val normalized = value.trim()
    require(normalized.isNotBlank()) { "$fieldName is required" }
    return try {
        Instant.parse(normalized)
    } catch (_: Exception) {
        throw IllegalArgumentException("$fieldName must be ISO-8601 instant, e.g. 2026-05-26T10:00:00Z")
    }
}