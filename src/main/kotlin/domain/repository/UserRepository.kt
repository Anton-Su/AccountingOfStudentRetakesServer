package domain.repository

import domain.model.User
import io.ktor.server.application.ApplicationCall

interface UserRepository {
    suspend fun findByEmail(email: String): User?
    suspend fun findById(id: Long): User?
}