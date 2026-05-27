package data.repository

import data.databases.UsersTable
import domain.model.User
import domain.repository.UserRepository
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class UserRepositoryImpl : UserRepository {
    override suspend fun findByEmail(email: String): User? {
        val normalized = email.trim().lowercase()
        return transaction {
            UsersTable.selectAll()
                .firstOrNull { it[UsersTable.email].trim().lowercase() == normalized }
                ?.toUser()
        }
    }

    override suspend fun findById(id: Int): User? {
        val targetId = id.toLong()
        return transaction {
            UsersTable.selectAll()
                .firstOrNull { it[UsersTable.id].value == targetId }
                ?.toUser()
        }
    }

    private fun org.jetbrains.exposed.sql.ResultRow.toUser(): User = User(
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
}