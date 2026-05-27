package domain.repository

import domain.model.User
import io.ktor.server.application.ApplicationCall

interface UserRepository {
    suspend fun findByEmail(email: String): User?
    suspend fun findById(id: Int): User?
    suspend fun getUser(call: ApplicationCall): User?
}