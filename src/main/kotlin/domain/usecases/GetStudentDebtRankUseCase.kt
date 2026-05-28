package domain.usecases

import domain.model.StudentDebtRank
import domain.repository.StudentRepository

class GetStudentDebtRankUseCase(
    private val studentRepository: StudentRepository
) {
    suspend operator fun invoke(studentId: Long): StudentDebtRank {
        val studentsDebts = studentRepository
            .getStudentsDebtCounts()
            .sortedBy { it.second }
        val totalStudents = studentsDebts.size
        if (totalStudents == 0) {
            return StudentDebtRank(
                studentId = studentId,
                debtsCount = 0,
                place = 0,
                totalStudents = 0,
                topPercent = 0
            )
        }
        val studentIndex = studentsDebts.indexOfFirst { it.first == studentId }
        if (studentIndex == -1) {
            return StudentDebtRank(
                studentId = studentId,
                debtsCount = 0,
                place = totalStudents,
                totalStudents = totalStudents,
                topPercent = 100 // хардкорное значение добавлено
            )
        }
        val place = studentIndex + 1
        val debtsCount = studentsDebts[studentIndex].second
        val topPercent =
            (((totalStudents - place).toDouble() / totalStudents) * 100).toInt()
        return StudentDebtRank(
            studentId = studentId,
            debtsCount = debtsCount,
            place = place,
            totalStudents = totalStudents,
            topPercent = topPercent
        )
    }
}