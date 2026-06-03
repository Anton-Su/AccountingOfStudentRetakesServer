package data.dto

import kotlinx.serialization.Serializable

@Serializable
data class GradeRequest(
    val score: Int
)