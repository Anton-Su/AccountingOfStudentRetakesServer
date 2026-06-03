package data.dto

import kotlinx.serialization.Serializable

@Serializable
data class RetakeDto(
    val id: Long,
    val type: String,
    val place: String,
    val startAt: String,
    val endAt: String,
    val lastModified: String,
    val teacherIds: List<Long>,
    val admission: String? = null,
    val subjectId: Long
)