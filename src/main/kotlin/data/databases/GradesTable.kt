package data.databases

import org.jetbrains.exposed.dao.id.LongIdTable

object GradesTable : LongIdTable("grades") {
    val retakeId = reference("retake_id", RetakesTable)
    val studentId = reference("student_id", StudentsTable.userId)
    val score = integer("score")
    val gradedAt = long("graded_at")
    val status = varchar("status", 64).nullable()
}