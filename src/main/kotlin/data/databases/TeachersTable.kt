package data.databases

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object TeachersTable : IdTable<Long>("teachers") {
    override val id: Column<EntityID<Long>> = reference("user_id", UsersTable)
    override val primaryKey = PrimaryKey(id)
}