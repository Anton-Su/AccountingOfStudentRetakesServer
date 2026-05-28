package data.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateRetakeRequestDto(
	val startAt: String,
	val endAt: String,
	val teacherIds: List<Long>,
	val subjectId: Long,
	val type: String,
	val place: String,
	val admission: String? = null
)