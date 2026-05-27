package data.databases

import org.jetbrains.exposed.sql.Table

object StudentsTable : Table("students") {
    val userId = reference("user_id", UsersTable).uniqueIndex()
    val groupName = varchar("group_name", 64)
    override val primaryKey = PrimaryKey(userId)
}