package data.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateCommentRequestDto(
    val gradeplace: Int,
    val gradeteacher: Int,
    val gradeoverall: Int,
    val comment: String? = null,
    val retakeId: Long,
)