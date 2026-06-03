package domain.model.mappers

import data.dto.SubjectDto
import domain.model.Subject

fun Subject.toSubjectDto() = SubjectDto(
    id = this.id,
    title = this.title
)