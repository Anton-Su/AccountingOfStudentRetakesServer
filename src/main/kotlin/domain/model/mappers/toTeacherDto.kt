package domain.model.mappers

import data.dto.TeacherDto
import domain.model.Teacher

fun Teacher.toTeacherDto() = TeacherDto(
    userId = this.userId,
    fullName = this.fullName,
    disciplines = this.disciplines
)