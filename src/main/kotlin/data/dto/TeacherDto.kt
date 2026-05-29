package data.dto

import kotlinx.serialization.Serializable
import domain.model.Teacher

@Serializable
data class TeacherDto(
    val userId: Long,
    val fullName: String,
    val disciplines: List<String>
)


fun Teacher.toTeacherDto() = TeacherDto(
    userId = this.userId,
    fullName = this.fullName,
    disciplines = this.disciplines
)