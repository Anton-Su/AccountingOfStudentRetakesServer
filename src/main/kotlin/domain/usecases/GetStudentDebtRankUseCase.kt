package domain.usecases

import domain.model.StudentDebtRank
import domain.repository.StudentRepository
import kotlin.math.roundToInt

class GetStudentDebtRankUseCase(private val studentRepository: StudentRepository) {
    suspend operator fun invoke(studentId: Long): StudentDebtRank {
        val studentsDebts = studentRepository.getStudentsDebtCounts().sortedBy { it.second }
        val totalStudents = studentsDebts.size
        val studentIndex = studentsDebts.indexOfFirst { it.first == studentId }
        if (totalStudents == 0 || studentIndex == -1) {
            return StudentDebtRank(
                studentId = studentId,
                debtsCount = 0,
                place = 0,
                totalStudents = totalStudents,
                topPercent = 100 // без понятия, как регулировать, что у человека нет долгов, так что будет в абсолютном топе
            )
        }
        val place = studentIndex + 1
        val debtsCount = studentsDebts[studentIndex].second
        val topPercent = (((totalStudents - place).toDouble() / totalStudents) * 100).roundToInt()
        return StudentDebtRank(
            studentId = studentId,
            debtsCount = debtsCount,
            place = place,
            totalStudents = totalStudents,
            topPercent = topPercent
        )
    }
}