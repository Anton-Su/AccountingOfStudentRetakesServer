package domain.repository

import domain.model.Debt
import domain.model.Retake
import domain.model.Subject

interface StudentRepository {
    suspend fun findDebtsByStudentId(studentId: Long): List<Debt>
    suspend fun findSubjectById(subjectId: Long): Subject?
    suspend fun findRetakeById(retakeId: Long): Retake?
    suspend fun findRetakeIdByDebtId(debtId: Long): Long?
    suspend fun enrollToRetake(studentId: Long, debtId: Long, retakeId: Long): Debt
    suspend fun cancelRetakeEnrollment(studentId: Long, debtId: Long, retakeId: Long): Debt
}

