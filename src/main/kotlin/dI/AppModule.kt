package com.example.dI


import Security.PasswordHasher
//import data.repository.UserRepositoryImpl
//import domain.repository.UserRepository
//import controller.AuthController
//import controller.PrizeController
//import controller.UserController
//import data.repository.PrizeRepositoryImpl
//import domain.repository.PrizeRepository
//import domain.usecases.AddFavoritePrizeUseCase
//import domain.usecases.GetCurrentUserUseCase
//import domain.usecases.GetPrizeUseCase
//import domain.usecases.GetUserFavoritesUseCase
//import domain.usecases.LoginUseCase
//import domain.usecases.RemoveFavoritePrizeUseCase
import kotlin.getValue

object AppContainer {
//    val prizeRepository: PrizeRepository by lazy { PrizeRepositoryImpl() }
//    val userRepository: UserRepository by lazy { UserRepositoryImpl() }
//    val loginUseCase: LoginUseCase by lazy { LoginUseCase(userRepository, PasswordHasher) }
//    val getPrizeUseCase: GetPrizeUseCase by lazy { GetPrizeUseCase(prizeRepository) }
//    val getCurrentUserUseCase: GetCurrentUserUseCase by lazy { GetCurrentUserUseCase(userRepository) }
//    val getUserFavoritesUseCase: GetUserFavoritesUseCase by lazy { GetUserFavoritesUseCase(userRepository) }
//    val addFavoritePrizeUseCase: AddFavoritePrizeUseCase by lazy { AddFavoritePrizeUseCase(userRepository) }
//    val removeFavoritePrizeUseCase: RemoveFavoritePrizeUseCase by lazy { RemoveFavoritePrizeUseCase(userRepository) }
//    val authController: AuthController by lazy { AuthController(loginUseCase) }
//    val prizeController: PrizeController by lazy { PrizeController(getPrizeUseCase) }
//    val userController: UserController by lazy { controller.UserController(getCurrentUserUseCase, getUserFavoritesUseCase, addFavoritePrizeUseCase, removeFavoritePrizeUseCase, getPrizeUseCase) }
}

fun appModule() {
    println("DI инициализирован")
}