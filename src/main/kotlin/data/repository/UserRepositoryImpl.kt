package data.repository

import data.databases.UsersTable
import domain.model.User
import domain.repository.UserRepository
import io.ktor.server.application.ApplicationCall
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import security.currentEmail

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

    override suspend fun getUser(call: ApplicationCall): User? {
        val email = call.currentEmail() ?: return null
        return findByEmail(email)
    }

    private fun ResultRow.toUser(): User = User(
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