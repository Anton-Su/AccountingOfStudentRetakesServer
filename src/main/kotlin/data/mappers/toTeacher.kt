package data.mappers

import data.databases.UsersTable
import domain.model.Teacher
import org.jetbrains.exposed.sql.ResultRow

fun ResultRow.toTeacher(disciplines: List<String>) = Teacher(
    userId = this[UsersTable.id].value,
    fullName = "${this[UsersTable.secondName]} ${this[UsersTable.firstName]} ${this[UsersTable.lastName]}",
    disciplines = disciplines
)