package domain.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Long,
    val role: UserRole,
    val firstName: String,
    val secondName: String,
    val lastName: String,
    val gender: String,
    val age: Int,
    val email: String,
    val passwordHash: String,
)