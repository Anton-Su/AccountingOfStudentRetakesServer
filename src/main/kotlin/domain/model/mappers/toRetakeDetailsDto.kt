package domain.model.mappers

import data.dto.RetakeDetailsDto
import domain.model.RetakeDetails

fun RetakeDetails.toRetakeDetailsDto(): RetakeDetailsDto = RetakeDetailsDto(
    retake = retake.toRetakeDto(),
    enrollments = enrollments.map { it.toEnrollmentDto() }
)