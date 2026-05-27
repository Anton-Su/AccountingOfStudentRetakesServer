package data.dto

import kotlinx.serialization.Serializable

@Serializable
data class GradeRequestDto(
    val score: Int
)