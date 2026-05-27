package data.databases

import org.jetbrains.exposed.dao.id.LongIdTable

object RetakeEnrollmentsTable : LongIdTable("retake_enrollments") {
    val studentSubjectId = reference("student_subject_id", StudentSubjectsTable)
    val retakeId = reference("retake_id", RetakesTable)
    val enrolledAt = long("enrolled_at")

    init {
        uniqueIndex(studentSubjectId, retakeId)
    }
}