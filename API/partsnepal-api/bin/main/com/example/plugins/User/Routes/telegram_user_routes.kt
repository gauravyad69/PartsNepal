package com.example.plugins.User.Routes

import com.example.plugins.TelegramUserService
import com.example.plugins.User.TelegramUser
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class UserIdAndUsername(
    val userId: String,
    val username: String
)
fun Route.telegramUserRoutes(telegramUserService: TelegramUserService) {


        route("/telegramUser") {
            // GET - Retrieve a user by session
            get {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("userId").asString()


                val user = telegramUserService.readByUserId(userId)
                    print("nigga, $user")
                    if (user != null) {
                        call.respond(HttpStatusCode.OK, user)
                    } else {
                        call.respondText("User not found", status = HttpStatusCode.NotFound)
                    }
            }

            // POST - Create a new user
            post {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("userId").asString()

                val user = call.receive<TelegramUser>()
                if (user.userInfo.userId == userId) {
                val createdUser = telegramUserService.create(user)
                call.respondText("User created successfully. ID: $createdUser", status = HttpStatusCode.Created)
                }
            }

            // PUT - Update an existing user
            patch {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("userId").asString()

                val user = call.receive<TelegramUser>()
                if (user.userInfo.userId == userId) {
                    val updatedUser = telegramUserService.updateByUserId(userId, user)
                    if (updatedUser != null) {
                        call.respondText("User updated successfully", status = HttpStatusCode.OK)
                    } else {
                        call.respondText("User not found", status = HttpStatusCode.NotFound)
                    }
                }
            }




            // DELETE - Delete a user //todo make this admin only
            delete("/{userId}") {
                val userId = call.parameters["userId"] ?: return@delete call.respondText(
                    "Missing or malformed userId",
                    status = HttpStatusCode.BadRequest
                )

                val deletedUser = telegramUserService.deleteByUserId(userId)
                if (deletedUser != null) {
                    call.respondText("User deleted successfully", status = HttpStatusCode.OK)
                } else {
                    call.respondText("User not found", status = HttpStatusCode.NotFound)
                }
            }
        }


}