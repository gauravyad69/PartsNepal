package com.example.plugins.User.Routes


import com.example.plugins.TelegramUserService
import com.example.plugins.User.BalanceInfo
import com.example.plugins.collection
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.telegramUserBalanceInfoRoutes(telegramUserService: TelegramUserService) {


    //universal update to balanceInfo

    ///battery routes, includes read and update using patch
    route("/balanceInfo") {
        patch {
            val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("userId").asString()
            val data = call.receive<BalanceInfo>()

            val filter = Filters.eq("userInfo.userId", userId)
            val update = Updates.set("balanceInfo", data)
            val result = collection.updateOne(filter, update)
            result.modifiedCount > 0

            call.respond(HttpStatusCode.OK)

        }

// New route to get taps-value
        get {
            val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("userId").asString()
            val  data= collection.find(Filters.eq("userInfo.userId", userId)).firstOrNull()!!.balanceInfo
            call.respond(HttpStatusCode.OK, data)
        }

    }




    ///battery routes, includes read and update using patch
    route("/totalBalance") {
        patch {
            val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("userId").asString()
            val data = call.receive<BalanceInfo>()
            val updated = telegramUserService.updateUserField(userId, "balanceInfo","totalBalance", data.totalBalance)
            handleUpdateResponse(updated, call)
        }

// New route to get taps-value
        get {
            val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("userId").asString()
            val battery = telegramUserService.readUserField(userId, "totalBalance")
            call.respond(HttpStatusCode.OK, battery!!)

        }
    }

    ///battery routes, includes read and update using patch
    route("/balance") {
        patch {
            val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("userId").asString()
            val data = call.receive<BalanceInfo>()
            val updated = telegramUserService.updateUserField(userId, "balanceInfo","balance", data.balance)
            handleUpdateResponse(updated, call)
        }

// New route to get taps-value
        get {
            val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("userId").asString()
            val  data= collection.find(Filters.eq("userInfo.userId", userId)).firstOrNull()!!.balanceInfo.balance
            call.respond(HttpStatusCode.OK, data)

        }
    }


    ///battery routes, includes read and update using patch
    route("/energy") {
        patch {
            val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("userId").asString()
            val data = call.receive<BalanceInfo>()
            val updated = telegramUserService.updateUserField(userId, "balanceInfo","energy", data.energy)
            handleUpdateResponse(updated, call)
        }

// New route to get taps-value
        get {
            val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("userId").asString()
            val  data= collection.find(Filters.eq("userInfo.userId", userId)).firstOrNull()!!.balanceInfo.energy
            call.respond(HttpStatusCode.OK, data)

        }
    }

    ///battery routes, includes read and update using patch
    route("/tapBalance") {
        patch {
            val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("userId").asString()
            val data = call.receive<BalanceInfo>()
            val updated = telegramUserService.updateUserField(userId, "balanceInfo","tapBalance", data.tapBalance)
            handleUpdateResponse(updated, call)
        }

// New route to get taps-value
        get {
            val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("userId").asString()
            val  data= collection.find(Filters.eq("userInfo.userId", userId)).firstOrNull()!!.balanceInfo.tapBalance
            call.respond(HttpStatusCode.OK, data)

        }
    }



    //


}

