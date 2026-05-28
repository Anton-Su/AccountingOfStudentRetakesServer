package domain.usecases

import domain.model.Retake
import domain.repository.AdminRepository

class GetAllRetakesUseCase(private val adminRepository: AdminRepository) {
    suspend operator fun invoke(): List<Retake> = adminRepository.findAllRetakes()
}