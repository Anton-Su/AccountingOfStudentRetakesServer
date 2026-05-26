package data.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateRetakeRequestDto(
	val startAt: String,
	val endAt: String,
	val teacherIds: List<Long>,
	val type: String,
	val admission: String? = null
)