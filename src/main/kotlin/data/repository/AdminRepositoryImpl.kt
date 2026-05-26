package data.repository

import domain.model.Retake
import domain.model.Teacher
import domain.repository.AdminRepository
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong

class AdminRepositoryImpl : AdminRepository {
    private val retakes = mutableListOf<Retake>()
    private val nextRetakeId = AtomicLong(1)
    private val teachers = listOf(
        Teacher(userId = 2, disciplines = listOf("math", "algebra")),
        Teacher(userId = 4, disciplines = listOf("physics", "mechanics")),
        Teacher(userId = 5, disciplines = listOf("math", "programming"))
    )

    override suspend fun findTeachersByDiscipline(discipline: String): List<Teacher> {
        val expected = discipline.trim().lowercase()
        return teachers.filter { teacher ->
            teacher.disciplines.any { it.trim().lowercase() == expected }
        }
    }

    override suspend fun createRetake(startAt: Instant, endAt: Instant, teacherIds: List<Long>, type: String, admission: String?): Retake {
        val retake = Retake(
            id = nextRetakeId.getAndIncrement(),
            type = type,
            admission = admission,
            startAt = startAt,
            endAt = endAt,
            teacherIds = teacherIds
        )
        retakes.add(retake)
        return retake
    }

    override suspend fun updateRetake(id: Long, startAt: Instant, endAt: Instant, teacherIds: List<Long>, type: String, admission: String?): Retake {
        val idx = retakes.indexOfFirst { it.id == id }
        if (idx == -1) throw IllegalArgumentException("Retake with id $id not found")
        val updated = Retake(
            id = id,
            type = type,
            admission = admission,
            startAt = startAt,
            endAt = endAt,
            teacherIds = teacherIds
        )
        retakes[idx] = updated
        return updated
    }
}