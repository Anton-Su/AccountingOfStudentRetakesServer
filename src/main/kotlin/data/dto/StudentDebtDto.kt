package data.dto

import domain.model.StudentDebt
import kotlinx.serialization.Serializable

@Serializable
data class StudentDebtDto(
    val id: Long,
    val subjectTitle: String,
)


fun StudentDebt.toDto(): StudentDebtDto {
    return StudentDebtDto(
        id = this.id,
        subjectTitle = this.subjectTitle,
    )
}
