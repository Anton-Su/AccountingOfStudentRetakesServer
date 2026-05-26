package domain.usecases

import Security.JwtConfig
import Security.PasswordHasher
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