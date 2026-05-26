package data.repository

import security.PasswordHasher
import domain.model.User
import domain.model.UserRole
import domain.repository.UserRepository

class UserRepositoryImpl : UserRepository {
    private val usersByEmail = mutableMapOf(
        "admin@example.com" to User(
            id = 1,
            role = UserRole.ADMIN,
            firstName = "Admin",
            secondName = "System",
            lastName = "User",
            gender = "unknown",
            age = 30,
            email = "admin@example.com",
            passwordHash = PasswordHasher.hash("Admin123!")
        ),
        "teacher@example.com" to User(
            id = 2,
            role = UserRole.TEACHER,
            firstName = "Teacher",
            secondName = "Default",
            lastName = "User",
            gender = "unknown",
            age = 35,
            email = "teacher@example.com",
            passwordHash = PasswordHasher.hash("Teacher123!")
        ),
        "student@example.com" to User(
            id = 3,
            role = UserRole.STUDENT,
            firstName = "Student",
            secondName = "Default",
            lastName = "User",
            gender = "unknown",
            age = 20,
            email = "student@example.com",
            passwordHash = PasswordHasher.hash("Student123!")
        )
    )

    override suspend fun findByEmail(email: String): User? {
        return usersByEmail[email.trim().lowercase()]
    }

    override suspend fun findById(id: Int): User? {
        return usersByEmail.values.firstOrNull { it.id == id.toLong() }
    }
}