package domain.model

import java.time.Instant

data class StudentSubject(
    val id: Long,
    val studentId: Long,
    val subjectId: Long,
    val subjectTitle: String,
    val status: StudentSubjectStatus,
    val score: Int? = null,
    val updatedAt: Instant
)