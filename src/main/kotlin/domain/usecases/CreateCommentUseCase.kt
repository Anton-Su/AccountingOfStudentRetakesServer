package domain.usecases

import domain.model.Comment
import domain.model.Retake
import domain.repository.StudentRepository

class CreateCommentUseCase(
    private val studentRepository: StudentRepository
) {
    suspend operator fun invoke(studentId: Long, gradePlace: Int, gradeTeacher: Int, gradeOverall: Int, comment: String?, retakeId: Long): Comment {
        require(gradePlace in 1..10) { "gradePlace must be between 1 and 10" }
        require(gradeTeacher in 1..10) { "gradeTeacher must be between 1 and 10" }
        require(gradeOverall in 1..100) { "gradeOverall must be between 1 and 100" }
        return studentRepository.createComment(studentId, gradePlace, gradeTeacher, gradeOverall, comment, retakeId)
    }
}

