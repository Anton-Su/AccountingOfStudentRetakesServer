package domain.usecases

import domain.model.RetakeEnrollment
import domain.repository.StudentRepository

class GradeStudentUseCase(
    private val studentRepository: StudentRepository
) {
    suspend operator fun invoke(retakeId: Long, studentId: Long, type: String, score: Int): RetakeEnrollment {
        require(retakeId > 0) { "Retake ID must be positive" }
        require(studentId > 0) { "Student ID must be positive" }
        val range = if (type == "Экзамен") 2..5 else 2..3
        require(score in range) {
            "Invalid score for type=$type"
        }
        return studentRepository.gradeStudent(retakeId, studentId, score)
    }
}
