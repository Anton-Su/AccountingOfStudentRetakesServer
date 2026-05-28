package domain.usecases

import domain.model.StudentDebt
import domain.repository.StudentRepository

class GetStudentDebtsUseCase(
    private val studentRepository: StudentRepository
) {
    suspend operator fun invoke(studentId: Long): List<StudentDebt> {
        return studentRepository.findDebtsByStudentId(studentId).mapNotNull { debt ->
            val subject = studentRepository.findSubjectById(debt.subjectId) ?: return@mapNotNull null
            StudentDebt(
                id = debt.id,
                subjectId = subject.id,
                subjectTitle = subject.title,
                //teacherId = debt.teacherId,
                //createdAt = debt.createdAt,
                //status = debt.status,
                //retakeId = studentRepository.findRetakeIdByDebtId(debt.id)
            )
        }
    }
}
