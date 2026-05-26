package data.repository

import data.store.InMemoryAcademicStore
import domain.model.Retake
import domain.model.Teacher
import domain.repository.AdminRepository
import java.time.Instant

class AdminRepositoryImpl : AdminRepository {
    override suspend fun findTeachersByDiscipline(discipline: String): List<Teacher> {
        val expected = discipline.trim().lowercase()
        return InMemoryAcademicStore.teachers.filter { teacher ->
            teacher.disciplines.any { it.trim().lowercase() == expected }
        }
    }

    override suspend fun createRetake(startAt: Instant, endAt: Instant, teacherIds: List<Long>, type: String, admission: String?): Retake {
        val retake = Retake(
            id = InMemoryAcademicStore.nextRetakeId.getAndIncrement(),
            type = type,
            admission = admission,
            startAt = startAt,
            endAt = endAt,
            teacherIds = teacherIds
        )
        InMemoryAcademicStore.retakes.add(retake)
        return retake
    }

    override suspend fun updateRetake(id: Long, startAt: Instant, endAt: Instant, teacherIds: List<Long>, type: String, admission: String?): Retake {
        val idx = InMemoryAcademicStore.retakes.indexOfFirst { it.id == id }
        if (idx == -1) throw IllegalArgumentException("Retake with id $id not found")
        val updated = Retake(
            id = id,
            type = type,
            admission = admission,
            startAt = startAt,
            endAt = endAt,
            teacherIds = teacherIds
        )
        InMemoryAcademicStore.retakes[idx] = updated
        return updated
    }
}