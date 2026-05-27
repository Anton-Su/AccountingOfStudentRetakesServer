package data.dto

import kotlinx.serialization.Serializable
import domain.model.Retake
import domain.model.RetakeEnrollment

@Serializable
data class RetakeDetailDto(
    val id: Long,
    val type: String,
    val startAt: String,
    val endAt: String,
    val teacherIds: List<Long>,
    val admission: String? = null
)

fun Retake.toRetakeDetailDto(): RetakeDetailDto = RetakeDetailDto(
    id = this.id,
    type = this.type,
    startAt = this.startAt.toString(),
    endAt = this.endAt.toString(),
    teacherIds = this.teacherIds,
    admission = this.admission
)
