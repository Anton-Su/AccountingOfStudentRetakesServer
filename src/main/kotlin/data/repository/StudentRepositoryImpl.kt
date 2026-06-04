package data.repository

import data.databases.CommentsTable
import data.databases.RetakeEnrollmentsTable
import data.databases.RetakeTeachersTable
import data.databases.RetakesTable
import data.databases.StudentSubjectsTable
import data.databases.StudentsTable
import data.databases.SubjectsTable
import data.databases.UsersTable
import data.mappers.toComment
import data.mappers.toRetake
import data.mappers.toStudentDebt
import data.mappers.toSubject
import domain.model.Comment
import domain.model.Retake
import domain.model.StudentDebt
import domain.model.StudentSubjectStatus
import domain.model.Subject
import domain.repository.StudentRepository
import helpers.fetchTeacherIds
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.temporal.ChronoUnit

class StudentRepositoryImpl : StudentRepository {
    override suspend fun findDebtsByStudentId(studentId: Long) = transaction {
        (StudentSubjectsTable innerJoin SubjectsTable)
            .selectAll()
            .where { (StudentSubjectsTable.studentId eq studentId) and
                    (StudentSubjectsTable.status eq StudentSubjectStatus.DEBT)
            }
            .map {
                it.toStudentDebt()
            }
    }

    override suspend fun findSubjectById(subjectId: Long): Subject? = transaction {
        SubjectsTable.selectAll()
            .where { SubjectsTable.id eq subjectId }
            .firstOrNull()
            ?.toSubject()
    }

    override suspend fun findRetakeById(retakeId: Long): Retake? = transaction {
        RetakesTable.selectAll()
            .where { RetakesTable.id eq retakeId }
            .firstOrNull()
            ?.toRetake(fetchTeacherIds(retakeId))
    }

    override suspend fun enrollToRetake(studentId: Long, debtId: Long, retakeId: Long): Boolean = transaction {
        val studentSubject = StudentSubjectsTable
            .selectAll()
            .where { (StudentSubjectsTable.studentId eq studentId) and (StudentSubjectsTable.subjectId eq debtId) }
            .firstOrNull() ?: throw IllegalArgumentException("Student subject not found")
        val studentSubjectId = studentSubject[StudentSubjectsTable.id].value
        val retake = RetakesTable.selectAll()
            .where { RetakesTable.id eq retakeId }
            .firstOrNull()
            ?.toRetake(fetchTeacherIds(retakeId))
            ?: throw IllegalArgumentException("Retake with id $retakeId not found")
        require(retake.subjectId == debtId) { "Retake subject does not match debt subject" }
        RetakeEnrollmentsTable.deleteWhere { RetakeEnrollmentsTable.studentSubjectId eq studentSubjectId }
        RetakeEnrollmentsTable.insert {
            it[RetakeEnrollmentsTable.retakeId] = retakeId
            it[RetakeEnrollmentsTable.studentSubjectId] = studentSubjectId
            it[RetakeEnrollmentsTable.enrolledAt] = Instant.now().toEpochMilli()
        }
        true
    }

    override suspend fun cancelRetakeEnrollment(studentId: Long, debtId: Long, retakeId: Long): Boolean = transaction {
        val studentSubject = StudentSubjectsTable
            .selectAll()
            .where { (StudentSubjectsTable.studentId eq studentId) and (StudentSubjectsTable.subjectId eq debtId) }
            .firstOrNull() ?: throw IllegalArgumentException("Student subject not found")
        val studentSubjectId = studentSubject[StudentSubjectsTable.id].value
        val exists = RetakeEnrollmentsTable
            .selectAll()
            .where { (RetakeEnrollmentsTable.studentSubjectId eq studentSubjectId) and (RetakeEnrollmentsTable.retakeId eq retakeId) }
            .any()
        require(exists) { "Student is not enrolled to this retake" }
        RetakeEnrollmentsTable.deleteWhere { (RetakeEnrollmentsTable.studentSubjectId eq studentSubjectId) and (RetakeEnrollmentsTable.retakeId eq retakeId) }
        true
    }

    override suspend fun createComment(studentId: Long, gradeplace: Int, gradeteacher: Int, gradeoverall: Int, comment: String?, retakeId: Long): Comment = transaction {
        val id = CommentsTable.insertAndGetId {
            it[CommentsTable.studentId] = studentId
            it[CommentsTable.gradePlace] = gradeplace
            it[CommentsTable.gradeTeacher] = gradeteacher
            it[CommentsTable.gradeOverall] = gradeoverall
            it[CommentsTable.comment] = comment
            it[CommentsTable.retakeId] = retakeId
        }.value
        (CommentsTable
                innerJoin UsersTable
                innerJoin StudentsTable
                innerJoin RetakesTable
                innerJoin SubjectsTable)
            .selectAll()
            .where { CommentsTable.id eq id }
            .single()
            .toComment()
    }

    override suspend fun getStudentsDebtCounts(): List<Pair<Long, Int>> = transaction {
        StudentSubjectsTable
            .select(StudentSubjectsTable.studentId, StudentSubjectsTable.id.count())
            .where { StudentSubjectsTable.status eq StudentSubjectStatus.DEBT }
            .groupBy(StudentSubjectsTable.studentId)
            .map { it[StudentSubjectsTable.studentId].value to it[StudentSubjectsTable.id.count()].toInt() }
    }

    override suspend fun findAvailableRetakes(studentId: Long): List<Retake> = transaction {
        val debtSubjectIds = StudentSubjectsTable
            .selectAll()
            .where { (StudentSubjectsTable.studentId eq studentId) and (StudentSubjectsTable.status eq StudentSubjectStatus.DEBT) }
            .map { it[StudentSubjectsTable.subjectId].value }
        if (debtSubjectIds.isEmpty()) return@transaction emptyList()
        val studentSubjectIds = StudentSubjectsTable
            .selectAll()
            .where { StudentSubjectsTable.studentId eq studentId }
            .map { it[StudentSubjectsTable.id].value }
        val enrolledRetakeIds = if (studentSubjectIds.isNotEmpty()) {
            RetakeEnrollmentsTable
                .selectAll()
                .where { RetakeEnrollmentsTable.studentSubjectId inList studentSubjectIds }
                .map { it[RetakeEnrollmentsTable.retakeId].value }
        } else emptyList()
        val availableRetakeIds = RetakesTable
            .selectAll()
            .where {
                (RetakesTable.subjectId inList debtSubjectIds) and (RetakesTable.id notInList enrolledRetakeIds) and
                        (RetakesTable.startAt greater System.currentTimeMillis())
            }
            .map { it[RetakesTable.id].value}
        if (availableRetakeIds.isEmpty()) return@transaction emptyList()
        val teacherIds = RetakeTeachersTable
            .selectAll()
            .where { RetakeTeachersTable.retakeId inList availableRetakeIds }
            .groupBy(
                { it[RetakeTeachersTable.retakeId].value },
                { it[RetakeTeachersTable.teacherId].value }
            )
        RetakesTable
            .selectAll()
            .where { RetakesTable.id inList availableRetakeIds }
            .map { row ->
                val retakeId = row[RetakesTable.id].value
                row.toRetake(teacherIds[retakeId] ?: emptyList())
            }
    }

    override suspend fun findEnrolledRetakes(studentId: Long): List<Retake> = transaction {
        val studentSubjectIds = StudentSubjectsTable
            .selectAll()
            .where { StudentSubjectsTable.studentId eq studentId }
            .map { it[StudentSubjectsTable.id].value }
        if (studentSubjectIds.isEmpty()) return@transaction emptyList()
        val enrolledRetakeIds = RetakeEnrollmentsTable
            .selectAll()
            .where { RetakeEnrollmentsTable.studentSubjectId inList studentSubjectIds }
            .map { it[RetakeEnrollmentsTable.retakeId].value }
        if (enrolledRetakeIds.isEmpty()) return@transaction emptyList()
        val teacherIds = RetakeTeachersTable
            .selectAll()
            .where { RetakeTeachersTable.retakeId inList enrolledRetakeIds }
            .groupBy(
                { it[RetakeTeachersTable.retakeId].value },
                { it[RetakeTeachersTable.teacherId].value }
            )
        val oneDayAgo = Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli()
        RetakesTable
            .selectAll()
            .where { (RetakesTable.id inList enrolledRetakeIds) and (RetakesTable.endAt greaterEq oneDayAgo)  }
            .map { row ->
                val retakeId = row[RetakesTable.id].value
                row.toRetake(teacherIds[retakeId] ?: emptyList())
            }
    }
}
