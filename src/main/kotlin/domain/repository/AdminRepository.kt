package domain.repository

import data.dto.SubjectDto
import domain.model.Comment
import domain.model.Retake
import domain.model.Subject
import domain.model.Teacher
import java.time.Instant

interface AdminRepository {
    suspend fun findTeachersByDiscipline(discipline: String): List<Teacher>
    suspend fun findAllSubjects(): List<Subject>
    suspend fun createRetake(startAt: Instant, endAt: Instant, teacherIds: List<Long>, type: String, place: String, admission: String?, subjectId: Long): Retake
    suspend fun updateRetake(id: Long, startAt: Instant, endAt: Instant, teacherIds: List<Long>, type: String, place: String, admission: String?, subjectId: Long): Retake
    suspend fun getAllComments(): List<Comment>
    suspend fun findAllRetakes(): List<Retake>
    suspend fun deleteRetake(id: Long)
}
