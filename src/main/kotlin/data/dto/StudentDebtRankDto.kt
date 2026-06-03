package data.dto

@kotlinx.serialization.Serializable
data class StudentDebtRankDto(
    val studentId: Long,
    val debtsCount: Int,
    val place: Int,
    val totalStudents: Int,
    val topPercent: Int
)