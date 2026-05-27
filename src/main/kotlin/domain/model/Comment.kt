package domain.model

data class Comment(
    val id: Long,
    val studentId: Long,
    val gradeplace: Int,
    val gradeteacher: Int,
    val gradeoverall: Int,
    val comment: String?,
    val retakeId: Long?
)

