package domain.repository

import domain.model.Debt
import domain.model.Retake
import domain.model.RetakeEnrollment
import domain.model.Subject

interface StudentRepository {
    suspend fun findDebtsByStudentId(studentId: Long): List<Debt>
    suspend fun findSubjectById(subjectId: Long): Subject?
    suspend fun findRetakeById(retakeId: Long): Retake?
    suspend fun findRetakeIdByDebtId(debtId: Long): Long?
    suspend fun enrollToRetake(studentId: Long, debtId: Long, retakeId: Long): Debt
    suspend fun cancelRetakeEnrollment(studentId: Long, debtId: Long, retakeId: Long): Debt
    suspend fun findRetakesByTeacherId(teacherId: Long): List<Retake>
    suspend fun findEnrollmentsByRetakeId(retakeId: Long): List<RetakeEnrollment>
    suspend fun gradeStudent(retakeId: Long, studentId: Long, score: Int): RetakeEnrollment
}

