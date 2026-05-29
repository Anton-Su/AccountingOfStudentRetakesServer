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
    fun seed() {
        if (UsersTable.selectAll().any()) return
        insertUser(role = UserRole.ADMIN, firstName = "Александр", secondName = "Сергеевич", lastName = "Волков", gender = "male", age = 42, email = "volkov.a.s@edu.mirea.ru", rawPassword = "Admin123!")
        val teacher1Id = insertUser(role = UserRole.TEACHER, firstName = "Ирина", secondName = "Андреевна", lastName = "Кузнецова", gender = "female", age = 39, email = "kuznetsova.i.a@edu.mirea.ru", rawPassword = "Teacher123!")
        val teacher2Id = insertUser(role = UserRole.TEACHER, firstName = "Дмитрий", secondName = "Олегович", lastName = "Смирнов", gender = "male", age = 44, email = "smirnov.d.o@edu.mirea.ru", rawPassword = "Teacher123!")
        val teacher3Id = insertUser(role = UserRole.TEACHER, firstName = "Марина", secondName = "Викторовна", lastName = "Соколова", gender = "female", age = 37, email = "sokolova.m.v@edu.mirea.ru", rawPassword = "Teacher123!")
        val teacher4Id = insertUser(role = UserRole.TEACHER, firstName = "Алексей", secondName = "Павлович", lastName = "Морозов", gender = "male", age = 51, email = "morozov.a.p@edu.mirea.ru", rawPassword = "Teacher123!")
        insertTeacherProfile(teacher1Id)
        insertTeacherProfile(teacher2Id)
        insertTeacherProfile(teacher3Id)
        insertTeacherProfile(teacher4Id)
        insertTeacherDiscipline(teacher1Id, "Конфигурационное управление")
        insertTeacherDiscipline(teacher1Id, "Мобильная разработка")
        insertTeacherDiscipline(teacher2Id, "Основы российской государственности")
        insertTeacherDiscipline(teacher3Id, "Системное администрирование")
        insertTeacherDiscipline(teacher3Id, "Исследование операций")
        insertTeacherDiscipline(teacher4Id, "Базы данных")
        insertTeacherDiscipline(teacher4Id, "Архитектура программных систем")

        val configSubjectId = insertSubject("Конфигурационное управление")
        val mobileSubjectId = insertSubject("Мобильная разработка")
        val statehoodSubjectId = insertSubject("Основы российской государственности")
        val sysAdminSubjectId = insertSubject("Системное администрирование")
        val operationsSubjectId = insertSubject("Исследование операций")
        val dbSubjectId = insertSubject("Базы данных")
        val architectureSubjectId = insertSubject("Архитектура программных систем")

        val student1Id = insertUser(role = UserRole.STUDENT, firstName = "Максим", secondName = "Игоревич", lastName = "Петров", gender = "male", age = 20, email = "petrov.m.i@edu.mirea.ru", rawPassword = "Student123!")
        val student2Id = insertUser(role = UserRole.STUDENT, firstName = "Анна", secondName = "Сергеевна", lastName = "Орлова", gender = "female", age = 19, email = "orlova.a.s@edu.mirea.ru", rawPassword = "Student123!")
        val student3Id = insertUser(role = UserRole.STUDENT, firstName = "Кирилл", secondName = "Андреевич", lastName = "Васильев", gender = "male", age = 21, email = "vasilev.k.a@edu.mirea.ru", rawPassword = "Student123!")
        val student4Id = insertUser(role = UserRole.STUDENT, firstName = "Екатерина", secondName = "Олеговна", lastName = "Фролова", gender = "female", age = 20, email = "frolova.e.o@edu.mirea.ru", rawPassword = "Student123!")
        val student5Id = insertUser(role = UserRole.STUDENT, firstName = "Даниил", secondName = "Максимович", lastName = "Лебедев", gender = "male", age = 22, email = "lebedev.d.m@edu.mirea.ru", rawPassword = "Student123!")
        insertStudentProfile(student1Id, "ИКБО-61-23")
        insertStudentProfile(student2Id, "ИКБО-61-23")
        insertStudentProfile(student3Id, "ИКБО-62-23")
        insertStudentProfile(student4Id, "ИКБО-62-23")
        insertStudentProfile(student5Id, "ИКБО-63-23")

        // Максим: долги по statehood и config, сдал mobile и db
        val maximStatehoodId = insertStudentSubject(studentId = student1Id, subjectId = statehoodSubjectId, status = StudentSubjectStatus.DEBT, score = null, updatedAt = 1_715_586_400_000)
        val maximConfigId = insertStudentSubject(studentId = student1Id, subjectId = configSubjectId, status = StudentSubjectStatus.DEBT, score = null, updatedAt = 1_715_500_000_000)
        insertStudentSubject(studentId = student1Id, subjectId = mobileSubjectId, status = StudentSubjectStatus.OK, score = 4, updatedAt = 1_715_400_000_000)
        insertStudentSubject(studentId = student1Id, subjectId = dbSubjectId, status = StudentSubjectStatus.OK, score = 5, updatedAt = 1_715_300_000_000)

        // Анна: долги по operations и sysAdmin, сдала mobile
        val annaOperationsId = insertStudentSubject(studentId = student2Id, subjectId = operationsSubjectId, status = StudentSubjectStatus.DEBT, score = null, updatedAt = 1_715_800_000_000)
        val annaSysAdminId = insertStudentSubject(studentId = student2Id, subjectId = sysAdminSubjectId, status = StudentSubjectStatus.DEBT, score = null, updatedAt = 1_715_900_000_000)
        insertStudentSubject(studentId = student2Id, subjectId = mobileSubjectId, status = StudentSubjectStatus.OK, score = 5, updatedAt = 1_715_400_000_000)

        // Кирилл: долг по architecture, сдал db и sysAdmin
        val kirillArchitectureId = insertStudentSubject(studentId = student3Id, subjectId = architectureSubjectId, status = StudentSubjectStatus.DEBT, score = null, updatedAt = 1_716_000_000_000)
        insertStudentSubject(studentId = student3Id, subjectId = dbSubjectId, status = StudentSubjectStatus.OK, score = 3, updatedAt = 1_715_000_000_000)
        insertStudentSubject(studentId = student3Id, subjectId = sysAdminSubjectId, status = StudentSubjectStatus.OK, score = 4, updatedAt = 1_715_100_000_000)

        // Екатерина: всё сдала
        insertStudentSubject(studentId = student4Id, subjectId = configSubjectId, status = StudentSubjectStatus.OK, score = 5, updatedAt = 1_714_000_000_000)
        insertStudentSubject(studentId = student4Id, subjectId = mobileSubjectId, status = StudentSubjectStatus.OK, score = 5, updatedAt = 1_714_100_000_000)
        insertStudentSubject(studentId = student4Id, subjectId = operationsSubjectId, status = StudentSubjectStatus.OK, score = 4, updatedAt = 1_714_200_000_000)

        // Даниил: долги по db, config, operations
        val daniilDbId = insertStudentSubject(studentId = student5Id, subjectId = dbSubjectId, status = StudentSubjectStatus.DEBT, score = null, updatedAt = 1_717_000_000_000)
        val daniilConfigId = insertStudentSubject(studentId = student5Id, subjectId = configSubjectId, status = StudentSubjectStatus.DEBT, score = null, updatedAt = 1_717_100_000_000)
        val daniilOperationsId = insertStudentSubject(studentId = student5Id, subjectId = operationsSubjectId, status = StudentSubjectStatus.DEBT, score = null, updatedAt = 1_717_200_000_000)

        // Пересдачи:
        // retake1 — statehood (teacher2)
        // retake2 — config (teacher1)
        // retake3 — operations (teacher3)
        // retake4 — db (teacher4)
        val retake1Id = insertRetake(type = "Экзамен", place = "Ауд. 101", admission = "40 баллов допуска", startAt = 1_716_000_000_000, endAt = 1_716_086_400_000, subjectId = statehoodSubjectId.value)
        val retake2Id = insertRetake(type = "Экзамен", place = "Ауд. 404", admission = "20 баллов допуска", startAt = 1_720_000_000_000, endAt = 1_720_086_400_000, subjectId = configSubjectId.value)
        val retake3Id = insertRetake(type = "Зачёт", place = "Ауд. 212", admission = "Лабораторные должны быть сданы", startAt = 1_721_000_000_000, endAt = 1_721_050_000_000, subjectId = operationsSubjectId.value)
        val retake4Id = insertRetake(type = "Экзамен", place = "Ауд. 505", admission = "Минимум 30 баллов", startAt = 1_722_000_000_000, endAt = 1_722_086_400_000, subjectId = dbSubjectId.value)

        linkRetakeTeacher(retake1Id, teacher2Id)
        linkRetakeTeacher(retake2Id, teacher1Id)
        linkRetakeTeacher(retake3Id, teacher3Id)
        linkRetakeTeacher(retake4Id, teacher4Id)

        // Записи на пересдачи (retake.subjectId == studentSubject.subjectId):
        // Максим: на retake1(statehood) → maximStatehoodId, на retake2(config) → maximConfigId
        insertEnrollment(retakeId = retake1Id, studentSubjectId = maximStatehoodId)
        insertEnrollment(retakeId = retake2Id, studentSubjectId = maximConfigId)
        // Анна: на retake3(operations) → annaOperationsId, долг по sysAdmin — пересдачи нет
        insertEnrollment(retakeId = retake3Id, studentSubjectId = annaOperationsId)
        // Кирилл: долг по architecture — пересдачи нет, на retake4(db) записать нельзя (db сдан)
        // Даниил: на retake4(db) → daniilDbId, на retake2(config) → daniilConfigId, на retake3(operations) → daniilOperationsId
        insertEnrollment(retakeId = retake4Id, studentSubjectId = daniilDbId)
        insertEnrollment(retakeId = retake2Id, studentSubjectId = daniilConfigId)
        insertEnrollment(retakeId = retake3Id, studentSubjectId = daniilOperationsId)

        // Оценки: Максим сдал statehood на 4, Анна сдала operations на 5
        val gradedAt1 = 1_716_000_100_000
        insertGrade(retakeId = retake1Id, studentSubjectId = maximStatehoodId, score = 4, gradedAt = gradedAt1)
        updateStudentSubjectAfterGrade(studentSubjectId = maximStatehoodId, score = 4, updatedAt = gradedAt1)

        val gradedAt2 = 1_721_000_100_000
        insertGrade(retakeId = retake3Id, studentSubjectId = annaOperationsId, score = 5, gradedAt = gradedAt2)
        updateStudentSubjectAfterGrade(studentSubjectId = annaOperationsId, score = 5, updatedAt = gradedAt2)
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

    private fun insertStudentSubject(
        studentId: EntityID<Long>,
        subjectId: EntityID<Long>,
        status: StudentSubjectStatus,
        score: Int?,
        updatedAt: Long
    ): EntityID<Long> =
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
}