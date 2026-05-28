package com.example


import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import dI.appModule
import data.databases.DatabaseFactory
import plugins.configureAuthentication
import plugins.configureCallLogging
import plugins.configureContentNegotiation
import plugins.configureStatusPages

fun main() {
    embeddedServer(Netty, port = 8080, host = "127.0.0.1") {
        module()
    }.start(wait = true)
}

fun Application.module() {
    DatabaseFactory.init()
    appModule()
    configureContentNegotiation()
    configureCallLogging()
    configureStatusPages()
    configureAuthentication()
    configureRouting()
}
