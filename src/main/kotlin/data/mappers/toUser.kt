package data.mappers

import data.databases.UsersTable
import domain.model.User
import org.jetbrains.exposed.sql.ResultRow

fun ResultRow.toUser() = User(
        id = this[UsersTable.id].value,
        role = this[UsersTable.role],
        firstName = this[UsersTable.firstName],
        secondName = this[UsersTable.secondName],
        lastName = this[UsersTable.lastName],
        gender = this[UsersTable.gender],
        age = this[UsersTable.age],
        email = this[UsersTable.email],
        passwordHash = this[UsersTable.passwordHash]
)