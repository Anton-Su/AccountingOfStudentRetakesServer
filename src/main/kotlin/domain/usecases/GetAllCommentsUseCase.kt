package domain.usecases

import domain.model.Comment
import domain.repository.AdminRepository

class GetAllCommentsUseCase(
    private val adminRepository: AdminRepository
) {
    suspend operator fun invoke(): List<Comment> = adminRepository.getAllComments()
}