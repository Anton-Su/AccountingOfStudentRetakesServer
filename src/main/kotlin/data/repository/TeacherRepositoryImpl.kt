package data.repository

import data.databases.CommentsTable
//import data.databases.DebtsTable
import data.databases.GradesTable
import data.databases.RetakeEnrollmentsTable
import data.databases.RetakeTeachersTable
import data.databases.RetakesTable
import data.databases.StudentSubjectsTable
import domain.model.Retake
import domain.model.RetakeEnrollment
import domain.model.StudentSubjectStatus
import domain.repository.TeacherRepository
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
        RetakeEnrollmentsTable.selectAll()
            .filter { it[RetakeEnrollmentsTable.retakeId].value == retakeId }
            .mapNotNull { it.toEnrollmentOrNull() }
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
            studentSubjectId = studentSubjectId
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

    private fun ResultRow.toEnrollmentOrNull(): RetakeEnrollment? {
        val studentSubjectId = this[RetakeEnrollmentsTable.studentSubjectId].value
        val ssRow = findStudentSubjectRow(studentSubjectId) ?: return null
        return RetakeEnrollment(
            id = this[RetakeEnrollmentsTable.id].value,
            retakeId = this[RetakeEnrollmentsTable.retakeId].value,
            studentId = ssRow[StudentSubjectsTable.studentId].value,
            studentSubjectId = studentSubjectId
        )
    }
}