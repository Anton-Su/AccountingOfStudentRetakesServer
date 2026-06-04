package domain.usecases

import domain.model.Retake
import domain.repository.AdminRepository
import helpers.validateAndNormalizeRetake

class CreateRetakeUseCase(private val adminRepository: AdminRepository) {
    suspend operator fun invoke(startAtIso: String, endAtIso: String, teacherIds: List<Long>, subjectId: Long, type: String, place: String, admission: String?): Retake {
        val (startAt, endAt, normalizedTeacherIds) = validateAndNormalizeRetake(startAtIso, endAtIso, teacherIds, type, place)
        return adminRepository.createRetake(startAt, endAt, normalizedTeacherIds, type.trim(), place.trim(), admission?.trim(), subjectId)
    }
}