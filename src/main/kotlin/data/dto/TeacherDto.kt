package data.dto

import kotlinx.serialization.Serializable
import domain.model.Teacher

@Serializable
data class TeacherDto(
    val userId: Long,
    val disciplines: List<String>
)


fun Teacher.toTeacherDto() = TeacherDto(userId = this.userId, disciplines = this.disciplines)

