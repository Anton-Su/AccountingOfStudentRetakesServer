package data.dto

import domain.model.User
import domain.model.UserRole
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val role: UserRole,
    val firstName: String,
    val secondName: String,
    val lastName: String,
    val gender: String,
    val age: Int,
    val email: String,
)


fun User.toUserDto(): UserDto = UserDto(
    role = this.role,
    firstName = this.firstName,
    secondName = this.secondName,
    lastName = this.lastName,
    gender = this.gender,
    age = this.age,
    email = this.email,
)
