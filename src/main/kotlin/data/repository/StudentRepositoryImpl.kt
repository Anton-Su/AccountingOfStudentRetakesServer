package data.repository

import data.databases.CommentsTable
//import data.databases.DebtsTable
import data.databases.GradesTable
import data.databases.RetakeEnrollmentsTable
import data.databases.RetakeTeachersTable
import data.databases.RetakesTable
import data.databases.StudentSubjectsTable
import data.databases.SubjectsTable
import domain.model.Comment
//import domain.model.Debt
//import domain.model.DebtStatus
import domain.model.Retake
import domain.model.RetakeEnrollment
import domain.model.StudentDebt
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
    override suspend fun findDebtsByStudentId(studentId: Long) = transaction {
        (StudentSubjectsTable innerJoin SubjectsTable)
            .selectAll()
            .where {
                (StudentSubjectsTable.studentId eq studentId) and
                        (StudentSubjectsTable.status eq StudentSubjectStatus.DEBT)
            }
            .map { row ->
                StudentDebt(
                    id = row[StudentSubjectsTable.id].value,
                    subjectId = row[StudentSubjectsTable.subjectId].value,
                    subjectTitle = row[SubjectsTable.title]
                )
            }
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

    override suspend fun enrollToRetake(studentId: Long, subjectId: Long, retakeId: Long): Boolean = transaction {
        val studentSubject = StudentSubjectsTable
            .selectAll()
            .firstOrNull {
                it[StudentSubjectsTable.studentId].value == studentId &&
                        it[StudentSubjectsTable.subjectId].value == subjectId
            } ?: throw IllegalArgumentException("Student subject not found")
        val studentSubjectId = studentSubject[StudentSubjectsTable.id].value
        require(findRetakeByIdInternal(retakeId) != null) {
            "Retake with id $retakeId not found"
        }
        RetakeEnrollmentsTable.deleteWhere {
            RetakeEnrollmentsTable.studentSubjectId eq studentSubjectId
        }
        RetakeEnrollmentsTable.insert {
            it[RetakeEnrollmentsTable.retakeId] = retakeId
            it[RetakeEnrollmentsTable.studentSubjectId] = studentSubjectId
            it[RetakeEnrollmentsTable.enrolledAt] = Instant.now().toEpochMilli()
        }
        true
    }

    override suspend fun cancelRetakeEnrollment(studentId: Long, subjectId: Long, retakeId: Long): Boolean = transaction {
        val studentSubject = StudentSubjectsTable
            .selectAll()
            .firstOrNull {
                it[StudentSubjectsTable.studentId].value == studentId &&
                        it[StudentSubjectsTable.subjectId].value == subjectId
            } ?: throw IllegalArgumentException("Student subject not found")
        val studentSubjectId = studentSubject[StudentSubjectsTable.id].value
        val exists = RetakeEnrollmentsTable.selectAll().any {
            it[RetakeEnrollmentsTable.studentSubjectId].value == studentSubjectId &&
                    it[RetakeEnrollmentsTable.retakeId].value == retakeId
        }
        require(exists) {
            "Student is not enrolled to this retake"
        }
        RetakeEnrollmentsTable.deleteWhere {
            (RetakeEnrollmentsTable.studentSubjectId eq studentSubjectId) and
                    (RetakeEnrollmentsTable.retakeId eq retakeId)
        }
        true
    }

    override suspend fun findRetakesByTeacherId(teacherId: Long): List<Retake> = transaction {
        val retakeIds = RetakeTeachersTable.selectAll()
            .filter { it[RetakeTeachersTable.teacherId] == teacherId }
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

    override suspend fun getStudentsDebtCounts(): List<Pair<Long, Int>> = transaction {
        StudentSubjectsTable
            .selectAll()
            .where {
                StudentSubjectsTable.status eq StudentSubjectStatus.DEBT
            }
            .groupBy { row ->
                row[StudentSubjectsTable.studentId].value
            }
            .map { (studentId, rows) ->
                studentId to rows.size
            }
    }

    private fun findRetakeByIdInternal(retakeId: Long): Retake? =
        RetakesTable.selectAll()
            .firstOrNull { it[RetakesTable.id].value == retakeId }
            ?.toRetake()

    private fun findStudentSubjectRow(studentSubjectId: Long): ResultRow? =
        StudentSubjectsTable.selectAll()
            .firstOrNull { it[StudentSubjectsTable.id].value == studentSubjectId }


    private fun ResultRow.toSubject(): Subject = Subject(
        id = this[SubjectsTable.id].value,
        title = this[SubjectsTable.title]
    )

    private fun ResultRow.toRetake(): Retake {
        val retakeId = this[RetakesTable.id].value
        val teacherIds = RetakeTeachersTable.selectAll()
            .filter { it[RetakeTeachersTable.retakeId].value == retakeId }
            .map { it[RetakeTeachersTable.teacherId]}
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
        return RetakeEnrollment(
            id = this[RetakeEnrollmentsTable.id].value,
            retakeId = this[RetakeEnrollmentsTable.retakeId].value,
            studentId = ssRow[StudentSubjectsTable.studentId].value,
            studentSubjectId = studentSubjectId
        )
    }
}
