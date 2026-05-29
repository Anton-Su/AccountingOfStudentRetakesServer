package data.databases

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object RetakeEnrollmentsTable : LongIdTable("retake_enrollments") {
    val studentSubjectId = reference("student_subject_id", StudentSubjectsTable, onDelete = ReferenceOption.CASCADE)
    val retakeId = reference("retake_id", RetakesTable, onDelete = ReferenceOption.CASCADE)
    val enrolledAt = long("enrolled_at")

    init {
        uniqueIndex(studentSubjectId, retakeId)
    }
}