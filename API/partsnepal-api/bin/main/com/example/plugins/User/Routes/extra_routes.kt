package com.example.plugins.User.Routes


import com.example.plugins.TelegramUserService
import com.example.plugins.User.TelegramUser
import com.example.plugins.collection
import com.mongodb.client.model.Filters
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.serialization.Serializable
import org.litote.kmongo.coroutine.CoroutineCollection


fun Route.telegramUserExtraRoutes(telegramUserService: TelegramUserService) {




    get("/allUsers") {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal!!.payload.getClaim("userId").asInt()
        try {
            val allUsers = telegramUserService.readAllUsers()
            call.respond(allUsers)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Error fetching users: ${e.message}")

        }
    }

    get("/totalBalanceEntire") {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal!!.payload.getClaim("userId").asInt()
        try {
            val allUsers = telegramUserService.readTotalBalance()
            call.respond(allUsers)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Error fetching users: ${e.message}")
        }

    }


}

