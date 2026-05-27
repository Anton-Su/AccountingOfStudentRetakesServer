package domain.model

data class StudentDebt(
    val debtId: Long,
    val subjectId: Long,
    val subjectTitle: String,
    val teacherId: Long,
    val createdAt: Long,
    val status: DebtStatus,
    val retakeId: Long? = null
)
