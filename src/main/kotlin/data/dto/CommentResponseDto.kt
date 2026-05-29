package data.dto

import domain.model.Comment
import kotlinx.serialization.Serializable

@Serializable
data class CommentResponseDto(
    val id: Long,
    val studentId: Long,
    val studentFullName: String,
    val subjectTitle: String,
    val groupName: String,
    val gradeplace: Int,
    val gradeteacher: Int,
    val gradeoverall: Int,
    val comment: String?,
    val retakeId: Long,
    val retakeStartAt: String,
    val retakeEndAt: String,
)

fun Comment.toDto(): CommentResponseDto = CommentResponseDto(
    id = id,
    studentId = studentId,
    gradeplace = gradeplace,
    gradeteacher = gradeteacher,
    gradeoverall = gradeoverall,
    comment = comment,
    retakeId = retakeId,
    retakeStartAt = retakeStartAt,
    retakeEndAt = retakeEndAt,
    studentFullName = studentFullName,
    subjectTitle = subjectTitle,
    groupName = groupName
)