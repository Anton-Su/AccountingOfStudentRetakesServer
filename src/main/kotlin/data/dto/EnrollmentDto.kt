package data.dto

import domain.model.RetakeEnrollment
import kotlinx.serialization.Serializable

@Serializable
data class EnrollmentDto(
    val id: Long,
    val retakeId: Long,
    val studentId: Long,
    val studentSubjectId: Long,
    val studentFullName: String,
    val groupName: String,
)

fun RetakeEnrollment.toEnrollmentDto(): EnrollmentDto = EnrollmentDto(
    id = this.id,
    retakeId = this.retakeId,
    studentId = this.studentId,
    studentSubjectId = this.studentSubjectId,
    studentFullName = this.studentFullName,
    groupName = this.groupName
)