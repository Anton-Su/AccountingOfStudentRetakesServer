package domain.usecases

import domain.model.Retake
import domain.repository.StudentRepository

class GetTeacherRetakesUseCase(
    private val studentRepository: StudentRepository
) {
    suspend operator fun invoke(teacherId: Long): List<Retake> {
        require(teacherId > 0) { "Teacher ID must be positive" }
        return studentRepository.findRetakesByTeacherId(teacherId)
    }
}
