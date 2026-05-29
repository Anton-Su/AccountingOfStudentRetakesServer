package data.repository

//import data.databases.DebtsTable
import data.databases.GradesTable
import data.databases.RetakeEnrollmentsTable
import data.databases.RetakeTeachersTable
import data.databases.RetakesTable
import data.databases.StudentSubjectsTable
import data.databases.StudentsTable
import data.databases.StudentsTable.groupName
import data.databases.UsersTable
import domain.model.Retake
import domain.model.RetakeEnrollment
import domain.model.StudentSubjectStatus
import domain.repository.TeacherRepository
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant


class TeacherRepositoryImpl : TeacherRepository {

    override suspend fun findRetakesByTeacherId(teacherId: Long): List<Retake> = transaction {
        val retakeIds = RetakeTeachersTable.selectAll()
            .filter { it[RetakeTeachersTable.teacherId].value == teacherId }
            .map { it[RetakeTeachersTable.retakeId].value }
            .distinct()
        retakeIds.mapNotNull { findRetakeByIdInternal(it) }
    }

    override suspend fun findEnrollmentsByRetakeId(retakeId: Long): List<RetakeEnrollment> = transaction {
        RetakeEnrollmentsTable
            .join(StudentSubjectsTable, JoinType.INNER, RetakeEnrollmentsTable.studentSubjectId, StudentSubjectsTable.id)
            .join(UsersTable, JoinType.INNER, StudentSubjectsTable.studentId, UsersTable.id)
            .join(StudentsTable, JoinType.INNER, StudentSubjectsTable.studentId, StudentsTable.id)
            .selectAll()
            .where { RetakeEnrollmentsTable.retakeId eq retakeId }
            .map {
                RetakeEnrollment(
                    id = it[RetakeEnrollmentsTable.id].value,
                    retakeId = it[RetakeEnrollmentsTable.retakeId].value,
                    studentId = it[StudentSubjectsTable.studentId].value,
                    studentSubjectId = it[RetakeEnrollmentsTable.studentSubjectId].value,
                    studentFullName = "${it[UsersTable.secondName]} ${it[UsersTable.firstName]} ${it[UsersTable.lastName]}",
                    groupName = it[StudentsTable.groupName],
                )
            }
    }

    override suspend fun gradeStudent(retakeId: Long, studentId: Long, score: Int): RetakeEnrollment = transaction {
        val enrollmentRow = RetakeEnrollmentsTable.selectAll()
            .firstOrNull { row ->
                if (row[RetakeEnrollmentsTable.retakeId].value != retakeId) return@firstOrNull false
                val ssRow = findStudentSubjectRow(
                    row[RetakeEnrollmentsTable.studentSubjectId].value
                ) ?: return@firstOrNull false
                ssRow[StudentSubjectsTable.studentId].value == studentId
            }
            ?: throw IllegalArgumentException(
                "Enrollment not found for retake $retakeId and student $studentId"
            )
        val studentSubjectId = enrollmentRow[RetakeEnrollmentsTable.studentSubjectId].value
        val now = Instant.now().toEpochMilli()
        GradesTable.insert {
            it[GradesTable.retakeId] = retakeId
            it[GradesTable.studentSubjectId] = studentSubjectId
            it[GradesTable.score] = score
            it[GradesTable.gradedAt] = now
        }
        val newStatus = if (score == 2) StudentSubjectStatus.DEBT else StudentSubjectStatus.PASSED
        StudentSubjectsTable.update({ StudentSubjectsTable.id eq studentSubjectId }) {
            it[status] = newStatus
            it[StudentSubjectsTable.score] = score
            it[updatedAt] = now
        }
        RetakeEnrollment(
            id = enrollmentRow[RetakeEnrollmentsTable.id].value,
            retakeId = retakeId,
            studentId = studentId,
            studentSubjectId = studentSubjectId,
            studentFullName = "Test",
            groupName = "Test",
        )
    }

    private fun findRetakeByIdInternal(retakeId: Long): Retake? =
        RetakesTable.selectAll()
            .firstOrNull { it[RetakesTable.id].value == retakeId }
            ?.toRetake()

    private fun findStudentSubjectRow(studentSubjectId: Long): ResultRow? =
        StudentSubjectsTable.selectAll()
            .firstOrNull { it[StudentSubjectsTable.id].value == studentSubjectId }

    private fun ResultRow.toRetake(): Retake {
        val retakeId = this[RetakesTable.id].value
        val teacherIds = RetakeTeachersTable.selectAll()
            .filter { it[RetakeTeachersTable.retakeId].value == retakeId }
            .map { it[RetakeTeachersTable.teacherId].value }
        return Retake(
            id = retakeId,
            type = this[RetakesTable.type],
            place = this[RetakesTable.place],
            admission = this[RetakesTable.admission],
            subjectId = this[RetakesTable.subjectId].value,
            startAt = Instant.ofEpochMilli(this[RetakesTable.startAt]),
            endAt = Instant.ofEpochMilli(this[RetakesTable.endAt]),
            lastModified = Instant.ofEpochMilli(this[RetakesTable.lastModified]),
            teacherIds = teacherIds
        )
    }

}