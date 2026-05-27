package data.databases

import org.jetbrains.exposed.dao.id.LongIdTable

object CommentsTable : LongIdTable("comments") {
    val studentId = reference("student_id", StudentsTable.userId)
    val gradeplace = integer("gradeplace")
    val gradeteacher = integer("gradeteacher")
    val gradeoverall = integer("gradeoverall")
    val comment = text("comment").nullable()
    val retakeId = reference("retake_id", RetakesTable).nullable()
}

