package domain.model

data class Comment(
    val id: Long,
    val studentId: Long,
    val studentFullName: String,
    val groupName: String,
    val subjectTitle: String,
    val gradePlace: Int,
    val gradeTeacher: Int,
    val gradeOverall: Int,
    val comment: String?,
    val retakeId: Long,
    val retakeStartAt: String,
    val retakeEndAt: String,
)

