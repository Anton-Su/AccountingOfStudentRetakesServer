package data.databases

import domain.model.StudentSubjectStatus
import domain.model.UserRole
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import security.PasswordHasher
import java.time.Instant

object DatabaseSeeder {
    private val NOW = Instant.now().toEpochMilli()
    private const val HOUR = 3_600_000L
    private const val DAY = 86_400_000L

    fun seed() {
        if (UsersTable.selectAll().any()) return

        // ── Админ ──────────────────────────────────────────────────────────
        insertUser(role = UserRole.ADMIN, firstName = "Александр", secondName = "Сергеевич", lastName = "Волков", gender = "male", age = 42, email = "volkov.a.s@edu.mirea.ru", rawPassword = "Admin123!")

        // ── Преподаватели ──────────────────────────────────────────────────
        val teacher1Id = insertUser(role = UserRole.TEACHER, firstName = "Ирина",   secondName = "Андреевна",  lastName = "Кузнецова", gender = "female", age = 39, email = "kuznetsova.i.a@edu.mirea.ru", rawPassword = "Teacher123!")
        val teacher2Id = insertUser(role = UserRole.TEACHER, firstName = "Дмитрий", secondName = "Олегович",   lastName = "Смирнов",   gender = "male",   age = 44, email = "smirnov.d.o@edu.mirea.ru",   rawPassword = "Teacher123!")
        val teacher3Id = insertUser(role = UserRole.TEACHER, firstName = "Марина",  secondName = "Викторовна", lastName = "Соколова",  gender = "female", age = 37, email = "sokolova.m.v@edu.mirea.ru",  rawPassword = "Teacher123!")
        val teacher4Id = insertUser(role = UserRole.TEACHER, firstName = "Алексей", secondName = "Павлович",   lastName = "Морозов",   gender = "male",   age = 51, email = "morozov.a.p@edu.mirea.ru",   rawPassword = "Teacher123!")

        listOf(teacher1Id, teacher2Id, teacher3Id, teacher4Id).forEach { insertTeacherProfile(it) }

        insertTeacherDiscipline(teacher1Id, "Конфигурационное управление")
        insertTeacherDiscipline(teacher1Id, "Мобильная разработка")
        insertTeacherDiscipline(teacher2Id, "Основы российской государственности")
        insertTeacherDiscipline(teacher3Id, "Системное администрирование")
        insertTeacherDiscipline(teacher3Id, "Исследование операций")
        insertTeacherDiscipline(teacher4Id, "Базы данных")
        insertTeacherDiscipline(teacher4Id, "Архитектура программных систем")

        // ── Предметы ───────────────────────────────────────────────────────
        val configSubjectId       = insertSubject("Конфигурационное управление")
        val mobileSubjectId       = insertSubject("Мобильная разработка")
        val statehoodSubjectId    = insertSubject("Основы российской государственности")
        val sysAdminSubjectId     = insertSubject("Системное администрирование")
        val operationsSubjectId   = insertSubject("Исследование операций")
        val dbSubjectId           = insertSubject("Базы данных")
        val architectureSubjectId = insertSubject("Архитектура программных систем")

        // ── Студенты ───────────────────────────────────────────────────────
        val student1Id = insertUser(role = UserRole.STUDENT, firstName = "Максим",    secondName = "Игоревич",   lastName = "Петров",   gender = "male",   age = 20, email = "petrov.m.i@edu.mirea.ru",   rawPassword = "Student123!")
        val student2Id = insertUser(role = UserRole.STUDENT, firstName = "Анна",      secondName = "Сергеевна",  lastName = "Орлова",   gender = "female", age = 19, email = "orlova.a.s@edu.mirea.ru",   rawPassword = "Student123!")
        val student3Id = insertUser(role = UserRole.STUDENT, firstName = "Кирилл",    secondName = "Андреевич",  lastName = "Васильев", gender = "male",   age = 21, email = "vasilev.k.a@edu.mirea.ru",  rawPassword = "Student123!")
        val student4Id = insertUser(role = UserRole.STUDENT, firstName = "Екатерина", secondName = "Олеговна",   lastName = "Фролова",  gender = "female", age = 20, email = "frolova.e.o@edu.mirea.ru",  rawPassword = "Student123!")
        val student5Id = insertUser(role = UserRole.STUDENT, firstName = "Даниил",    secondName = "Максимович", lastName = "Лебедев",  gender = "male",   age = 22, email = "lebedev.d.m@edu.mirea.ru",  rawPassword = "Student123!")

        insertStudentProfile(student1Id, "ИКБО-61-23")
        insertStudentProfile(student2Id, "ИКБО-61-23")
        insertStudentProfile(student3Id, "ИКБО-62-23")
        insertStudentProfile(student4Id, "ИКБО-62-23")
        insertStudentProfile(student5Id, "ИКБО-63-23")

        // ── Долги студентов ────────────────────────────────────────────────
        // Максим: долги statehood, config; сдал mobile, db
        val maximStatehoodId = insertStudentSubject(student1Id, statehoodSubjectId, StudentSubjectStatus.DEBT,   null, NOW - 30 * DAY)
        val maximConfigId    = insertStudentSubject(student1Id, configSubjectId,    StudentSubjectStatus.DEBT,   null, NOW - 31 * DAY)
        insertStudentSubject(student1Id, mobileSubjectId, StudentSubjectStatus.OK, 4, NOW - 60 * DAY)
        insertStudentSubject(student1Id, dbSubjectId,     StudentSubjectStatus.OK, 5, NOW - 62 * DAY)

        // Анна: долги operations, sysAdmin; сдала mobile
        val annaOperationsId = insertStudentSubject(student2Id, operationsSubjectId, StudentSubjectStatus.DEBT, null, NOW - 20 * DAY)
        val annaSysAdminId   = insertStudentSubject(student2Id, sysAdminSubjectId,   StudentSubjectStatus.DEBT, null, NOW - 21 * DAY)
        insertStudentSubject(student2Id, mobileSubjectId, StudentSubjectStatus.OK, 5, NOW - 50 * DAY)

        // Кирилл: долг architecture, sysAdmin; сдал db
        val kirillArchitectureId = insertStudentSubject(student3Id, architectureSubjectId, StudentSubjectStatus.DEBT, null, NOW - 15 * DAY)
        val kirillSysAdminId     = insertStudentSubject(student3Id, sysAdminSubjectId,     StudentSubjectStatus.DEBT, null, NOW - 16 * DAY)
        insertStudentSubject(student3Id, dbSubjectId, StudentSubjectStatus.OK, 3, NOW - 70 * DAY)

        // Екатерина: всё сдала
        insertStudentSubject(student4Id, configSubjectId,     StudentSubjectStatus.OK, 5, NOW - 90 * DAY)
        insertStudentSubject(student4Id, mobileSubjectId,     StudentSubjectStatus.OK, 5, NOW - 91 * DAY)
        insertStudentSubject(student4Id, operationsSubjectId, StudentSubjectStatus.OK, 4, NOW - 92 * DAY)

        // Даниил: долги db, config, operations
        val daniilDbId         = insertStudentSubject(student5Id, dbSubjectId,         StudentSubjectStatus.DEBT, null, NOW - 10 * DAY)
        val daniilConfigId     = insertStudentSubject(student5Id, configSubjectId,     StudentSubjectStatus.DEBT, null, NOW - 11 * DAY)
        val daniilOperationsId = insertStudentSubject(student5Id, operationsSubjectId, StudentSubjectStatus.DEBT, null, NOW - 12 * DAY)

        // ══════════════════════════════════════════════════════════════════
        // ПЕРЕСДАЧИ
        // ══════════════════════════════════════════════════════════════════

        // ── ПРОШЕДШИЕ (для комментариев) ───────────────────────────────────
        val pastRetake1Id = insertRetake(  // statehood — прошла 30 дней назад
            type = "Экзамен", place = "Ауд. 101", admission = "40 баллов допуска",
            startAt = NOW - 30 * DAY, endAt = NOW - 30 * DAY + 2 * HOUR,
            subjectId = statehoodSubjectId.value
        )
        val pastRetake2Id = insertRetake(  // operations — прошла 20 дней назад
            type = "Зачёт", place = "Ауд. 212", admission = "Лабораторные должны быть сданы",
            startAt = NOW - 20 * DAY, endAt = NOW - 20 * DAY + 2 * HOUR,
            subjectId = operationsSubjectId.value
        )
        val pastRetake3Id = insertRetake(  // db — прошла 10 дней назад
            type = "Экзамен", place = "Ауд. 505", admission = "Минимум 30 баллов",
            startAt = NOW - 10 * DAY, endAt = NOW - 10 * DAY + 2 * HOUR,
            subjectId = dbSubjectId.value
        )

        linkRetakeTeacher(pastRetake1Id, teacher2Id)
        linkRetakeTeacher(pastRetake2Id, teacher3Id)
        linkRetakeTeacher(pastRetake3Id, teacher4Id)

        // ── БУДУЩИЕ (открыты для записи) ──────────────────────────────────
        val futureRetake1Id = insertRetake(  // config — через 7 дней
            type = "Экзамен", place = "Ауд. 404", admission = "20 баллов допуска",
            startAt = NOW + 7 * DAY, endAt = NOW + 7 * DAY + 2 * HOUR,
            subjectId = configSubjectId.value
        )
        val futureRetake2Id = insertRetake(  // sysAdmin — через 10 дней
            type = "Зачёт", place = "Ауд. 303", admission = "Все лабораторные сданы",
            startAt = NOW + 10 * DAY, endAt = NOW + 10 * DAY + 2 * HOUR,
            subjectId = sysAdminSubjectId.value
        )
        val futureRetake3Id = insertRetake(  // operations — через 14 дней
            type = "Зачёт", place = "Ауд. 212", admission = "Лабораторные должны быть сданы",
            startAt = NOW + 14 * DAY, endAt = NOW + 14 * DAY + 2 * HOUR,
            subjectId = operationsSubjectId.value
        )
        val futureRetake4Id = insertRetake(  // db — через 21 день
            type = "Экзамен", place = "Ауд. 505", admission = "Минимум 30 баллов",
            startAt = NOW + 21 * DAY, endAt = NOW + 21 * DAY + 2 * HOUR,
            subjectId = dbSubjectId.value
        )
        val futureRetake5Id = insertRetake(  // architecture — через 28 дней
            type = "Экзамен", place = "Ауд. 108", admission = "Курсовая защищена",
            startAt = NOW + 28 * DAY, endAt = NOW + 28 * DAY + 2 * HOUR,
            subjectId = architectureSubjectId.value
        )
        val futureRetake6Id = insertRetake(  // statehood — через 35 дней
            type = "Экзамен", place = "Ауд. 101", admission = "40 баллов допуска",
            startAt = NOW + 35 * DAY, endAt = NOW + 35 * DAY + 2 * HOUR,
            subjectId = statehoodSubjectId.value
        )

        linkRetakeTeacher(futureRetake1Id, teacher1Id)
        linkRetakeTeacher(futureRetake2Id, teacher3Id)
        linkRetakeTeacher(futureRetake3Id, teacher3Id)
        linkRetakeTeacher(futureRetake4Id, teacher4Id)
        linkRetakeTeacher(futureRetake5Id, teacher4Id)
        linkRetakeTeacher(futureRetake6Id, teacher2Id)

        // ── Записи на прошедшие пересдачи ─────────────────────────────────
        insertEnrollment(pastRetake1Id, maximStatehoodId)
        insertEnrollment(pastRetake2Id, annaOperationsId)
        insertEnrollment(pastRetake2Id, daniilOperationsId)
        insertEnrollment(pastRetake3Id, daniilDbId)

        // ── Оценки по прошедшим пересдачам ────────────────────────────────
        val gradedAt1 = NOW - 30 * DAY + 2 * HOUR
        insertGrade(pastRetake1Id, maximStatehoodId, score = 4, gradedAt = gradedAt1)
        updateStudentSubjectAfterGrade(maximStatehoodId, score = 4, updatedAt = gradedAt1)

        val gradedAt2 = NOW - 20 * DAY + 2 * HOUR
        insertGrade(pastRetake2Id, annaOperationsId, score = 5, gradedAt = gradedAt2)
        updateStudentSubjectAfterGrade(annaOperationsId, score = 5, updatedAt = gradedAt2)

        val gradedAt3 = NOW - 10 * DAY + 2 * HOUR
        insertGrade(pastRetake3Id, daniilDbId, score = 3, gradedAt = gradedAt3)
        updateStudentSubjectAfterGrade(daniilDbId, score = 3, updatedAt = gradedAt3)

        // ── Записи на будущие пересдачи ───────────────────────────────────
        // Максим записан на config (futureRetake1)
        insertEnrollment(futureRetake1Id, maximConfigId)
        // Анна записана на sysAdmin (futureRetake2)
        insertEnrollment(futureRetake2Id, annaSysAdminId)
        // Даниил записан на operations и config
        insertEnrollment(futureRetake3Id, daniilOperationsId)
        insertEnrollment(futureRetake1Id, daniilConfigId)
        // Кирилл записан на architecture и sysAdmin
        insertEnrollment(futureRetake5Id, kirillArchitectureId)
        insertEnrollment(futureRetake2Id, kirillSysAdminId)

        // ══════════════════════════════════════════════════════════════════
        // КОММЕНТАРИИ к прошедшим пересдачам
        // ══════════════════════════════════════════════════════════════════

        // pastRetake1 (statehood) — Максим получил 4
        insertComment(studentId = student1Id, retakeId = pastRetake1Id, gradePlace = 4, gradeTeacher = 5, gradeOverall = 4, comment = "Хорошая атмосфера, преподаватель объяснял непонятные моменты. Вопросы были по билету, без сюрпризов.")
        // pastRetake2 (operations) — Анна получила 5, Даниил не сдал (нет оценки — нет комментария от него, но Анна оставила)
        insertComment(studentId = student2Id, retakeId = pastRetake2Id, gradePlace = 5, gradeTeacher = 5, gradeOverall = 5, comment = "Всё прошло отлично! Преподаватель была очень доброжелательна, задавала наводящие вопросы.")
        // pastRetake3 (db) — Даниил получил 3
        insertComment(studentId = student5Id, retakeId = pastRetake3Id, gradePlace = 3, gradeTeacher = 4, gradeOverall = 3, comment = "Аудитория маловата, душно. Преподаватель строгий, но справедливый. Лучше готовиться по индексам и транзакциям.")
    }

    private fun insertUser(role: UserRole, firstName: String, secondName: String, lastName: String, gender: String, age: Int, email: String, rawPassword: String): EntityID<Long> = UsersTable.insertAndGetId {
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
            it[StudentsTable.id] = userId
            it[StudentsTable.groupName] = groupName
        }
    }

    private fun insertTeacherProfile(userId: EntityID<Long>) {
        TeachersTable.insert {
            it[TeachersTable.id] = userId
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

    private fun insertStudentSubject(studentId: EntityID<Long>, subjectId: EntityID<Long>, status: StudentSubjectStatus, score: Int?, updatedAt: Long): EntityID<Long> =
        StudentSubjectsTable.insertAndGetId {
            it[StudentSubjectsTable.studentId] = studentId
            it[StudentSubjectsTable.subjectId] = subjectId
            it[StudentSubjectsTable.status] = status
            it[StudentSubjectsTable.score] = score
            it[StudentSubjectsTable.updatedAt] = updatedAt
        }

    private fun insertRetake(type: String, place: String, admission: String?, startAt: Long, endAt: Long, subjectId: Long): EntityID<Long> =
        RetakesTable.insertAndGetId {
            it[RetakesTable.type] = type
            it[RetakesTable.place] = place
            it[RetakesTable.subjectId] = subjectId
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

    private fun insertEnrollment(retakeId: EntityID<Long>, studentSubjectId: EntityID<Long>): EntityID<Long> =
        RetakeEnrollmentsTable.insertAndGetId {
            it[RetakeEnrollmentsTable.retakeId] = retakeId
            it[RetakeEnrollmentsTable.studentSubjectId] = studentSubjectId
            it[RetakeEnrollmentsTable.enrolledAt] = Instant.now().toEpochMilli()
        }

    private fun insertGrade(retakeId: EntityID<Long>, studentSubjectId: EntityID<Long>, score: Int, gradedAt: Long) {
        GradesTable.insert {
            it[GradesTable.retakeId] = retakeId
            it[GradesTable.studentSubjectId] = studentSubjectId
            it[GradesTable.score] = score
            it[GradesTable.gradedAt] = gradedAt
        }
    }

    private fun updateStudentSubjectAfterGrade(studentSubjectId: EntityID<Long>, score: Int, updatedAt: Long) {
        StudentSubjectsTable.update({ StudentSubjectsTable.id eq studentSubjectId }) {
            it[StudentSubjectsTable.score] = score
            it[StudentSubjectsTable.status] = StudentSubjectStatus.PASSED
            it[StudentSubjectsTable.updatedAt] = updatedAt
        }
    }

    private fun insertComment(studentId: EntityID<Long>, retakeId: EntityID<Long>, gradePlace: Int, gradeTeacher: Int, gradeOverall: Int, comment: String?) {
        CommentsTable.insert {
            it[CommentsTable.studentId]    = studentId
            it[CommentsTable.retakeId]     = retakeId
            it[CommentsTable.gradePlace]   = gradePlace
            it[CommentsTable.gradeTeacher] = gradeTeacher
            it[CommentsTable.gradeOverall] = gradeOverall
            it[CommentsTable.comment]      = comment
        }
    }
}