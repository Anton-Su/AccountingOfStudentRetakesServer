package domain.usecases

import domain.model.RetakeEnrollment
import domain.repository.StudentRepository
import domain.repository.TeacherRepository

class GradeStudentUseCase(
    private val teacherRepository: TeacherRepository
) {
    suspend operator fun invoke(retakeId: Long, studentId: Long, type: String, score: Int): RetakeEnrollment {
        require(retakeId > 0) { "Retake ID must be positive" }
        require(studentId > 0) { "Student ID must be positive" }
        val range = if (type == "Экзамен") 2..5 else 2..3
        require(score in range) {
            "Invalid score for type=$type"
        }
        return teacherRepository.gradeStudent(retakeId, studentId, score)
    }
}
