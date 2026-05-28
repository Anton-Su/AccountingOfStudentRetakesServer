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
        insertTeacherDiscipline(teacher1Id, "Конфигурационное управление")
        insertTeacherDiscipline(teacher1Id, "Мобильная разработка")
        insertTeacherDiscipline(teacher2Id, "Основы российской государственности")
        val configSubjectId = insertSubject("Конфигурационное управление")
        val mobileSubjectId = insertSubject("Мобильная разработка")
        val statehoodSubjectId = insertSubject("Основы российской государственности")
        // ЖУРНАЛ СТУДЕНТА
        val configStudentSubjectId = insertStudentSubject(
            studentId = studentId,
            subjectId = configSubjectId,
            status = StudentSubjectStatus.DEBT,
            score = null,
            updatedAt = 1_715_500_000_000
        )
        val statehoodStudentSubjectId = insertStudentSubject(
            studentId = studentId,
            subjectId = statehoodSubjectId,
            status = StudentSubjectStatus.DEBT,
            score = null,
            updatedAt = 1_715_586_400_000
        )
        insertStudentSubject(
            studentId = studentId,
            subjectId = mobileSubjectId,
            status = StudentSubjectStatus.OK,
            score = 4,
            updatedAt = 1_715_400_000_000
        )
        // РЕТЕЙКИ
        val retakeId = insertRetake(
            type = "Экзамен",
            place = "Ауд. 101",
            admission = "40 баллов допуска",
            startAt = 1_716_000_000_000,
            endAt = 1_716_086_400_000
        )
        val retake2Id = insertRetake(
            type = "Экзамен",
            place = "Ауд. 404",
            admission = "20 баллов допуска",
            startAt = 1_720_000_000_000,
            endAt = 1_720_086_400_000
        )
        linkRetakeTeacher(retakeId, teacher2Id)
        linkRetakeTeacher(retake2Id, teacher1Id)
        // ЗАПИСЬ НА ПЕРЕСДАЧУ
        insertEnrollment(
            retakeId = retakeId,
            studentSubjectId = configStudentSubjectId
        )
        insertEnrollment(
            retakeId = retake2Id,
            studentSubjectId = statehoodStudentSubjectId
        )

        // ОЦЕНКА
        val gradedAt = 1_716_000_100_000
        insertGrade(
            retakeId = retakeId,
            studentSubjectId = configStudentSubjectId,
            score = 4,
            gradedAt = gradedAt
        )
        // ОБНОВЛЕНИЕ ЖУРНАЛА
        updateStudentSubjectAfterGrade(
            studentSubjectId = configStudentSubjectId,
            score = 5,
            updatedAt = gradedAt
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