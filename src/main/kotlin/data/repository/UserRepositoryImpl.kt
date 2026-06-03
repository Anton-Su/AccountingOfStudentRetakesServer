package data.repository

import data.databases.UsersTable
import data.mappers.toUser
import domain.model.User
import domain.repository.UserRepository
import io.ktor.server.application.ApplicationCall
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import helpers.currentEmail
import kotlin.text.toLong

class UserRepositoryImpl : UserRepository {
    override suspend fun findByEmail(email: String): User? = transaction {
        val normalized = email.trim().lowercase()
        UsersTable.selectAll()
            .where { UsersTable.email eq normalized }
            .firstOrNull()
            ?.toUser()
    }

    override suspend fun findById(id: Long): User? = transaction {
        UsersTable.selectAll()
            .where { UsersTable.id eq id }
            .firstOrNull()
            ?.toUser()
    }
}