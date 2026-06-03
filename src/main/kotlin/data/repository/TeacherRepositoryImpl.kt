package data.repository

import data.databases.GradesTable
import data.databases.RetakeEnrollmentsTable
import data.databases.RetakeTeachersTable
import data.databases.RetakesTable
import data.databases.StudentSubjectsTable
import data.databases.StudentsTable
import data.databases.StudentsTable.groupName
import data.databases.UsersTable
import data.mappers.toRetake
import domain.model.Retake
import domain.model.RetakeEnrollment
import domain.model.StudentSubjectStatus
import domain.repository.TeacherRepository
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant

class TeacherRepositoryImpl : TeacherRepository {
    override suspend fun findRetakesByTeacherId(teacherId: Long): List<Retake> = transaction {
        val retakeIds = RetakeTeachersTable.selectAll()
            .where { RetakeTeachersTable.teacherId eq teacherId }
            .map { it[RetakeTeachersTable.retakeId].value }
            .distinct()
        val now = Instant.now().toEpochMilli()
        val allTeacherIds = RetakeTeachersTable.selectAll()
            .where { RetakeTeachersTable.retakeId inList retakeIds }
            .groupBy(
                { it[RetakeTeachersTable.retakeId].value },
                { it[RetakeTeachersTable.teacherId].value }
            )
        RetakesTable.selectAll()
            .where { (RetakesTable.id inList retakeIds) and (RetakesTable.endAt greaterEq now) }
            .map { row ->
                val retakeId = row[RetakesTable.id].value
                row.toRetake(allTeacherIds[retakeId] ?: emptyList())
            }
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
        val enrollmentRow = RetakeEnrollmentsTable
            .join(StudentSubjectsTable, JoinType.INNER, RetakeEnrollmentsTable.studentSubjectId, StudentSubjectsTable.id)
            .join(UsersTable, JoinType.INNER, StudentSubjectsTable.studentId, UsersTable.id)
            .join(StudentsTable, JoinType.INNER, StudentSubjectsTable.studentId, StudentsTable.id)
            .selectAll()
            .where { (RetakeEnrollmentsTable.retakeId eq retakeId) and (StudentSubjectsTable.studentId eq studentId) }
            .firstOrNull() ?: throw IllegalArgumentException("Enrollment not found for retake $retakeId and student $studentId")
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
            retakeId = retakeId, studentId = studentId,
            studentSubjectId = studentSubjectId,
            studentFullName = "${enrollmentRow[UsersTable.secondName]} ${enrollmentRow[UsersTable.firstName]} ${enrollmentRow[UsersTable.lastName]}",
            groupName = enrollmentRow[StudentsTable.groupName],
        )
    }
}