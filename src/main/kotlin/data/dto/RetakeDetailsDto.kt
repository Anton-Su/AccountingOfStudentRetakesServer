package data.dto

import kotlinx.serialization.Serializable

@Serializable
data class RetakeDetailsDto(
    val retake: RetakeDto,
    val enrollments: List<EnrollmentDto>
)