package data.repository

import data.databases.CommentsTable
import data.databases.DebtsTable
import data.databases.GradesTable
import data.databases.RetakeEnrollmentsTable
import data.databases.RetakeTeachersTable
import data.databases.RetakesTable
import data.databases.SubjectStudentsTable
import data.databases.SubjectsTable
import domain.model.Comment
import domain.model.Debt
import domain.model.DebtStatus
import domain.model.Grade
import domain.model.Retake
import domain.model.RetakeEnrollment
import domain.model.Subject
import domain.repository.StudentRepository
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

class StudentRepositoryImpl : StudentRepository {
    override suspend fun findDebtsByStudentId(studentId: Long): List<Debt> = transaction {
        DebtsTable.selectAll()
            .filter { it[DebtsTable.studentId].value == studentId }
            .map { it.toDebt() }
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
        val debt = requireOwnedActiveDebtInternal(studentId, debtId)
        require(findRetakeByIdInternal(retakeId) != null) { "Retake with id $retakeId not found" }
        RetakeEnrollmentsTable.deleteWhere { RetakeEnrollmentsTable.debtId eq debtId }
        RetakeEnrollmentsTable.insert {
            it[RetakeEnrollmentsTable.retakeId] = retakeId
            it[RetakeEnrollmentsTable.studentId] = studentId
            it[RetakeEnrollmentsTable.debtId] = debtId
        }
        debt
    }

    override suspend fun cancelRetakeEnrollment(studentId: Long, debtId: Long, retakeId: Long): Debt = transaction {
        val debt = requireOwnedDebtInternal(studentId, debtId)
        val currentRetakeId = findRetakeIdByDebtIdInternal(debtId)
            ?: throw IllegalArgumentException("Debt ${debt.id} is not enrolled to any retake")
        require(currentRetakeId == retakeId) { "Debt ${debt.id} is enrolled to another retake" }
        RetakeEnrollmentsTable.deleteWhere {
            (RetakeEnrollmentsTable.debtId eq debtId) and
                (RetakeEnrollmentsTable.studentId eq studentId) and
                (RetakeEnrollmentsTable.retakeId eq retakeId)
        }
        debt
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
            .map { it.toEnrollment() }
    }

    override suspend fun gradeStudent(retakeId: Long, studentId: Long, score: Int): RetakeEnrollment = transaction {
        require(score in 0..100) { "Score must be between 0 and 100" }
        val enrollmentRow = RetakeEnrollmentsTable.selectAll()
            .firstOrNull { row ->
                row[RetakeEnrollmentsTable.retakeId].value == retakeId &&
                    row[RetakeEnrollmentsTable.studentId].value == studentId
            }
            ?: throw IllegalArgumentException("Enrollment not found for retake $retakeId and student $studentId")
        val updatedEnrollment = enrollmentRow.toEnrollment()
        val now = Instant.now()
        val grade = Grade(
            id = GradesTable.insertAndGetId {
                it[GradesTable.retakeId] = retakeId
                it[GradesTable.studentId] = studentId
                it[GradesTable.score] = score
                it[GradesTable.gradedAt] = now.toEpochMilli()
                it[GradesTable.status] = "accepted"
            }.value,
            retakeId = retakeId,
            studentId = studentId,
            score = score,
            gradedAt = now,
            status = "accepted"
        )
        val debt = DebtsTable.selectAll()
            .first { it[DebtsTable.id].value == updatedEnrollment.debtId }
        val subjectId = debt[DebtsTable.subjectId].value
        SubjectStudentsTable.deleteWhere {
            (SubjectStudentsTable.studentId eq studentId) and
                (SubjectStudentsTable.subjectId eq subjectId)
        }
        SubjectStudentsTable.insert {
            it[SubjectStudentsTable.studentId] = studentId
            it[SubjectStudentsTable.subjectId] = subjectId
            it[SubjectStudentsTable.retakeId] = retakeId
            it[SubjectStudentsTable.score] = score
            it[SubjectStudentsTable.gradedAt] = grade.gradedAt.toEpochMilli()
        }
        updatedEnrollment
    }

    override suspend fun createComment(
        studentId: Long,
        gradeplace: Int,
        gradeteacher: Int,
        gradeoverall: Int,
        comment: String?,
        retakeId: Long,
    ): Comment = transaction {
        Comment(id = CommentsTable.insertAndGetId {
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

    private fun requireOwnedDebtInternal(studentId: Long, debtId: Long): Debt =
        DebtsTable.selectAll()
            .firstOrNull { row -> row[DebtsTable.id].value == debtId && row[DebtsTable.studentId].value == studentId }
            ?.toDebt()
            ?: throw IllegalArgumentException("Debt with id $debtId not found for student $studentId")

    private fun requireOwnedActiveDebtInternal(studentId: Long, debtId: Long): Debt {
        val debt = requireOwnedDebtInternal(studentId, debtId)
        require(debt.status == DebtStatus.ACTIVE) { "Debt ${debt.id} is not active" }
        return debt
    }

    private fun findRetakeByIdInternal(retakeId: Long): Retake? =
        RetakesTable.selectAll()
            .firstOrNull { it[RetakesTable.id].value == retakeId }
            ?.toRetake()

    private fun findRetakeIdByDebtIdInternal(debtId: Long): Long? =
        RetakeEnrollmentsTable.selectAll()
            .firstOrNull { it[RetakeEnrollmentsTable.debtId].value == debtId }
            ?.let { it[RetakeEnrollmentsTable.retakeId].value }

    private fun ResultRow.toDebt(): Debt = Debt(
        id = this[DebtsTable.id].value,
        studentId = this[DebtsTable.studentId].value,
        subjectId = this[DebtsTable.subjectId].value,
        teacherId = this[DebtsTable.teacherId].value,
        createdAt = this[DebtsTable.createdAt],
        status = this[DebtsTable.status]
    )

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

    private fun ResultRow.toEnrollment(): RetakeEnrollment = RetakeEnrollment(
        id = this[RetakeEnrollmentsTable.id].value,
        retakeId = this[RetakeEnrollmentsTable.retakeId].value,
        studentId = this[RetakeEnrollmentsTable.studentId].value,
        debtId = this[RetakeEnrollmentsTable.debtId].value
    )
}

