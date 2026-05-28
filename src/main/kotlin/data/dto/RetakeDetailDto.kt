package data.dto

import kotlinx.serialization.Serializable
import domain.model.Retake

@Serializable
data class RetakeDetailDto(
    val id: Long,
    val type: String,
    val place: String,
    val startAt: String,
    val endAt: String,
    val lastModified: String,
    val teacherIds: List<Long>,
    val admission: String? = null
)

fun Retake.toRetakeDetailDto(): RetakeDetailDto = RetakeDetailDto(
    id = this.id,
    type = this.type,
    place = this.place,
    startAt = this.startAt.toString(),
    endAt = this.endAt.toString(),
    lastModified = this.lastModified.toString(),
    teacherIds = this.teacherIds,
    admission = this.admission
)
