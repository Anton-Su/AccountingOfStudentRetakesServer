package data.databases

import org.jetbrains.exposed.dao.id.LongIdTable

object SubjectsTable : LongIdTable("subjects") {
    val title = varchar("title", 255).uniqueIndex()
}
