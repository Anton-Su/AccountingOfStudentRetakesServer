package data.repository

import data.store.InMemoryAcademicStore
import domain.model.Debt
import domain.model.DebtStatus
import domain.model.Retake
import domain.model.Subject
import domain.repository.StudentRepository

class StudentRepositoryImpl : StudentRepository {
    override suspend fun findDebtsByStudentId(studentId: Long): List<Debt> =
        InMemoryAcademicStore.debts.filter { it.studentId == studentId }

    override suspend fun findSubjectById(subjectId: Long): Subject? =
        InMemoryAcademicStore.subjects.firstOrNull { it.id == subjectId }

    override suspend fun findRetakeById(retakeId: Long): Retake? =
        InMemoryAcademicStore.retakes.firstOrNull { it.id == retakeId }

    override suspend fun findRetakeIdByDebtId(debtId: Long): Long? =
        InMemoryAcademicStore.enrollmentByDebtId[debtId]

    override suspend fun enrollToRetake(studentId: Long, debtId: Long, retakeId: Long): Debt {
        val debt = requireOwnedActiveDebt(studentId, debtId)
        require(findRetakeById(retakeId) != null) { "Retake with id $retakeId not found" }
        InMemoryAcademicStore.enrollmentByDebtId[debt.id] = retakeId
        return debt
    }

    override suspend fun cancelRetakeEnrollment(studentId: Long, debtId: Long, retakeId: Long): Debt {
        val debt = requireOwnedDebt(studentId, debtId)
        val currentRetakeId = InMemoryAcademicStore.enrollmentByDebtId[debt.id]
            ?: throw IllegalArgumentException("Debt ${debt.id} is not enrolled to any retake")
        require(currentRetakeId == retakeId) { "Debt ${debt.id} is enrolled to another retake" }
        InMemoryAcademicStore.enrollmentByDebtId.remove(debt.id)
        return debt
    }

    private fun requireOwnedDebt(studentId: Long, debtId: Long): Debt {
        return InMemoryAcademicStore.debts.firstOrNull { it.id == debtId && it.studentId == studentId }
            ?: throw IllegalArgumentException("Debt with id $debtId not found for student $studentId")
    }

    private fun requireOwnedActiveDebt(studentId: Long, debtId: Long): Debt {
        val debt = requireOwnedDebt(studentId, debtId)
        require(debt.status == DebtStatus.ACTIVE) { "Debt ${debt.id} is not active" }
        return debt
    }
}

