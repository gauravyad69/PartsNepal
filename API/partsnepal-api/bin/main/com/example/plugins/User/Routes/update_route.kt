package com.example.plugins.User.Routes

import io.ktor.server.application.*
import com.example.plugins.TelegramUserService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.telegramUserUpdateRoutes(telegramUserService: TelegramUserService) {

}

suspend fun handleUpdateResponse(updated: Boolean, call: ApplicationCall) {
    if (updated) {
        call.respondText("Field updated successfully", status = HttpStatusCode.OK)
    } else {
        call.respondText("User not found or field not updated", status = HttpStatusCode.NotFound)
    }
}