package domain.model.mappers

import data.dto.UserDto
import domain.model.User

fun User.toUserDto() = UserDto(
    id = this.id,
    role = this.role,
    firstName = this.firstName,
    secondName = this.secondName,
    lastName = this.lastName,
    gender = this.gender,
    age = this.age,
    email = this.email,
)