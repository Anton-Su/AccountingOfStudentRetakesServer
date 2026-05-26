package domain.usecases

import domain.model.Teacher
import domain.repository.AdminRepository

class GetTeachersByDisciplineUseCase(
    private val adminRepository: AdminRepository
) {
    suspend operator fun invoke(discipline: String): List<Teacher> {
        val normalized = discipline.trim()
        require(normalized.isNotBlank()) { "Query parameter 'discipline' is required" }
        return adminRepository.findTeachersByDiscipline(normalized)
    }
}