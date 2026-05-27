package domain.usecases

import domain.model.RetakeEnrollment
import domain.repository.StudentRepository

class GradeStudentUseCase(
    private val studentRepository: StudentRepository
) {
    suspend operator fun invoke(retakeId: Long, studentId: Long, score: Int): RetakeEnrollment {
        require(retakeId > 0) { "Retake ID must be positive" }
        require(studentId > 0) { "Student ID must be positive" }
        require(score in 0..100) { "Score must be between 0 and 100" }
        return studentRepository.gradeStudent(retakeId, studentId, score)
    }
}
