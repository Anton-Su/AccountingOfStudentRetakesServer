package domain.usecases

import domain.model.Retake
import domain.repository.StudentRepository

class GetEnrolledRetakesUseCase(
    private val studentRepository: StudentRepository
) {
    suspend operator fun invoke(studentId: Long): List<Retake> =
        studentRepository.findEnrolledRetakes(studentId)
}