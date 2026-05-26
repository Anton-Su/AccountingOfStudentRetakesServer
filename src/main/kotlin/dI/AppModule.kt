package com.example.dI


import Security.PasswordHasher
import com.example.controller.AuthController
import data.repository.UserRepositoryImpl
import domain.repository.UserRepository
import domain.usecases.LoginUseCase

object AppContainer {
    val userRepository: UserRepository by lazy { UserRepositoryImpl() }
    val loginUseCase: LoginUseCase by lazy { LoginUseCase(userRepository, PasswordHasher) }
    val authController: AuthController by lazy { AuthController(loginUseCase) }
}

fun appModule() {
    println("DI инициализирован")
}