package data.dto

import kotlinx.serialization.Serializable

@Serializable
data class CommentDto(
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