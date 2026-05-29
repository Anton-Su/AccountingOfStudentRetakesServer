package data.databases

import domain.model.StudentSubjectStatus
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object StudentSubjectsTable : LongIdTable("student_subjects") {
    val studentId = reference("student_id", StudentsTable, onDelete = ReferenceOption.CASCADE)
    val subjectId = reference("subject_id", SubjectsTable, onDelete = ReferenceOption.CASCADE)
    val status = enumerationByName("status", 32, StudentSubjectStatus::class)
    val score = integer("score").nullable()
    val updatedAt = long("updated_at")
    init { uniqueIndex(studentId, subjectId) }
}
