package data.repository

import data.databases.CommentsTable
import data.databases.RetakeEnrollmentsTable
import data.databases.RetakeTeachersTable
import data.databases.RetakesTable
import data.databases.StudentSubjectsTable
import data.databases.StudentsTable
import data.databases.SubjectsTable
import data.databases.UsersTable
import domain.model.Comment
import domain.model.Retake
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

    override suspend fun enrollToRetake(studentId: Long, debtId: Long, retakeId: Long): Boolean = transaction {
        val studentSubject = StudentSubjectsTable
            .selectAll()
            .firstOrNull {
                it[StudentSubjectsTable.studentId].value == studentId &&
                        it[StudentSubjectsTable.subjectId].value == debtId
            } ?: throw IllegalArgumentException("Student subject not found")
        val studentSubjectId = studentSubject[StudentSubjectsTable.id].value
        val retake = findRetakeByIdInternal(retakeId)
            ?: throw IllegalArgumentException("Retake with id $retakeId not found")
        require(retake.subjectId == debtId) {
            "Retake subject does not match debt subject"
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

    override suspend fun cancelRetakeEnrollment(studentId: Long, debtId: Long, retakeId: Long): Boolean = transaction {
        println(123)
        val studentSubject = StudentSubjectsTable
            .selectAll()
            .firstOrNull {
                it[StudentSubjectsTable.studentId].value == studentId &&
                        it[StudentSubjectsTable.subjectId].value == debtId
            } ?: throw IllegalArgumentException("Student subject not found")
        println(345)
        print(studentSubject)
        val studentSubjectId = studentSubject[StudentSubjectsTable.id].value
        println(studentSubjectId)
        println(567)
        val exists = RetakeEnrollmentsTable.selectAll().any {
            it[RetakeEnrollmentsTable.studentSubjectId].value == studentSubjectId &&
                    it[RetakeEnrollmentsTable.retakeId].value == retakeId
        }
        print(exists)
        println(87789)
        require(exists) {
            "Student is not enrolled to this retake"
        }
        print(99999)
        RetakeEnrollmentsTable.deleteWhere {
            (RetakeEnrollmentsTable.studentSubjectId eq studentSubjectId) and
                    (RetakeEnrollmentsTable.retakeId eq retakeId)
        }
        print(1000000)
        true
    }

    override suspend fun createComment(
        studentId: Long,
        gradeplace: Int,
        gradeteacher: Int,
        gradeoverall: Int,
        comment: String?,
        retakeId: Long
    ): Comment = transaction {
        val id = CommentsTable.insertAndGetId {
            it[CommentsTable.studentId] = studentId
            it[CommentsTable.gradeplace] = gradeplace
            it[CommentsTable.gradeteacher] = gradeteacher
            it[CommentsTable.gradeoverall] = gradeoverall
            it[CommentsTable.comment] = comment
            it[CommentsTable.retakeId] = retakeId
        }.value
        val user = UsersTable.selectAll()
            .first { it[UsersTable.id].value == studentId }
        val student = StudentsTable.selectAll()
            .first { it[StudentsTable.id].value == studentId }
        val retake = RetakesTable.selectAll()
            .first { it[RetakesTable.id].value == retakeId }
        val subject = SubjectsTable.selectAll()
            .first { it[SubjectsTable.id].value == retake[RetakesTable.subjectId].value }
        Comment(
            id = id,
            studentId = studentId,
            studentFullName = "${user[UsersTable.secondName]} ${user[UsersTable.firstName]} ${user[UsersTable.lastName]}",
            groupName = student[StudentsTable.groupName],
            gradeplace = gradeplace,
            gradeteacher = gradeteacher,
            gradeoverall = gradeoverall,
            comment = comment,
            retakeId = retakeId,
            retakeStartAt = Instant.ofEpochMilli(retake[RetakesTable.startAt]).toString(),
            retakeEndAt = Instant.ofEpochMilli(retake[RetakesTable.endAt]).toString(),
            subjectTitle = subject[SubjectsTable.title],
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

    override suspend fun findAvailableRetakes(studentId: Long): List<Retake> = transaction {
        val debtSubjectIds = StudentSubjectsTable
            .selectAll()
            .where {
                (StudentSubjectsTable.studentId eq studentId) and
                        (StudentSubjectsTable.status eq StudentSubjectStatus.DEBT)
            }
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
        RetakesTable
            .selectAll()
            .where {
                (RetakesTable.subjectId inList debtSubjectIds) and
                        (RetakesTable.id notInList enrolledRetakeIds)
            }
            .map { it.toRetake() }
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
        RetakesTable
            .selectAll()
            .where { RetakesTable.id inList enrolledRetakeIds }
            .map { it.toRetake() }
    }

    private fun findRetakeByIdInternal(retakeId: Long): Retake? =
        RetakesTable.selectAll().firstOrNull { it[RetakesTable.id].value == retakeId }?.toRetake()


    private fun ResultRow.toSubject(): Subject = Subject(
        id = this[SubjectsTable.id].value,
        title = this[SubjectsTable.title]
    )

    private fun ResultRow.toRetake(): Retake {
        val retakeId = this[RetakesTable.id].value
        val teacherIds = RetakeTeachersTable.selectAll()
            .filter { it[RetakeTeachersTable.retakeId].value == retakeId }
            .map { it[RetakeTeachersTable.teacherId].value}
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
