package data.databases

import domain.model.UserRole
import org.jetbrains.exposed.dao.id.LongIdTable

object UsersTable : LongIdTable("users") {
    val role = enumerationByName("role", 16, UserRole::class)
    val firstName = varchar("first_name", 64)
    val secondName = varchar("second_name", 64)
    val lastName = varchar("last_name", 64)
    val gender = varchar("gender", 32)
    val age = integer("age")
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
}