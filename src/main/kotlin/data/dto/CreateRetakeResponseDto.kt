package data.dto

import kotlinx.serialization.Serializable
import domain.model.Retake

@Serializable
data class CreateRetakeResponseDto(
	val id: Long,
	val startAt: String,
	val endAt: String,
	val teacherIds: List<Long>,
	val type: String,
	val admission: String?
)


fun Retake.toCreateRetakeResponseDto() = CreateRetakeResponseDto(
	id = this.id,
	startAt = this.startAt.toString(),
	endAt = this.endAt.toString(),
	teacherIds = this.teacherIds,
	type = this.type,
	admission = this.admission
)
