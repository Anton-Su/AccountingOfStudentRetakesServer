package domain.model

data class RetakeDetails(
    val retake: Retake,
    val enrollments: List<RetakeEnrollment>
)