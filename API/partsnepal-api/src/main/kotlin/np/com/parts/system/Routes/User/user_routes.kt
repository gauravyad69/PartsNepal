package np.com.parts.system.Routes.User

import np.com.parts.system.Services.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import np.com.parts.system.Models.OrderModel

fun Route.authenticatedUserRoutes(userService: UserService) {
        route("/users") {
            // GET - Get current user's profile
            get("/profile") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal!!.payload.getClaim("userId").asString().toInt()

                    val user = userService.getUserById(userId)
                    if (user != null) {
                        call.respond(HttpStatusCode.OK, user)
                    } else {
                        call.respond(HttpStatusCode.NotFound, "system not found")
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error fetching user profile")
                }
            }

            // PUT - Update user profile
            put("/profile") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal!!.payload.getClaim("userId").asString().toInt()
                    val updates = call.receive<Map<String, Any>>()

                    val updated = userService.updateUserFields(userId, updates)
                    if (updated) {
                        call.respond(HttpStatusCode.OK, "Profile updated successfully")
                    } else {
                        call.respond(HttpStatusCode.NotFound, "system not found")
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error updating profile")
                }
            }

            // POST - Add order to user history
            post("/orders") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal!!.payload.getClaim("userId").asString().toInt()
                    val order = call.receive<OrderModel>()

                    val added = userService.addOrder(userId, order)
                    if (added) {
                        call.respond(HttpStatusCode.OK, "Order added successfully")
                    } else {
                        call.respond(HttpStatusCode.NotFound, "system not found")
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error adding order")
                }
            }

            // PUT - Update user preferences
            put("/preferences") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal!!.payload.getClaim("userId").asString().toInt()
                    val preferences = call.receive<Map<String, String>>()

                    val updated = userService.updatePreferences(userId, preferences)
                    if (updated) {
                        call.respond(HttpStatusCode.OK, "Preferences updated successfully")
                    } else {
                        call.respond(HttpStatusCode.NotFound, "system not found")
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error updating preferences")
                }
            }

            // POST - Add review to user's history
            post("/reviews") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal!!.payload.getClaim("userId").asString().toInt()
                    val review = call.receive<String>()

                    val added = userService.addReview(userId, review)
                    if (added) {
                        call.respond(HttpStatusCode.OK, "Review added successfully")
                    } else {
                        call.respond(HttpStatusCode.NotFound, "system not found")
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error adding review")
                }
            }

    }
}