package com.example.plugins.User.Routes


import com.example.plugins.TelegramUserService
import com.example.plugins.User.Battery
import com.example.plugins.User.Level
import com.example.plugins.User.TapValue
import com.example.plugins.User.TimeRefill
import com.example.plugins.collection
import com.mongodb.client.model.Filters
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.telegramUserUpgradeInfoRoutes(telegramUserService: TelegramUserService) {


    //update routes, includes get and update using patch
    route("/level") {
        patch {
            val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("userId").asString()
            val level = call.receive<Level>()
            val updated = telegramUserService.updateUserField(userId, "upgradesInfo","level", level)
            handleUpdateResponse(updated, call)
        }
// New route to get referrals
        get {
            val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("userId").asString()
            val  data= collection.find(Filters.eq("userInfo.userId", userId)).firstOrNull()!!.upgradesInfo.level
            call.respond(HttpStatusCode.OK, data)

        }
    }




    ///tap value routes, includes read and update using patch
    route("/tap-value") {
        patch {
            val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("userId").asString()
            val tapValue = call.receive<TapValue>()
            val updated = telegramUserService.updateUserField(userId, "upgradesInfo","tapValue", tapValue)
            handleUpdateResponse(updated, call)
        }
// New route to get taps-value
        get {
            val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("userId").asString()

            val  data= collection.find(Filters.eq("userInfo.userId", userId)).firstOrNull()!!.upgradesInfo.tapValue
            call.respond(HttpStatusCode.OK, data)

        }
    }





    ///time-refill routes, includes read and update using patch
    route("/time-refill") {
        patch {
            val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("userId").asString()
            val data = call.receive<TimeRefill>()
            val updated = telegramUserService.updateUserField(userId, "upgradesInfo","timeRefill", data)
            handleUpdateResponse(updated, call)
        }

// New route to get taps-value
        get {
            val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("userId").asString()
            val  data= collection.find(Filters.eq("userInfo.userId", userId)).firstOrNull()!!.upgradesInfo.timeRefill
            call.respond(HttpStatusCode.OK, data)
        }
    }



    ///battery routes, includes read and update using patch
    route("/battery") {
        patch {
            val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("userId").asString()
            val data = call.receive<Battery>()
            val updated = telegramUserService.updateUserField(userId, "upgradesInfo","battery", data)
            handleUpdateResponse(updated, call)
        }

// New route to get taps-value
        get {
            val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("userId").asString()
            val  data= collection.find(Filters.eq("userInfo.userId", userId)).firstOrNull()!!.upgradesInfo.battery
            call.respond(HttpStatusCode.OK, data)

        }
    }



    //


}

