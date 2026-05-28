package data.dto

import kotlinx.serialization.Serializable
import domain.model.Retake

@Serializable
data class CreateRetakeResponseDto(
	val id: Long,
	val startAt: String,
	val endAt: String,
	val subjectId: Long,
	val teacherIds: List<Long>,
	val type: String,
	val place: String,
	val lastModified: String,
	val admission: String?
)


fun Retake.toCreateRetakeResponseDto() = CreateRetakeResponseDto(
	id = this.id,
	startAt = this.startAt.toString(),
	endAt = this.endAt.toString(),
	subjectId = this.subjectId,
	teacherIds = this.teacherIds,
	type = this.type,
	place = this.place,
	lastModified = this.lastModified.toString(),
	admission = this.admission
)
