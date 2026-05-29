package domain.model

data class Teacher(
    val userId: Long,
    val fullName: String,
    val disciplines: List<String>
)