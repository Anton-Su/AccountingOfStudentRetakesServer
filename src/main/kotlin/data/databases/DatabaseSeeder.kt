package data.databases

import domain.model.DebtStatus
import domain.model.UserRole
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import security.PasswordHasher

object DatabaseSeeder {
    fun seed() {
        if (UsersTable.selectAll().any()) return
        val adminId = insertUser(
            role = UserRole.ADMIN,
            firstName = "Admin",
            secondName = "System",
            lastName = "User",
            gender = "unknown",
            age = 30,
            email = "admin@example.com",
            rawPassword = "Admin123!"
        )
        val teacher1Id = insertUser(
            role = UserRole.TEACHER,
            firstName = "Teacher",
            secondName = "Math",
            lastName = "One",
            gender = "unknown",
            age = 35,
            email = "teacher1@example.com",
            rawPassword = "Teacher123!"
        )
        val teacher2Id = insertUser(
            role = UserRole.TEACHER,
            firstName = "Teacher",
            secondName = "Physics",
            lastName = "Two",
            gender = "unknown",
            age = 38,
            email = "teacher2@example.com",
            rawPassword = "Teacher123!"
        )
        val studentId = insertUser(
            role = UserRole.STUDENT,
            firstName = "Student",
            secondName = "Default",
            lastName = "User",
            gender = "unknown",
            age = 20,
            email = "student@example.com",
            rawPassword = "Student123!"
        )
        insertStudentProfile(studentId, "CS-101")
        insertTeacherProfile(teacher1Id)
        insertTeacherProfile(teacher2Id)
        val mathSubjectId = insertSubject("Math")
        val physicsSubjectId = insertSubject("Physics")
        val programmingSubjectId = insertSubject("Programming")
        val mathDebtId = insertDebt(
            studentId = studentId,
            subjectId = mathSubjectId,
            teacherId = teacher1Id,
            createdAt = 1_715_500_000_000,
            status = DebtStatus.ACTIVE
        )
        val physicsDebtId = insertDebt(
            studentId = studentId,
            subjectId = physicsSubjectId,
            teacherId = teacher2Id,
            createdAt = 1_715_586_400_000,
            status = DebtStatus.ACTIVE
        )
        val retakeId = insertRetake(
            type = "Exam retake",
            admission = "Winter 2026",
            startAt = 1_716_000_000_000,
            endAt = 1_716_086_400_000
        )
        linkRetakeTeacher(retakeId, teacher1Id)
        linkRetakeTeacher(retakeId, teacher2Id)
        val enrollmentId = insertEnrollment(
            retakeId = retakeId,
            studentId = studentId,
            debtId = mathDebtId,
            score = 85
        )
        val gradedAt = 1_716_000_100_000
        insertGrade(retakeId = retakeId, studentId = studentId, score = 85, gradedAt = gradedAt)
        insertSubjectStudent(
            studentId = studentId,
            subjectId = mathSubjectId,
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

    private fun insertRetake(type: String, admission: String?, startAt: Long, endAt: Long): EntityID<Long> =
        RetakesTable.insertAndGetId {
            it[RetakesTable.type] = type
            it[RetakesTable.admission] = admission
            it[RetakesTable.startAt] = startAt
            it[RetakesTable.endAt] = endAt
        }

    private fun linkRetakeTeacher(retakeId: EntityID<Long>, teacherId: EntityID<Long>) {
        RetakeTeachersTable.insert {
            it[RetakeTeachersTable.retakeId] = retakeId
            it[RetakeTeachersTable.teacherId] = teacherId
        }
    }

    private fun insertEnrollment(retakeId: EntityID<Long>, studentId: EntityID<Long>, debtId: EntityID<Long>, score: Int?): EntityID<Long> =
        RetakeEnrollmentsTable.insertAndGetId {
            it[RetakeEnrollmentsTable.retakeId] = retakeId
            it[RetakeEnrollmentsTable.studentId] = studentId
            it[RetakeEnrollmentsTable.debtId] = debtId
            it[RetakeEnrollmentsTable.score] = score
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