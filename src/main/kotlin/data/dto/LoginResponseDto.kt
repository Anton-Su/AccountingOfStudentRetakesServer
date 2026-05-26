package data.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponseDto(val token: String)
