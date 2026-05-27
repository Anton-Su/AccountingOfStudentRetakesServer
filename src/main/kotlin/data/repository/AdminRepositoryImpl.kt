package data.repository

import data.databases.TeacherDisciplinesTable
import data.databases.RetakeTeachersTable
import data.databases.RetakesTable
import domain.model.Retake
import domain.model.Teacher
import domain.repository.AdminRepository
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

    override suspend fun createRetake(startAt: Instant, endAt: Instant, teacherIds: List<Long>, type: String, admission: String?): Retake = transaction {
        val normalizedTeacherIds = teacherIds.distinct()
        val retakeId = RetakesTable.insertAndGetId {
            it[RetakesTable.type] = type
            it[RetakesTable.admission] = admission
            it[RetakesTable.startAt] = startAt.toEpochMilli()
            it[RetakesTable.endAt] = endAt.toEpochMilli()
        }.value
        normalizedTeacherIds.forEach { teacherId ->
            RetakeTeachersTable.insert {
                it[RetakeTeachersTable.retakeId] = retakeId
                it[RetakeTeachersTable.teacherId] = teacherId
            }
        }
        loadRetake(retakeId)
    }

    override suspend fun updateRetake(id: Long, startAt: Instant, endAt: Instant, teacherIds: List<Long>, type: String, admission: String?): Retake = transaction {
        val updatedRows = RetakesTable.update({ RetakesTable.id eq id }) {
            it[RetakesTable.type] = type
            it[RetakesTable.admission] = admission
            it[RetakesTable.startAt] = startAt.toEpochMilli()
            it[RetakesTable.endAt] = endAt.toEpochMilli()
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

    private fun loadRetake(retakeId: Long): Retake {
        val row = RetakesTable.selectAll().first { it[RetakesTable.id].value == retakeId }
        val teacherIds = RetakeTeachersTable.selectAll()
            .filter { it[RetakeTeachersTable.retakeId].value == retakeId }
            .map { it[RetakeTeachersTable.teacherId].value }
        return row.toRetake(teacherIds)
    }

    private fun ResultRow.toRetake(teacherIds: List<Long>): Retake = Retake(
        id = this[RetakesTable.id].value,
        type = this[RetakesTable.type],
        admission = this[RetakesTable.admission],
        startAt = Instant.ofEpochMilli(this[RetakesTable.startAt]),
        endAt = Instant.ofEpochMilli(this[RetakesTable.endAt]),
        teacherIds = teacherIds
    )
}