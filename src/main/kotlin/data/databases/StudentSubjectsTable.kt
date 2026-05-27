package data.databases

import domain.model.StudentSubjectStatus
import org.jetbrains.exposed.dao.id.LongIdTable

object StudentSubjectsTable : LongIdTable("student_subjects") {
    val studentId = reference("student_id", StudentsTable.userId)
    val subjectId = reference("subject_id", SubjectsTable)
    val status = enumerationByName("status", 32, StudentSubjectStatus::class)
    val score = integer("score").nullable()
    val updatedAt = long("updated_at")
    init {
        uniqueIndex(studentId, subjectId)
    }
}

