package data.dto

import domain.model.StudentSubject
import kotlinx.serialization.Serializable

@Serializable
data class StudentSubjectDto(
    val id: Long,
    val subjectId: Long,
    val status: String,
    val score: Int? = null,
    val updatedAt: Long
)


fun StudentSubject.toDto(): StudentSubjectDto {
    return StudentSubjectDto(
        id = this.id,
        subjectId = this.subjectId,
        status = this.status.name,
        score = this.score,
        updatedAt = this.updatedAt.toEpochMilli()
    )
}