package data.databases

import domain.model.DebtStatus
import domain.model.UserRole
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import security.PasswordHasher
import java.time.Instant

object DatabaseSeeder {
    fun seed() {
        if (UsersTable.selectAll().any()) return
        insertUser(
            role = UserRole.ADMIN,
            firstName = "Александр",
            secondName = "Сергеевич",
            lastName = "Волков",
            gender = "male",
            age = 42,
            email = "volkov.a.s@edu.mirea.ru",
            rawPassword = "Admin123!"
        )
        val teacher1Id = insertUser(
            role = UserRole.TEACHER,
            firstName = "Ирина",
            secondName = "Андреевна",
            lastName = "Кузнецова",
            gender = "female",
            age = 39,
            email = "kuznetsova.i.a@edu.mirea.ru",
            rawPassword = "Teacher123!"
        )
        val teacher2Id = insertUser(
            role = UserRole.TEACHER,
            firstName = "Дмитрий",
            secondName = "Олегович",
            lastName = "Смирнов",
            gender = "male",
            age = 44,
            email = "smirnov.d.o@edu.mirea.ru",
            rawPassword = "Teacher123!"
        )
        val studentId = insertUser(
            role = UserRole.STUDENT,
            firstName = "Максим",
            secondName = "Игоревич",
            lastName = "Петров",
            gender = "male",
            age = 20,
            email = "petrov.m.i@edu.mirea.ru",
            rawPassword = "Student123!"
        )
        insertStudentProfile(studentId, "ИКБО-61-23")
        insertTeacherProfile(teacher1Id)
        insertTeacherProfile(teacher2Id)
        insertTeacherDiscipline(
            teacher1Id,
            "Конфигурационное управление"
        )
        insertTeacherDiscipline(
            teacher1Id,
            "Мобильная разработка"
        )
        insertTeacherDiscipline(
            teacher2Id,
            "Основы российской государственности"
        )
        val configManagementSubjectId =
            insertSubject("Конфигурационное управление")
        val mobileDevSubjectId =
            insertSubject("Мобильная разработка")
        val statehoodSubjectId =
            insertSubject("Основы российской государственности")
        val configDebtId = insertDebt(
            studentId = studentId,
            subjectId = configManagementSubjectId,
            teacherId = teacher1Id,
            createdAt = 1_715_500_000_000,
            status = DebtStatus.ACTIVE
        )
        insertDebt(
            studentId = studentId,
            subjectId = statehoodSubjectId,
            teacherId = teacher2Id,
            createdAt = 1_715_586_400_000,
            status = DebtStatus.ACTIVE
        )
        val retakeId = insertRetake(
            type = "Экзамен",
            place = "Ауд. 101",
            admission = "40 баллов допуска",
            startAt = 1_716_000_000_000,
            endAt = 1_716_086_400_000
        )
        linkRetakeTeacher(retakeId, teacher1Id)
        linkRetakeTeacher(retakeId, teacher2Id)
        insertEnrollment(
            retakeId = retakeId,
            studentId = studentId,
            debtId = configDebtId
        )
        val gradedAt = 1_716_000_100_000
        insertGrade(
            retakeId = retakeId,
            studentId = studentId,
            score = 85,
            gradedAt = gradedAt
        )
        insertSubjectStudent(
            studentId = studentId,
            subjectId = configManagementSubjectId,
            retakeId = retakeId,
            score = 85,
            gradedAt = gradedAt
        )
    }

    private fun insertUser(
        role: UserRole,
        firstName: String,
        secondName: String,
        lastName: String,
        gender: String,
        age: Int,
        email: String,
        rawPassword: String
    ): EntityID<Long> = UsersTable.insertAndGetId {
        it[UsersTable.role] = role
        it[UsersTable.firstName] = firstName
        it[UsersTable.secondName] = secondName
        it[UsersTable.lastName] = lastName
        it[UsersTable.gender] = gender
        it[UsersTable.age] = age
        it[UsersTable.email] = email
        it[UsersTable.passwordHash] = PasswordHasher.hash(rawPassword)
    }

    private fun insertStudentProfile(userId: EntityID<Long>, groupName: String) {
        StudentsTable.insert {
            it[StudentsTable.userId] = userId
            it[StudentsTable.groupName] = groupName
        }
    }

    private fun insertTeacherProfile(userId: EntityID<Long>) {
        TeachersTable.insert {
            it[TeachersTable.userId] = userId
        }
    }

    private fun insertTeacherDiscipline(userId: EntityID<Long>, discipline: String) {
        TeacherDisciplinesTable.insert {
            it[TeacherDisciplinesTable.teacherId] = userId
            it[TeacherDisciplinesTable.discipline] = discipline
        }
    }

    private fun insertSubject(title: String): EntityID<Long> = SubjectsTable.insertAndGetId {
        it[SubjectsTable.title] = title
    }

    private fun insertDebt(
        studentId: EntityID<Long>,
        subjectId: EntityID<Long>,
        teacherId: EntityID<Long>,
        createdAt: Long,
        status: DebtStatus
    ): EntityID<Long> = DebtsTable.insertAndGetId {
        it[DebtsTable.studentId] = studentId
        it[DebtsTable.subjectId] = subjectId
        it[DebtsTable.teacherId] = teacherId
        it[DebtsTable.createdAt] = createdAt
        it[DebtsTable.status] = status
    }

    private fun insertRetake(type: String, place: String, admission: String?, startAt: Long, endAt: Long): EntityID<Long> =
        RetakesTable.insertAndGetId {
            it[RetakesTable.type] = type
            it[RetakesTable.place] = place
            it[RetakesTable.admission] = admission
            it[RetakesTable.startAt] = startAt
            it[RetakesTable.endAt] = endAt
            it[RetakesTable.lastModified] = Instant.now().toEpochMilli()
        }

    private fun linkRetakeTeacher(retakeId: EntityID<Long>, teacherId: EntityID<Long>) {
        RetakeTeachersTable.insert {
            it[RetakeTeachersTable.retakeId] = retakeId
            it[RetakeTeachersTable.teacherId] = teacherId
        }
    }

    private fun insertEnrollment(retakeId: EntityID<Long>, studentId: EntityID<Long>, debtId: EntityID<Long>): EntityID<Long> =
        RetakeEnrollmentsTable.insertAndGetId {
            it[RetakeEnrollmentsTable.retakeId] = retakeId
            it[RetakeEnrollmentsTable.studentId] = studentId
            it[RetakeEnrollmentsTable.debtId] = debtId
        }

    private fun insertGrade(retakeId: EntityID<Long>, studentId: EntityID<Long>, score: Int, gradedAt: Long) {
        GradesTable.insert {
            it[GradesTable.retakeId] = retakeId
            it[GradesTable.studentId] = studentId
            it[GradesTable.score] = score
            it[GradesTable.gradedAt] = gradedAt
            it[GradesTable.status] = "accepted"
        }
    }

    private fun insertSubjectStudent(
        studentId: EntityID<Long>,
        subjectId: EntityID<Long>,
        retakeId: EntityID<Long>,
        score: Int,
        gradedAt: Long
    ) {
        SubjectStudentsTable.insert {
            it[SubjectStudentsTable.studentId] = studentId
            it[SubjectStudentsTable.subjectId] = subjectId
            it[SubjectStudentsTable.retakeId] = retakeId
            it[SubjectStudentsTable.score] = score
            it[SubjectStudentsTable.gradedAt] = gradedAt
        }
    }
}