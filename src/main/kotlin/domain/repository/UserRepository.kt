package domain.repository

import domain.model.User

interface UserRepository {
    suspend fun findByEmail(email: String): User?
    suspend fun findById(id: Int): User?
}