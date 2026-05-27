package data.dto

import domain.model.RetakeEnrollment
import kotlinx.serialization.Serializable

@Serializable
data class EnrollmentDto(
    val id: Long,
    val retakeId: Long,
    val studentId: Long,
    val debtId: Long,
)

fun RetakeEnrollment.toEnrollmentDto(): EnrollmentDto = EnrollmentDto(
    id = this.id,
    retakeId = this.retakeId,
    studentId = this.studentId,
    debtId = this.debtId,
)