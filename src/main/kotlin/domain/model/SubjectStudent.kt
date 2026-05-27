package domain.model

data class SubjectStudent(
    val id: Long,
    val studentId: Long,
    val subjectId: Long,
    val retakeId: Long,
    val score: Int,
    val gradedAt: Long
)

