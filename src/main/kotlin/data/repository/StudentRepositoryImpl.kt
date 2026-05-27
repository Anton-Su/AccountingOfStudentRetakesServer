package data.repository

import data.store.InMemoryAcademicStore
import domain.model.Debt
import domain.model.DebtStatus
import domain.model.Grade
import domain.model.Retake
import domain.model.RetakeEnrollment
import domain.model.Subject
import domain.model.SubjectStudent
import domain.repository.StudentRepository
import java.time.Instant

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

    override suspend fun findRetakesByTeacherId(teacherId: Long): List<Retake> =
        InMemoryAcademicStore.retakes.filter { it.teacherIds.contains(teacherId) }

    override suspend fun findEnrollmentsByRetakeId(retakeId: Long): List<RetakeEnrollment> =
        InMemoryAcademicStore.enrollments.filter { it.retakeId == retakeId }

    override suspend fun gradeStudent(retakeId: Long, studentId: Long, score: Int): RetakeEnrollment {
        require(score in 0..100) { "Score must be between 0 and 100" }
        val enrollment = InMemoryAcademicStore.enrollments.firstOrNull {
            it.retakeId == retakeId && it.studentId == studentId
        } ?: throw IllegalArgumentException("Enrollment not found for retake $retakeId and student $studentId")
        val updated = enrollment.copy(score = score)
        val idx = InMemoryAcademicStore.enrollments.indexOf(enrollment)
        InMemoryAcademicStore.enrollments[idx] = updated
        val grade = Grade(
            id = InMemoryAcademicStore.nextGradeId.getAndIncrement(),
            retakeId = retakeId,
            studentId = studentId,
            score = score,
            gradedAt = Instant.now()
        )
        InMemoryAcademicStore.grades.add(grade)

        val debt = InMemoryAcademicStore.debts.firstOrNull { it.id == enrollment.debtId }
            ?: throw IllegalArgumentException("Debt with id ${enrollment.debtId} not found")
        val subjectStudentIdx = InMemoryAcademicStore.subjectStudents.indexOfFirst {
            it.studentId == studentId && it.subjectId == debt.subjectId
        }
        val subjectStudent = SubjectStudent(
            id = if (subjectStudentIdx >= 0) {
                InMemoryAcademicStore.subjectStudents[subjectStudentIdx].id
            } else {
                InMemoryAcademicStore.nextSubjectStudentId.getAndIncrement()
            },
            studentId = studentId,
            subjectId = debt.subjectId,
            retakeId = retakeId,
            score = score,
            gradedAt = grade.gradedAt.toEpochMilli()
        )
        if (subjectStudentIdx >= 0) {
            InMemoryAcademicStore.subjectStudents[subjectStudentIdx] = subjectStudent
        } else {
            InMemoryAcademicStore.subjectStudents.add(subjectStudent)
        }
        return updated
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

