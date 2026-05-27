package data.dto

import domain.model.Subject
import kotlinx.serialization.Serializable

@Serializable
data class SubjectDto(
    val title: String
)

fun Subject.toSubjectDto() = SubjectDto(title = this.title)
