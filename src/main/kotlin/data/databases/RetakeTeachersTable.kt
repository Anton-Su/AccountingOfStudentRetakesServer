package data.databases

import org.jetbrains.exposed.sql.Table

object RetakeTeachersTable : Table("retake_teachers") {
    val retakeId  = reference("retake_id", RetakesTable)
    val teacherId = long("teacher_id")
    init {
        uniqueIndex(retakeId, teacherId)
    }
    override val primaryKey = PrimaryKey(retakeId, teacherId)
}