package data.databases

import org.jetbrains.exposed.sql.Table

object TeacherDisciplinesTable : Table("teacher_disciplines") {
    val teacherId = reference("teacher_id", TeachersTable.userId)
    val discipline = varchar("discipline", 128)
    init {
        uniqueIndex(teacherId, discipline)
    }
    override val primaryKey = PrimaryKey(teacherId, discipline)
}

