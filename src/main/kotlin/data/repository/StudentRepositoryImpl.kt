package data.repository

import data.databases.CommentsTable
import data.databases.DebtsTable
import data.databases.GradesTable
import data.databases.RetakeEnrollmentsTable
import data.databases.RetakeTeachersTable
import data.databases.RetakesTable
import data.databases.StudentSubjectsTable
import data.databases.SubjectsTable
import domain.model.Comment
import domain.model.Debt
import domain.model.DebtStatus
import domain.model.Retake
import domain.model.RetakeEnrollment
import domain.model.StudentSubjectStatus
import domain.model.Subject
import domain.repository.StudentRepository
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant

class StudentRepositoryImpl : StudentRepository {
    override suspend fun findDebtsByStudentId(studentId: Long): List<Debt> = transaction {
        val studentSubjectIds = StudentSubjectsTable.selectAll()
            .filter { it[StudentSubjectsTable.studentId].value == studentId }
            .map { it[StudentSubjectsTable.id].value }
            .toSet()

        DebtsTable.selectAll()
            .filter { it[DebtsTable.studentSubjectId].value in studentSubjectIds }
            .mapNotNull { it.toDebtOrNull() }
    }

    override suspend fun findSubjectById(subjectId: Long): Subject? = transaction {
        SubjectsTable.selectAll()
            .firstOrNull { it[SubjectsTable.id].value == subjectId }
            ?.toSubject()
    }

    override suspend fun findRetakeById(retakeId: Long): Retake? = transaction {
        RetakesTable.selectAll()
            .firstOrNull { it[RetakesTable.id].value == retakeId }
            ?.toRetake()
    }

    override suspend fun findRetakeIdByDebtId(debtId: Long): Long? = transaction {
        findRetakeIdByDebtIdInternal(debtId)
    }

    override suspend fun enrollToRetake(studentId: Long, debtId: Long, retakeId: Long): Debt = transaction {
        val debtRow = requireOwnedActiveDebtRowInternal(studentId, debtId)
        val studentSubjectId = debtRow[DebtsTable.studentSubjectId].value
        require(findRetakeByIdInternal(retakeId) != null) { "Retake with id $retakeId not found" }
        RetakeEnrollmentsTable.deleteWhere { RetakeEnrollmentsTable.studentSubjectId eq studentSubjectId }
        RetakeEnrollmentsTable.insert {
            it[RetakeEnrollmentsTable.retakeId] = retakeId
            it[RetakeEnrollmentsTable.studentSubjectId] = studentSubjectId
            it[RetakeEnrollmentsTable.enrolledAt] = Instant.now().toEpochMilli()
        }
        debtRow.toDebtOrNull() ?: throw IllegalArgumentException("Debt with id $debtId not found")
    }

    override suspend fun cancelRetakeEnrollment(studentId: Long, debtId: Long, retakeId: Long): Debt = transaction {
        val debtRow = requireOwnedDebtRowInternal(studentId, debtId)
        val studentSubjectId = debtRow[DebtsTable.studentSubjectId].value
        val currentRetakeId = findRetakeIdByDebtIdInternal(debtId)
            ?: throw IllegalArgumentException("Debt $debtId is not enrolled to any retake")
        require(currentRetakeId == retakeId) { "Debt $debtId is enrolled to another retake" }

        RetakeEnrollmentsTable.deleteWhere {
            (RetakeEnrollmentsTable.studentSubjectId eq studentSubjectId) and
                (RetakeEnrollmentsTable.retakeId eq retakeId)
        }
        debtRow.toDebtOrNull() ?: throw IllegalArgumentException("Debt with id $debtId not found")
    }

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
                val ssRow = findStudentSubjectRow(row[RetakeEnrollmentsTable.studentSubjectId].value)
                    ?: return@firstOrNull false
                ssRow[StudentSubjectsTable.studentId].value == studentId
            }
            ?: throw IllegalArgumentException("Enrollment not found for retake $retakeId and student $studentId")
        val studentSubjectId = enrollmentRow[RetakeEnrollmentsTable.studentSubjectId].value
        val now = Instant.now().toEpochMilli()
        GradesTable.insert {
            it[GradesTable.retakeId] = retakeId
            it[GradesTable.studentSubjectId] = studentSubjectId
            it[GradesTable.score] = score
            it[GradesTable.gradedAt] = now
        }
        val newStatus = if (score != 2) StudentSubjectStatus.PASSED else StudentSubjectStatus.DEBT
        StudentSubjectsTable.update({ StudentSubjectsTable.id eq studentSubjectId }) {
            it[StudentSubjectsTable.status] = newStatus
            it[StudentSubjectsTable.score] = score
            it[StudentSubjectsTable.updatedAt] = now
        }

        val debtRow = DebtsTable.selectAll()
            .firstOrNull { it[DebtsTable.studentSubjectId].value == studentSubjectId }
            ?: throw IllegalArgumentException("Debt not found for student_subject $studentSubjectId")

        RetakeEnrollment(
            id = enrollmentRow[RetakeEnrollmentsTable.id].value,
            retakeId = retakeId,
            studentId = studentId,
            debtId = debtRow[DebtsTable.id].value
        )
    }

    override suspend fun createComment(
        studentId: Long,
        gradeplace: Int,
        gradeteacher: Int,
        gradeoverall: Int,
        comment: String?,
        retakeId: Long
    ): Comment = transaction {
        Comment(
            id = CommentsTable.insertAndGetId {
                it[CommentsTable.studentId] = studentId
                it[CommentsTable.gradeplace] = gradeplace
                it[CommentsTable.gradeteacher] = gradeteacher
                it[CommentsTable.gradeoverall] = gradeoverall
                it[CommentsTable.comment] = comment
                it[CommentsTable.retakeId] = retakeId
            }.value,
            studentId = studentId,
            gradeplace = gradeplace,
            gradeteacher = gradeteacher,
            gradeoverall = gradeoverall,
            comment = comment,
            retakeId = retakeId
        )
    }

    private fun requireOwnedDebtRowInternal(studentId: Long, debtId: Long): ResultRow {
        val debtRow = DebtsTable.selectAll()
            .firstOrNull { it[DebtsTable.id].value == debtId }
            ?: throw IllegalArgumentException("Debt with id $debtId not found")

        val studentSubjectId = debtRow[DebtsTable.studentSubjectId].value
        val ssRow = findStudentSubjectRow(studentSubjectId)
            ?: throw IllegalArgumentException("Student subject $studentSubjectId not found")

        if (ssRow[StudentSubjectsTable.studentId].value != studentId) {
            throw IllegalArgumentException("Debt with id $debtId not found for student $studentId")
        }
        return debtRow
    }

    private fun requireOwnedActiveDebtRowInternal(studentId: Long, debtId: Long): ResultRow {
        val debtRow = requireOwnedDebtRowInternal(studentId, debtId)
        require(debtRow[DebtsTable.status] == DebtStatus.ACTIVE) { "Debt $debtId is not active" }
        return debtRow
    }

    private fun findRetakeByIdInternal(retakeId: Long): Retake? =
        RetakesTable.selectAll()
            .firstOrNull { it[RetakesTable.id].value == retakeId }
            ?.toRetake()

    private fun findRetakeIdByDebtIdInternal(debtId: Long): Long? {
        val debtRow = DebtsTable.selectAll().firstOrNull { it[DebtsTable.id].value == debtId } ?: return null
        val studentSubjectId = debtRow[DebtsTable.studentSubjectId].value
        return RetakeEnrollmentsTable.selectAll()
            .firstOrNull { it[RetakeEnrollmentsTable.studentSubjectId].value == studentSubjectId }
            ?.let { it[RetakeEnrollmentsTable.retakeId].value }
    }

    private fun findStudentSubjectRow(studentSubjectId: Long): ResultRow? =
        StudentSubjectsTable.selectAll()
            .firstOrNull { it[StudentSubjectsTable.id].value == studentSubjectId }

    private fun ResultRow.toDebtOrNull(): Debt? {
        val studentSubjectId = this[DebtsTable.studentSubjectId].value
        val ssRow = findStudentSubjectRow(studentSubjectId) ?: return null
        return Debt(
            id = this[DebtsTable.id].value,
            studentId = ssRow[StudentSubjectsTable.studentId].value,
            subjectId = ssRow[StudentSubjectsTable.subjectId].value,
            teacherId = this[DebtsTable.teacherId].value,
            createdAt = this[DebtsTable.createdAt],
            status = this[DebtsTable.status]
        )
    }

    private fun ResultRow.toSubject(): Subject = Subject(
        id = this[SubjectsTable.id].value,
        title = this[SubjectsTable.title]
    )

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
            startAt = Instant.ofEpochMilli(this[RetakesTable.startAt]),
            endAt = Instant.ofEpochMilli(this[RetakesTable.endAt]),
            lastModified = Instant.ofEpochMilli(this[RetakesTable.lastModified]),
            teacherIds = teacherIds
        )
    }

    private fun ResultRow.toEnrollmentOrNull(): RetakeEnrollment? {
        val studentSubjectId = this[RetakeEnrollmentsTable.studentSubjectId].value
        val ssRow = findStudentSubjectRow(studentSubjectId) ?: return null
        val debtRow = DebtsTable.selectAll().firstOrNull { it[DebtsTable.studentSubjectId].value == studentSubjectId } ?: return null

        return RetakeEnrollment(
            id = this[RetakeEnrollmentsTable.id].value,
            retakeId = this[RetakeEnrollmentsTable.retakeId].value,
            studentId = ssRow[StudentSubjectsTable.studentId].value,
            debtId = debtRow[DebtsTable.id].value
        )
    }
}
