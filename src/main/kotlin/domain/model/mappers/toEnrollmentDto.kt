package domain.model.mappers

import data.dto.EnrollmentDto
import domain.model.RetakeEnrollment

fun RetakeEnrollment.toEnrollmentDto() = EnrollmentDto(
    id = this.id,
    retakeId = this.retakeId,
    studentId = this.studentId,
    studentSubjectId = this.studentSubjectId,
    studentFullName = this.studentFullName,
    groupName = this.groupName
)