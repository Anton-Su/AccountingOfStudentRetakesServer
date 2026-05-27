package domain.usecases

import domain.model.Comment
import domain.model.Retake
import domain.repository.StudentRepository

class CreateCommentUseCase(
    private val studentRepository: StudentRepository
) {
    suspend operator fun invoke(
        studentId: Long,
        gradeplace: Int,
        gradeteacher: Int,
        gradeoverall: Int,
        comment: String?,
        retakeId: Long
    ): Comment {
        require(gradeplace in 1..10) { "gradeplace must be between 1 and 10" }
        require(gradeteacher in 1..10) { "gradeteacher must be between 1 and 10" }
        require(gradeoverall in 1..100) { "gradeoverall must be between 1 and 100" }
        return studentRepository.createComment(studentId, gradeplace, gradeteacher, gradeoverall, comment, retakeId)
    }
}

