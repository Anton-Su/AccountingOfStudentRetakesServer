package domain.repository

import domain.model.Retake
import domain.model.RetakeEnrollment

interface TeacherRepository {
    suspend fun findRetakesByTeacherId(teacherId: Long): List<Retake>
    suspend fun findEnrollmentsByRetakeId(retakeId: Long): List<RetakeEnrollment>
    suspend fun gradeStudent(retakeId: Long, studentId: Long, score: Int): RetakeEnrollment
}