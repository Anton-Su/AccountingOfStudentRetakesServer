package domain.model

data class RetakeEnrollment(
    val id: Long,
    val retakeId: Long,
    val studentId: Long,
    val debtId: Long,
    val score: Int? = null
)
