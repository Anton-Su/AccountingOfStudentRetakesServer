package data.databases

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object GradesTable : LongIdTable("grades") {
    val studentSubjectId = reference("student_subject_id", StudentSubjectsTable, onDelete = ReferenceOption.CASCADE)
    val retakeId = reference("retake_id", RetakesTable, onDelete = ReferenceOption.CASCADE)
    val score = integer("score")
    val gradedAt = long("graded_at")
}