package data.repository

import data.databases.CommentsTable
import data.databases.RetakeEnrollmentsTable
import data.databases.TeacherDisciplinesTable
import data.databases.RetakeTeachersTable
import data.databases.RetakesTable
import data.databases.StudentsTable
import data.databases.SubjectsTable
import data.databases.UsersTable
import data.mappers.toComment
import data.mappers.toRetake
import data.mappers.toSubject
import data.mappers.toTeacher
import domain.model.Comment
import domain.model.Retake
import domain.model.Subject
import domain.model.Teacher
import domain.repository.AdminRepository
import helpers.fetchTeacherIds
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant

class AdminRepositoryImpl : AdminRepository {
    override suspend fun findTeachersByDiscipline(discipline: String): List<Teacher> = transaction {
        val normalized = discipline.trim().lowercase()
        val teacherRows = TeacherDisciplinesTable
            .join(UsersTable, JoinType.INNER, TeacherDisciplinesTable.teacherId, UsersTable.id)
            .selectAll()
            .where { TeacherDisciplinesTable.discipline eq normalized }
            .groupBy { it[TeacherDisciplinesTable.teacherId].value }
        val teacherIds = teacherRows.keys.toList()
        val allDisciplines = TeacherDisciplinesTable.selectAll()
            .where { TeacherDisciplinesTable.teacherId inList teacherIds }
            .groupBy(
                { it[TeacherDisciplinesTable.teacherId].value },
                { it[TeacherDisciplinesTable.discipline] }
            )
        teacherRows.map { (teacherId, rows) ->
            rows.first().toTeacher(allDisciplines[teacherId] ?: emptyList())
        }
    }

    override suspend fun findAllSubjects(): List<Subject> = transaction {
        SubjectsTable.selectAll().map { it.toSubject() }
    }

    override suspend fun createRetake(startAt: Instant, endAt: Instant, teacherIds: List<Long>, type: String, place: String, admission: String?, subjectId: Long): Retake = transaction {
        val normalizedTeacherIds = teacherIds.distinct()
        val now = Instant.now().toEpochMilli()
        val retakeId = RetakesTable.insertAndGetId {
            it[RetakesTable.type] = type
            it[RetakesTable.place] = place
            it[RetakesTable.subjectId] = subjectId
            it[RetakesTable.admission] = admission
            it[RetakesTable.startAt] = startAt.toEpochMilli()
            it[RetakesTable.endAt] = endAt.toEpochMilli()
            it[RetakesTable.lastModified] = now
        }.value
        normalizedTeacherIds.forEach { teacherId ->
            RetakeTeachersTable.insert {
                it[RetakeTeachersTable.retakeId] = retakeId
                it[RetakeTeachersTable.teacherId] = teacherId
            }
        }
        loadRetake(retakeId)
    }

    override suspend fun updateRetake(id: Long, startAt: Instant, endAt: Instant, teacherIds: List<Long>, type: String, place: String, admission: String?, subjectId: Long): Retake = transaction {
        val now = Instant.now().toEpochMilli()
        val updatedRows = RetakesTable.update({ RetakesTable.id eq id }) {
            it[RetakesTable.type] = type
            it[RetakesTable.place] = place
            it[RetakesTable.subjectId] = subjectId
            it[RetakesTable.admission] = admission
            it[RetakesTable.startAt] = startAt.toEpochMilli()
            it[RetakesTable.endAt] = endAt.toEpochMilli()
            it[RetakesTable.lastModified] = now
        }
        if (updatedRows == 0) throw IllegalArgumentException("Retake with id $id not found")
        RetakeTeachersTable.deleteWhere { RetakeTeachersTable.retakeId eq id }
        teacherIds.distinct().forEach { teacherId ->
            RetakeTeachersTable.insert {
                it[RetakeTeachersTable.retakeId] = id
                it[RetakeTeachersTable.teacherId] = teacherId
            }
        }
        loadRetake(id)
    }

    override suspend fun getAllComments(): List<Comment> = transaction {
        CommentsTable
            .join(UsersTable, JoinType.INNER, CommentsTable.studentId, UsersTable.id)
            .join(StudentsTable, JoinType.INNER, CommentsTable.studentId, StudentsTable.id)
            .join(RetakesTable, JoinType.INNER, CommentsTable.retakeId, RetakesTable.id)
            .join(SubjectsTable, JoinType.INNER, RetakesTable.subjectId, SubjectsTable.id)
            .selectAll()
            .map {
                it.toComment()
            }
    }

    override suspend fun findAllRetakes(): List<Retake> = transaction {
        val allTeacherIds = RetakeTeachersTable.selectAll()
            .groupBy(
                { it[RetakeTeachersTable.retakeId].value },
                { it[RetakeTeachersTable.teacherId].value }
            )
        RetakesTable.selectAll().map { row ->
            val retakeId = row[RetakesTable.id].value
            row.toRetake(allTeacherIds[retakeId] ?: emptyList())
        }
    }

    override suspend fun deleteRetake(id: Long): Unit = transaction {
        val deleted = RetakesTable.deleteWhere { RetakesTable.id eq id }
        if (deleted == 0) throw IllegalArgumentException("Retake with id $id not found")
    }

    private fun loadRetake(retakeId: Long): Retake {
        val row = RetakesTable.selectAll()
            .where { RetakesTable.id eq retakeId }
            .first()
        return row.toRetake(fetchTeacherIds(retakeId))
    }
}