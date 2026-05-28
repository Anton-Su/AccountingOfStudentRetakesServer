package domain.usecases

import domain.model.StudentDebt
import domain.repository.StudentRepository

class CancelRetakeEnrollmentUseCase(
    private val studentRepository: StudentRepository
) {
    suspend operator fun invoke(studentId: Long, debtId: Long, retakeId: Long): Boolean {
        return studentRepository.cancelRetakeEnrollment(studentId, debtId, retakeId)
    }
}
