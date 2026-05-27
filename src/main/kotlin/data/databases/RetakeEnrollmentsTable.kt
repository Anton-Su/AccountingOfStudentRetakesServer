package data.databases

import org.jetbrains.exposed.dao.id.LongIdTable

object RetakeEnrollmentsTable : LongIdTable("retake_enrollments") {
    val retakeId = reference("retake_id", RetakesTable)
    val studentId = reference("student_id", StudentsTable.userId)
    val debtId = reference("debt_id", DebtsTable).uniqueIndex()
    init {
        uniqueIndex(retakeId, studentId, debtId)
    }
}