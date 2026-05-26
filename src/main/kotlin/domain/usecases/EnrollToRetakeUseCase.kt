package domain.usecases

import domain.model.StudentDebt
import domain.repository.StudentRepository

class EnrollToRetakeUseCase(
    private val studentRepository: StudentRepository
) {
    suspend operator fun invoke(studentId: Long, debtId: Long, retakeId: Long): StudentDebt {
        val debt = studentRepository.enrollToRetake(studentId, debtId, retakeId)
        val subject = studentRepository.findSubjectById(debt.subjectId)
            ?: throw IllegalArgumentException("Subject with id ${debt.subjectId} not found")
        return StudentDebt(
            debtId = debt.id,
            subjectId = subject.id,
            subjectTitle = subject.title,
            teacherId = debt.teacherId,
            createdAt = debt.createdAt,
            status = debt.status,
            retakeId = studentRepository.findRetakeIdByDebtId(debt.id)
        )
    }
}
