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
import kotlinx.serialization.Serializable
import java.math.BigInteger

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
                    print("nigga, $user, userfromjwt :$userId")
                    if (user != null) {
                        call.respond(HttpStatusCode.OK, user)
                    } else {
                        call.respondText("User not found", status = HttpStatusCode.NotFound)
                    }
            }

            // POST - Create a new user, also automatically handles the referee username update
            post {
                // Retrieve JWT principal for authentication
                val principal = call.principal<JWTPrincipal>()

                // Extract user ID from JWT payload
                val userId = principal!!.payload.getClaim("userId").asString()

                // Receive and deserialize the incoming request body as a TelegramUser object
                val user = call.receive<TelegramUser>()

                // Verify that the user ID in the request matches the authenticated user's ID
                if (user.userInfo.userId == userId) {
                    // Create a new user in the database
                    val createdUser = telegramUserService.create(user)

                    // Send the created user object back to the client
                    call.respond(
                        HttpStatusCode.Created,
                        createdUser
                    )
                } else {
                    // Return an error response if the user ID doesn't match
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        "User ID in the request doesn't match the authenticated user's ID."
                    )
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
                val userId = call.parameters["userId"]?.toString() ?: return@delete call.respondText(
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