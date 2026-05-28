package domain.repository


import domain.model.Comment
import domain.model.Retake
import domain.model.RetakeEnrollment
import domain.model.StudentDebt
//import domain.model.StudentSubject
import domain.model.Subject

interface StudentRepository {
    suspend fun findDebtsByStudentId(studentId: Long): List<StudentDebt>
    suspend fun findSubjectById(subjectId: Long): Subject?
    suspend fun findRetakeById(retakeId: Long): Retake?
    suspend fun enrollToRetake(studentId: Long, debtId: Long, retakeId: Long): Boolean
    suspend fun cancelRetakeEnrollment(studentId: Long, debtId: Long, retakeId: Long): Boolean
    suspend fun createComment(studentId: Long, gradeplace: Int, gradeteacher: Int, gradeoverall: Int, comment: String?, retakeId: Long): Comment
    suspend fun getStudentsDebtCounts(): List<Pair<Long, Int>>
}

