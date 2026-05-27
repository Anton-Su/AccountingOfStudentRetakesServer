package data.databases

import org.jetbrains.exposed.dao.id.LongIdTable

object GradesTable : LongIdTable("grades") {
    val studentSubjectId = reference("student_subject_id", StudentSubjectsTable)
    val retakeId = reference("retake_id", RetakesTable)
    val score = integer("score")
    val gradedAt = long("graded_at")
}