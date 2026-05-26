package data.store

import domain.model.Debt
import domain.model.DebtStatus
import domain.model.Retake
import domain.model.Subject
import domain.model.Teacher
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong

object InMemoryAcademicStore {
    val subjects: MutableList<Subject> = mutableListOf(
        Subject(1, "Math"),
        Subject(2, "Physics"),
        Subject(3, "Programming")
    )

    val teachers: MutableList<Teacher> = mutableListOf(
        Teacher(userId = 2, disciplines = listOf("math", "algebra")),
        Teacher(userId = 4, disciplines = listOf("physics", "mechanics")),
        Teacher(userId = 5, disciplines = listOf("math", "programming"))
    )

    val retakes: MutableList<Retake> = mutableListOf()
    val debts: MutableList<Debt> = mutableListOf(
        Debt(1, 3, 1, 2, Instant.parse("2026-05-01T10:00:00Z").toEpochMilli(), DebtStatus.ACTIVE),
        Debt(2, 3, 2, 4, Instant.parse("2026-05-02T10:00:00Z").toEpochMilli(), DebtStatus.ACTIVE),
        Debt(3, 6, 3, 5,  Instant.parse("2026-05-03T10:00:00Z").toEpochMilli(), DebtStatus.CLOSED)
    )

    val enrollmentByDebtId: MutableMap<Long, Long> = mutableMapOf()

    val nextRetakeId = AtomicLong(1)
}
