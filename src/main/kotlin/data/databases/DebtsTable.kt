package data.databases

import domain.model.DebtStatus
import org.jetbrains.exposed.dao.id.LongIdTable

object DebtsTable : LongIdTable("debts") {
    val studentId = reference("student_id", StudentsTable.userId)
    val subjectId = reference("subject_id", SubjectsTable)
    val teacherId = reference("teacher_id", TeachersTable.userId)
    val createdAt = long("created_at")
    val place = varchar("place", 255)
    val status = enumerationByName("status", 16, DebtStatus::class)
}