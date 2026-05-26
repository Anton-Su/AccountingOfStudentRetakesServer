package domain.model

import java.time.Instant

data class Retake(
    val id: Long,
    val type: String,
    val admission: String?,
    val startAt: Instant,
    val endAt: Instant,
    val teacherIds: List<Long>
)

