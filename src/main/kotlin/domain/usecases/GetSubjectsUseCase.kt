package domain.usecases

import domain.model.Subject
import domain.repository.AdminRepository

class GetSubjectsUseCase(
    private val adminRepository: AdminRepository
) {
    suspend operator fun invoke(): List<Subject> = adminRepository.findAllSubjects()
}

