package data.databases

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object CommentsTable : LongIdTable("comments") {
    val studentId  = reference("student_id", StudentsTable, onDelete = ReferenceOption.CASCADE)
    val gradePlace = integer("grade_place")
    val gradeTeacher = integer("grade_teacher")
    val gradeOverall = integer("grade_overall")
    val comment = text("comment").nullable()
    val retakeId = reference("retake_id", RetakesTable, onDelete = ReferenceOption.CASCADE)
}

