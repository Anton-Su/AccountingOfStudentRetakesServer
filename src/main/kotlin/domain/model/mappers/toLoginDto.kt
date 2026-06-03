package domain.model.mappers

import data.dto.LoginDto
import domain.model.Login


fun Login.toLoginDto() = LoginDto(
    token = this.token
)