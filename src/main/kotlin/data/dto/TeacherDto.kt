package data.dto

import kotlinx.serialization.Serializable

@Serializable
data class TeacherDto(
    val userId: Long,
    val disciplines: List<String>
)
