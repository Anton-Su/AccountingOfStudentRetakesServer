package domain.usecases

import domain.model.Retake
import domain.repository.AdminRepository
import helpers.validateAndNormalizeRetake

class RedactRetakeUseCase(
    private val adminRepository: AdminRepository
) {
    suspend operator fun invoke(id: Long, startAtIso: String, endAtIso: String, teacherIds: List<Long>, type: String, place: String, admission: String?, subjectId: Long): Retake {
        val (startAt, endAt, normalizedTeacherIds) = validateAndNormalizeRetake(startAtIso, endAtIso, teacherIds, type, place)
        return adminRepository.updateRetake(id, startAt, endAt, normalizedTeacherIds, type.trim(), place.trim(), admission?.trim(), subjectId = subjectId)
    }
}