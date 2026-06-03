package data.dto

import kotlinx.serialization.Serializable

@Serializable
data class EnrollmentDto(
    val id: Long,
    val retakeId: Long,
    val studentId: Long,
    val studentSubjectId: Long,
    val studentFullName: String,
    val groupName: String,
)