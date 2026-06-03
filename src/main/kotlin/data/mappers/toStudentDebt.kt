package data.mappers

import data.databases.StudentSubjectsTable
import data.databases.SubjectsTable
import domain.model.StudentDebt
import org.jetbrains.exposed.sql.ResultRow

fun ResultRow.toStudentDebt() = StudentDebt(
    id = this[StudentSubjectsTable.id].value,
    subjectId = this[StudentSubjectsTable.subjectId].value,
    subjectTitle = this[SubjectsTable.title]
)