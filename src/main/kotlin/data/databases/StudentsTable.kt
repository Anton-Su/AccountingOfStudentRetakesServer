package data.databases

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object StudentsTable : IdTable<Long>("students") {
    override val id: Column<EntityID<Long>> = reference("user_id", UsersTable, onDelete = ReferenceOption.CASCADE)
    val groupName = varchar("group_name", 64)
    override val primaryKey = PrimaryKey(id)
}