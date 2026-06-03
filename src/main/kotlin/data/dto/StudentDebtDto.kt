package data.dto

import kotlinx.serialization.Serializable

@Serializable
data class StudentDebtDto(
    val id: Long,
    val subjectTitle: String,
    val subjectId: Long,
)