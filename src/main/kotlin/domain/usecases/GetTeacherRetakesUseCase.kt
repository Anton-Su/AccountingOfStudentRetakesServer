package domain.usecases

import domain.model.Retake
import domain.repository.StudentRepository
import domain.repository.TeacherRepository

class GetTeacherRetakesUseCase(
    private val teacherRepository: TeacherRepository
) {
    suspend operator fun invoke(teacherId: Long): List<Retake> {
        require(teacherId > 0) { "Teacher ID must be positive" }
        return teacherRepository.findRetakesByTeacherId(teacherId)
    }
}
