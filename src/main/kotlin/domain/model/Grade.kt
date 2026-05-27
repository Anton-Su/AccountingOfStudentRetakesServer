package domain.model

import java.time.Instant

data class Grade(
    val id: Long,
    val retakeId: Long,
    val studentId: Long,
    val score: Int,
    val gradedAt: Instant,
    val status: String? = null
)
