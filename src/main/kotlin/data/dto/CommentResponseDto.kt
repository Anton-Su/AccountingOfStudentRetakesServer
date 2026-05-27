package data.dto

import domain.model.Comment
import kotlinx.serialization.Serializable

@Serializable
data class CommentResponseDto(
    val id: Long,
    val studentId: Long,
    val gradeplace: Int,
    val gradeteacher: Int,
    val gradeoverall: Int,
    val comment: String? = null
)

fun Comment.toDto(): CommentResponseDto = CommentResponseDto(
    id = id,
    studentId = studentId,
    gradeplace = gradeplace,
    gradeteacher = gradeteacher,
    gradeoverall = gradeoverall,
    comment = comment
)