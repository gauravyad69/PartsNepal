package com.example.plugins.User.Routes


import com.example.plugins.TelegramUserService
import com.example.plugins.User.Referral
import com.example.plugins.User.TapValue
import com.example.plugins.collection
import com.mongodb.client.model.Filters
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.telegramUserReferralRoutes(telegramUserService: TelegramUserService) {

    //referral routes, get and update using patch(only updates partially)
    route("/referral") {
        post {
            val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("userId").asString()
            val referral = call.receive<Referral>()
            val added = telegramUserService.addReferral(userId, referral)
            if (added) {
                call.respondText("Referral added successfully", status = HttpStatusCode.OK,)
            }
        }

// New route to get referrals
        get {
            val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("userId").asString()
            val  data= collection.find(Filters.eq("userInfo.userId", userId)).firstOrNull()!!.userInfo.referrals
            call.respond(HttpStatusCode.OK, data)

        }


        patch {
            val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("userId").asString()
            val value = call.receive<Referral>()
            val updated = telegramUserService.updateArrayField(userId, "userInfo","referrals", value)
            handleUpdateResponse(updated, call)
            print("referral updated")
        }








    }


}

