package data.mappers

import data.databases.CommentsTable
import data.databases.RetakesTable
import data.databases.StudentsTable
import data.databases.SubjectsTable
import data.databases.UsersTable
import domain.model.Comment
import org.jetbrains.exposed.sql.ResultRow
import java.time.Instant

fun ResultRow.toComment() = Comment(
    id = this[CommentsTable.id].value,
    studentId = this[CommentsTable.studentId].value,
    studentFullName = "${this[UsersTable.secondName]} ${this[UsersTable.firstName]} ${this[UsersTable.lastName]}",
    groupName = this[StudentsTable.groupName],
    gradeplace = this[CommentsTable.gradeplace],
    gradeteacher = this[CommentsTable.gradeteacher],
    gradeoverall = this[CommentsTable.gradeoverall],
    comment = this[CommentsTable.comment],
    retakeId = this[CommentsTable.retakeId].value,
    retakeStartAt = Instant.ofEpochMilli(this[RetakesTable.startAt]).toString(),
    retakeEndAt = Instant.ofEpochMilli(this[RetakesTable.endAt]).toString(),
    subjectTitle = this[SubjectsTable.title],
)