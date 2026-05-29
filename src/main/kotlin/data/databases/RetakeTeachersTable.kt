package data.databases

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object RetakeTeachersTable : Table("retake_teachers") {
    val retakeId  = reference("retake_id", RetakesTable, onDelete = ReferenceOption.CASCADE)
    val teacherId = reference("teacher_id", TeachersTable, onDelete = ReferenceOption.CASCADE)
    init { uniqueIndex(retakeId, teacherId) }
    override val primaryKey = PrimaryKey(retakeId, teacherId)
}