package dI


import security.PasswordHasher
import controller.AdminController
import controller.AuthController
import controller.UserController
import controller.StudentController
import controller.TeacherController
import data.repository.AdminRepositoryImpl
import data.repository.UserRepositoryImpl
import data.repository.StudentRepositoryImpl
import data.repository.TeacherRepositoryImpl
import domain.repository.AdminRepository
import domain.repository.UserRepository
import domain.repository.StudentRepository
import domain.repository.TeacherRepository
import domain.usecases.CreateRetakeUseCase
import domain.usecases.CancelRetakeEnrollmentUseCase
import domain.usecases.EnrollToRetakeUseCase
import domain.usecases.GradeStudentUseCase
import domain.usecases.GetRetakeDetailsUseCase
import domain.usecases.GetTeacherRetakesUseCase
import domain.usecases.GetStudentDebtsUseCase
import domain.usecases.CreateCommentUseCase
import domain.usecases.DeleteRetakeUseCase
import domain.usecases.GetAllCommentsUseCase
import domain.usecases.GetAllRetakesUseCase
import domain.usecases.GetStudentDebtRankUseCase
import domain.usecases.RedactRetakeUseCase
import domain.usecases.GetSubjectsUseCase
import domain.usecases.GetTeachersByDisciplineUseCase
import domain.usecases.LoginUseCase

object AppContainer {
    val userRepository: UserRepository by lazy { UserRepositoryImpl() }
    val studentRepository: StudentRepository by lazy { StudentRepositoryImpl() }
    val adminRepository: AdminRepository by lazy { AdminRepositoryImpl() }
    val teacherRepository: TeacherRepository by lazy { TeacherRepositoryImpl() }
    val loginUseCase: LoginUseCase by lazy { LoginUseCase(userRepository, PasswordHasher) }
    val getStudentDebtsUseCase: GetStudentDebtsUseCase by lazy { GetStudentDebtsUseCase(studentRepository) }
    val createCommentUseCase: CreateCommentUseCase by lazy { CreateCommentUseCase(studentRepository) }
    val getStudentDebtRankUseCase: GetStudentDebtRankUseCase by lazy { GetStudentDebtRankUseCase(studentRepository) }
    val enrollToRetakeUseCase: EnrollToRetakeUseCase by lazy { EnrollToRetakeUseCase(studentRepository) }
    val cancelRetakeEnrollmentUseCase: CancelRetakeEnrollmentUseCase by lazy { CancelRetakeEnrollmentUseCase(studentRepository) }
    val getTeachersByDisciplineUseCase: GetTeachersByDisciplineUseCase by lazy {
        GetTeachersByDisciplineUseCase(adminRepository)
    }
    val getAllRetakesUseCase: GetAllRetakesUseCase by lazy { GetAllRetakesUseCase(adminRepository) }
    val getAllCommentsUseCase: GetAllCommentsUseCase by lazy {GetAllCommentsUseCase(adminRepository)}
    val deleteRetakeUseCase: DeleteRetakeUseCase by lazy { DeleteRetakeUseCase(adminRepository)}
    val getSubjectsUseCase: GetSubjectsUseCase by lazy { GetSubjectsUseCase(adminRepository) }
    val getTeacherRetakesUseCase: GetTeacherRetakesUseCase by lazy { GetTeacherRetakesUseCase(teacherRepository) }
    val getRetakeDetailsUseCase: GetRetakeDetailsUseCase by lazy { GetRetakeDetailsUseCase(studentRepository, teacherRepository) }
    val gradeStudentUseCase: GradeStudentUseCase by lazy { GradeStudentUseCase(teacherRepository) }
    val createRetakeUseCase: CreateRetakeUseCase by lazy { CreateRetakeUseCase(adminRepository) }
    val redactRetakeUseCase: RedactRetakeUseCase by lazy { RedactRetakeUseCase(adminRepository) }
    val authController: AuthController by lazy { AuthController(loginUseCase) }
    val userController: UserController by lazy { UserController(userRepository) }
    val studentController: StudentController by lazy {
        StudentController(userRepository, getStudentDebtsUseCase, enrollToRetakeUseCase, cancelRetakeEnrollmentUseCase, createCommentUseCase, getStudentDebtRankUseCase)
    }
    val adminController: AdminController by lazy {
        AdminController(getTeachersByDisciplineUseCase, getSubjectsUseCase, createRetakeUseCase, redactRetakeUseCase, getAllCommentsUseCase, getAllRetakesUseCase, deleteRetakeUseCase)
    }
    val teacherController: TeacherController by lazy {
        TeacherController(userRepository, studentRepository, getTeacherRetakesUseCase, getRetakeDetailsUseCase, gradeStudentUseCase)
    }
}

fun appModule() {
    println("DI инициализирован")
}