package data.databases

import org.jetbrains.exposed.dao.id.LongIdTable

object SubjectStudentsTable : LongIdTable("subject_student") {
    val studentId = reference("student_id", StudentsTable.userId)
    val subjectId = reference("subject_id", SubjectsTable)
    val retakeId = reference("retake_id", RetakesTable)
    val score = integer("score")
    val gradedAt = long("graded_at")
    init {
        uniqueIndex(studentId, subjectId)
    }
}