package domain.usecases

import domain.repository.UserRepository
import security.JwtConfig
import security.PasswordHasher

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