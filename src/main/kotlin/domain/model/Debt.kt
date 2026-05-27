package domain.model

data class Debt(
    val id: Long,
    val studentId: Long,
    val subjectId: Long,
    val teacherId: Long,
    val createdAt: Long,
    val status: DebtStatus
)



