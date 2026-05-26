package com.example.dI


import security.PasswordHasher
import controller.AdminController
import controller.AuthController
import data.repository.AdminRepositoryImpl
import data.repository.UserRepositoryImpl
import domain.repository.AdminRepository
import domain.repository.UserRepository
import domain.usecases.CreateRetakeUseCase
import domain.usecases.RedactRetakeUseCase
import domain.usecases.GetTeachersByDisciplineUseCase
import domain.usecases.LoginUseCase

object AppContainer {
    val userRepository: UserRepository by lazy { UserRepositoryImpl() }
    val adminRepository: AdminRepository by lazy { AdminRepositoryImpl() }
    val loginUseCase: LoginUseCase by lazy { LoginUseCase(userRepository, PasswordHasher) }
    val getTeachersByDisciplineUseCase: GetTeachersByDisciplineUseCase by lazy {
        GetTeachersByDisciplineUseCase(adminRepository)
    }
    val createRetakeUseCase: CreateRetakeUseCase by lazy { CreateRetakeUseCase(adminRepository) }
    val redactRetakeUseCase: RedactRetakeUseCase by lazy { RedactRetakeUseCase(adminRepository) }
    val authController: AuthController by lazy { AuthController(loginUseCase) }
    val adminController: AdminController by lazy {
        AdminController(getTeachersByDisciplineUseCase, createRetakeUseCase, redactRetakeUseCase)
    }
}

fun appModule() {
    println("DI инициализирован")
}