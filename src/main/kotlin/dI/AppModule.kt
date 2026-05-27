package dI


import security.PasswordHasher
import controller.AdminController
import controller.AuthController
import controller.StudentController
import controller.TeacherController
import data.repository.AdminRepositoryImpl
import data.repository.UserRepositoryImpl
import data.repository.StudentRepositoryImpl
import domain.repository.AdminRepository
import domain.repository.UserRepository
import domain.repository.StudentRepository
import domain.usecases.CreateRetakeUseCase
import domain.usecases.CancelRetakeEnrollmentUseCase
import domain.usecases.EnrollToRetakeUseCase
import domain.usecases.GradeStudentUseCase
import domain.usecases.GetRetakeDetailsUseCase
import domain.usecases.GetTeacherRetakesUseCase
import domain.usecases.GetStudentDebtsUseCase
import domain.usecases.CreateCommentUseCase
import domain.usecases.RedactRetakeUseCase
import domain.usecases.GetTeachersByDisciplineUseCase
import domain.usecases.LoginUseCase

object AppContainer {
    val userRepository: UserRepository by lazy { UserRepositoryImpl() }
    val studentRepository: StudentRepository by lazy { StudentRepositoryImpl() }
    val adminRepository: AdminRepository by lazy { AdminRepositoryImpl() }
    val loginUseCase: LoginUseCase by lazy { LoginUseCase(userRepository, PasswordHasher) }
    val getStudentDebtsUseCase: GetStudentDebtsUseCase by lazy { GetStudentDebtsUseCase(studentRepository) }
    val createCommentUseCase: CreateCommentUseCase by lazy { CreateCommentUseCase(studentRepository) }
    val enrollToRetakeUseCase: EnrollToRetakeUseCase by lazy { EnrollToRetakeUseCase(studentRepository) }
    val cancelRetakeEnrollmentUseCase: CancelRetakeEnrollmentUseCase by lazy { CancelRetakeEnrollmentUseCase(studentRepository) }
    val getTeachersByDisciplineUseCase: GetTeachersByDisciplineUseCase by lazy {
        GetTeachersByDisciplineUseCase(adminRepository)
    }
    val getTeacherRetakesUseCase: GetTeacherRetakesUseCase by lazy { GetTeacherRetakesUseCase(studentRepository) }
    val getRetakeDetailsUseCase: GetRetakeDetailsUseCase by lazy { GetRetakeDetailsUseCase(studentRepository) }
    val gradeStudentUseCase: GradeStudentUseCase by lazy { GradeStudentUseCase(studentRepository) }
    val createRetakeUseCase: CreateRetakeUseCase by lazy { CreateRetakeUseCase(adminRepository) }
    val redactRetakeUseCase: RedactRetakeUseCase by lazy { RedactRetakeUseCase(adminRepository) }
    val authController: AuthController by lazy { AuthController(loginUseCase) }
    val studentController: StudentController by lazy {
        StudentController(
            userRepository,
            getStudentDebtsUseCase,
            enrollToRetakeUseCase,
            cancelRetakeEnrollmentUseCase,
            createCommentUseCase
        )
    }
    val adminController: AdminController by lazy {
        AdminController(getTeachersByDisciplineUseCase, createRetakeUseCase, redactRetakeUseCase)
    }
    val teacherController: TeacherController by lazy {
        TeacherController(userRepository, studentRepository, getTeacherRetakesUseCase, getRetakeDetailsUseCase, gradeStudentUseCase)
    }
}

fun appModule() {
    println("DI инициализирован")
}