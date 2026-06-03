package domain.model.mappers

import data.dto.RetakeDto
import domain.model.Retake

fun Retake.toRetakeDto() = RetakeDto(
    id = this.id,
    type = this.type,
    place = this.place,
    startAt = this.startAt.toString(),
    endAt = this.endAt.toString(),
    lastModified = this.lastModified.toString(),
    teacherIds = this.teacherIds,
    subjectId = this.subjectId,
    admission = this.admission
)
