package data.databases

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Table

object TeachersTable : Table("teachers") {
    val userId = reference("user_id", UsersTable).uniqueIndex()
    override val primaryKey = PrimaryKey(userId)
}