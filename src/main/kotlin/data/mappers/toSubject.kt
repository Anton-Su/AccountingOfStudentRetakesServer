package data.mappers

import data.databases.SubjectsTable
import domain.model.Subject
import org.jetbrains.exposed.sql.ResultRow

fun ResultRow.toSubject() = Subject(
    id = this[SubjectsTable.id].value,
    title = this[SubjectsTable.title]
)