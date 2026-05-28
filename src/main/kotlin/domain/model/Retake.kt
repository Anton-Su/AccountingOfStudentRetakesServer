package domain.model

import java.time.Instant

data class Retake(
    val id: Long,
    val type: String,
    val subjectId: Long,
    val place: String,
    val admission: String?,
    val startAt: Instant,
    val endAt: Instant,
    val lastModified: Instant,
    val teacherIds: List<Long>
)

