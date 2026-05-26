package domain.usecases

import security.JwtConfig
import security.PasswordHasher
import domain.repository.UserRepository

class LoginUseCase(
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher
) {
    suspend fun login(email: String, password: String): String? {
        val user = userRepository.findByEmail(email) ?: return null
        if (!passwordHasher.verify(password, user.passwordHash)) return null
        return JwtConfig.generateToken(email, user.role.name)
    }
}