package data.repository

import data.databases.CommentsTable
import data.databases.RetakeEnrollmentsTable
import data.databases.TeacherDisciplinesTable
import data.databases.RetakeTeachersTable
import data.databases.RetakesTable
import data.databases.SubjectsTable
import data.databases.UsersTable
import domain.model.Comment
import domain.model.Retake
import domain.model.Subject
import domain.model.Teacher
import domain.repository.AdminRepository
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
        val teacherIds = TeacherDisciplinesTable.selectAll()
            .filter { it[TeacherDisciplinesTable.discipline].trim().lowercase() == normalized }
            .map { it[TeacherDisciplinesTable.teacherId].value }
            .distinct()
        teacherIds.mapNotNull { teacherId ->
            val disciplines = TeacherDisciplinesTable.selectAll()
                .filter { it[TeacherDisciplinesTable.teacherId].value == teacherId }
                .map { it[TeacherDisciplinesTable.discipline] }
            if (disciplines.isEmpty()) null else Teacher(userId = teacherId, disciplines = disciplines)
        }
    }



    override suspend fun findAllSubjects(): List<Subject> = transaction {
        SubjectsTable.selectAll()
            .map {
                Subject(
                    id = it[SubjectsTable.id].value,
                    title = it[SubjectsTable.title]
                )
            }
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
            .join(RetakesTable, JoinType.INNER, CommentsTable.retakeId, RetakesTable.id)
            .join(SubjectsTable, JoinType.INNER, RetakesTable.subjectId, SubjectsTable.id)
            .selectAll()
            .map {
                Comment(
                    id = it[CommentsTable.id].value,
                    studentId = it[CommentsTable.studentId].value,
                    studentFullName = "${it[UsersTable.secondName]} ${it[UsersTable.firstName]} ${it[UsersTable.lastName]}",
                    gradeplace = it[CommentsTable.gradeplace],
                    gradeteacher = it[CommentsTable.gradeteacher],
                    gradeoverall = it[CommentsTable.gradeoverall],
                    comment = it[CommentsTable.comment],
                    retakeId = it[CommentsTable.retakeId].value,
                    retakeStartAt = Instant.ofEpochMilli(it[RetakesTable.startAt]).toString(),
                    retakeEndAt = Instant.ofEpochMilli(it[RetakesTable.endAt]).toString(),
                    subjectTitle = it[SubjectsTable.title],
                )
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
        // RetakeEnrollmentsTable.deleteWhere { RetakeEnrollmentsTable.retakeId eq id }
        // RetakeTeachersTable.deleteWhere { RetakeTeachersTable.retakeId eq id }
        val deleted = RetakesTable.deleteWhere { RetakesTable.id eq id }
        if (deleted == 0) throw IllegalArgumentException("Retake with id $id not found")
    }

    private fun loadRetake(retakeId: Long): Retake {
        val row = RetakesTable.selectAll().first { it[RetakesTable.id].value == retakeId }
        val teacherIds = RetakeTeachersTable.selectAll()
            .filter { it[RetakeTeachersTable.retakeId].value == retakeId }
            .map { it[RetakeTeachersTable.teacherId].value } // .value вернуть
        return row.toRetake(teacherIds)
    }

    private fun ResultRow.toRetake(teacherIds: List<Long>): Retake = Retake(
        id = this[RetakesTable.id].value,
        type = this[RetakesTable.type],
        place = this[RetakesTable.place],
        subjectId = this[RetakesTable.subjectId].value,
        admission = this[RetakesTable.admission],
        startAt = Instant.ofEpochMilli(this[RetakesTable.startAt]),
        endAt = Instant.ofEpochMilli(this[RetakesTable.endAt]),
        lastModified = Instant.ofEpochMilli(this[RetakesTable.lastModified]),
        teacherIds = teacherIds
    )
}