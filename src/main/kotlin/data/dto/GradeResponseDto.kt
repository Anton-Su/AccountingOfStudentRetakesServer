package data.dto

import kotlinx.serialization.Serializable

@Serializable
data class GradeResponseDto(
    val id: Long,
    val retakeId: Long,
    val studentId: Long,
    val score: Int,
    val gradedAt: String
)