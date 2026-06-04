package domain.model.mappers

import data.dto.CommentDto
import domain.model.Comment

fun Comment.toCommentDto() = CommentDto(
    id = id,
    studentId = studentId,
    gradeplace = gradePlace,
    gradeteacher = gradeTeacher,
    gradeoverall = gradeOverall,
    comment = comment,
    retakeId = retakeId,
    retakeStartAt = retakeStartAt,
    retakeEndAt = retakeEndAt,
    studentFullName = studentFullName,
    subjectTitle = subjectTitle,
    groupName = groupName
)