package domain.model

import java.time.Instant

data class StudentSubject(
    val id: Long,
    val studentId: Long,
    val subjectId: Long,
    val status: StudentSubjectStatus,
    val score: Long,
    val updated: Instant
)