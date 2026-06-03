package data.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginDto(
    val token: String
)
