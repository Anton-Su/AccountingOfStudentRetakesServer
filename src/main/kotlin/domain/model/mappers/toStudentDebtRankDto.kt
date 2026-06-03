package domain.model.mappers

import data.dto.StudentDebtRankDto
import domain.model.StudentDebtRank

fun StudentDebtRank.toStudentDebtRankDto() = StudentDebtRankDto(
    studentId = studentId,
    debtsCount = debtsCount,
    place = place,
    totalStudents = totalStudents,
    topPercent = topPercent
)