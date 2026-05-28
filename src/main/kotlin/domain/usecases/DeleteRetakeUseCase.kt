package domain.usecases

import domain.repository.AdminRepository

class DeleteRetakeUseCase(private val adminRepository: AdminRepository) {
    suspend operator fun invoke(id: Long) = adminRepository.deleteRetake(id)
}