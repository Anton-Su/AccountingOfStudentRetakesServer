package data.dto

import domain.model.StudentSubject
import kotlinx.serialization.Serializable
import kotlin.String

@Serializable
data class StudentDebt2Dto(
    val id: Long,
    val subjectTitle: String,
)


fun StudentSubject.toDto(): StudentDebt2Dto {
    return StudentDebt2Dto(
        id = this.id,
        subjectTitle = this.subjectTitle,
    )
}