package com.example.dI


import security.PasswordHasher
import controller.AdminController
import controller.AuthController
import controller.StudentController
import data.repository.AdminRepositoryImpl
import data.repository.UserRepositoryImpl
import domain.repository.AdminRepository
import domain.repository.UserRepository
import domain.usecases.CreateRetakeUseCase
import domain.usecases.CancelRetakeEnrollmentUseCase
import domain.usecases.EnrollToRetakeUseCase
import domain.usecases.GetStudentDebtsUseCase
import domain.usecases.RedactRetakeUseCase
import domain.usecases.GetTeachersByDisciplineUseCase
import domain.usecases.LoginUseCase
import data.repository.StudentRepositoryImpl
import domain.repository.StudentRepository

object AppContainer {
    val userRepository: UserRepository by lazy { UserRepositoryImpl() }
    val studentRepository: StudentRepository by lazy { StudentRepositoryImpl() }
    val adminRepository: AdminRepository by lazy { AdminRepositoryImpl() }
    val loginUseCase: LoginUseCase by lazy { LoginUseCase(userRepository, PasswordHasher) }
    val getStudentDebtsUseCase: GetStudentDebtsUseCase by lazy { GetStudentDebtsUseCase(studentRepository) }
    val enrollToRetakeUseCase: EnrollToRetakeUseCase by lazy { EnrollToRetakeUseCase(studentRepository) }
    val cancelRetakeEnrollmentUseCase: CancelRetakeEnrollmentUseCase by lazy { CancelRetakeEnrollmentUseCase(studentRepository) }
    val getTeachersByDisciplineUseCase: GetTeachersByDisciplineUseCase by lazy {
        GetTeachersByDisciplineUseCase(adminRepository)
    }
    val createRetakeUseCase: CreateRetakeUseCase by lazy { CreateRetakeUseCase(adminRepository) }
    val redactRetakeUseCase: RedactRetakeUseCase by lazy { RedactRetakeUseCase(adminRepository) }
    val authController: AuthController by lazy { AuthController(loginUseCase) }
    val studentController: StudentController by lazy {
        StudentController(userRepository, getStudentDebtsUseCase, enrollToRetakeUseCase, cancelRetakeEnrollmentUseCase)
    }
    val adminController: AdminController by lazy {
        AdminController(getTeachersByDisciplineUseCase, createRetakeUseCase, redactRetakeUseCase)
    }
}

fun appModule() {
    println("DI инициализирован")
}