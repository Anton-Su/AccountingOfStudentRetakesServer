package data.databases

import domain.model.DebtStatus
import org.jetbrains.exposed.dao.id.LongIdTable

//object DebtsTable : LongIdTable("debts") {
//    val studentSubjectId = reference("student_subject_id", StudentSubjectsTable).uniqueIndex()
//    val teacherId = reference("teacher_id", TeachersTable.userId)
//    val createdAt = long("created_at")
//    val status = enumerationByName("status", 16, DebtStatus::class)
//}