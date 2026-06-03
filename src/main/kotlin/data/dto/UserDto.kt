package data.dto

import domain.model.UserRole
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: Long,
    val role: UserRole,
    val firstName: String,
    val secondName: String,
    val lastName: String,
    val gender: String,
    val age: Int,
    val email: String,
)