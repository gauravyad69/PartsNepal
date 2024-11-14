package com.example

import com.example.plugins.*
import com.example.plugins.User.telegramAuthUserRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSockets()
    configureSerialization()
    configureHTTP()
    configureDatabases()
    configureMonitoring()
    configureSecurity()
    configureRouting()
    val connection=connectToMongoDB()
    val telegramUserService = TelegramUserService(connection)

    routing {
    telegramAuthUserRoutes(telegramUserService)
    }
}

