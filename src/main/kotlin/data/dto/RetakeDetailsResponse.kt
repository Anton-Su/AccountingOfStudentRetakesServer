package data.dto

import kotlinx.serialization.Serializable

@Serializable
data class RetakeDetailsResponse(
    val retake: RetakeDetailDto,
    val enrollments: List<EnrollmentDto>
)