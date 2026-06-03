package domain.model.mappers

import data.dto.StudentDebtDto
import domain.model.StudentDebt

fun StudentDebt.toStudentDebtDto() = StudentDebtDto(
    id = this.id,
    subjectTitle = this.subjectTitle,
    subjectId = this.subjectId
)
