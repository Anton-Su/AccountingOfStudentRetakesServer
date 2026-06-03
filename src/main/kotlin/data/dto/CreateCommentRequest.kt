package data.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateCommentRequest(
    val gradePlace: Int,
    val gradeTeacher: Int,
    val gradeOverall: Int,
    val comment: String? = null,
    val retakeId: Long,
)