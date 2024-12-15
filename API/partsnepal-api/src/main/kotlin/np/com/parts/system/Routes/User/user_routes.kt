package np.com.parts.system.Routes.User

import np.com.parts.system.Services.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import np.com.parts.system.Models.*

fun Route.authenticatedUserRoutes(userService: UserService) {
        route("/users") {
            // GET - Get current user's profile
            get("/profile") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    if (principal == null) {
                        call.respond(HttpStatusCode.Unauthorized, "Not authenticated")
                        return@get
                    }

                    val userId = UserId(principal.payload.getClaim("userId").asInt())
                    val user = userService.getUserById(userId)
                    
                    if (user != null) {
                        call.respond(HttpStatusCode.OK, user)
                    } else {
                        call.respond(HttpStatusCode.NotFound, "User not found")
                    }
                } catch (e: Exception) {
                    application.log.error("Error in /users/profile", e)
                    call.respond(HttpStatusCode.InternalServerError, "Error fetching user profile")
                }
            }

            // PUT - Update user profile
            put("/profile") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = UserId(principal!!.payload.getClaim("userId").asInt())
                    val updateRequest = call.receive<UpdateProfileRequest>()
                    
                    if (!updateRequest.isValid()) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse(
                                message = "Invalid update data",
                                code = "INVALID_DATA"
                            )
                        )
                        return@put
                    }

                    val updated = userService.updateProfile(userId, updateRequest)
                    if (updated) {
                        // Get updated user profile
                        val updatedUser = userService.getUserById(userId)
                        call.respond(HttpStatusCode.OK, updatedUser!!)
                    } else {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse(
                                message = "User not found",
                                code = "USER_NOT_FOUND"
                            )
                        )
                    }
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse(
                            message = "Error updating profile",
                            code = "UPDATE_ERROR"
                        )
                    )
                }
            }

            // POST - Add order to user history
            post("/orders") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = UserId(principal!!.payload.getClaim("userId").asInt())
                    val order = call.receive<OrderRef>()

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
                    val userId = UserId(principal!!.payload.getClaim("userId").asInt())
                    val preferences = call.receive<UserPreferences>()

                    val updated = userService.updatePreferences(userId, preferences)
                    if (updated) {
                        call.respond(HttpStatusCode.OK, "Preferences updated successfully")
                    } else {
                        call.respond(HttpStatusCode.NotFound, "User not found")
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error updating preferences")
                }
            }

            // POST - Add review to user's history
            post("/reviews") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = UserId(principal!!.payload.getClaim("userId").asInt())
                    val review = call.receive<ReviewRef>()

                    val added = userService.addReview(userId, review)
                    if (added) {
                        call.respond(HttpStatusCode.OK, "Review added successfully")
                    } else {
                        call.respond(HttpStatusCode.NotFound, "User not found")
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error adding review")
                }
            }

            // PUT - Update user preferences
            patch("/status") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = UserId(principal!!.payload.getClaim("userId").asInt())
                    val status = call.parameters["status"]
                        ?: return@patch call.respond(HttpStatusCode.BadRequest, "Status is required")

                    if (status=="ACTIVE"){
                        val updated = userService.updateAccountStatus(userId, status = AccountStatus.ACTIVE)
                        call.respond(HttpStatusCode.OK, "Updated Status, $updated")

                    } else {
                        call.respond(HttpStatusCode.BadRequest, "can only set to active")
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error updating preferences")
                }
            }



                get("/email") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    if (principal == null) {
                        call.respond(HttpStatusCode.Unauthorized, "Not authenticated")
                        return@get
                    }

                    val userId = UserId(principal.payload.getClaim("userId").asInt())
                    val user = userService.getFullUserById(userId)

                    if (user != null) {
                        call.respond(HttpStatusCode.OK, Creds(user.user.email!!, user.credentials.hashedPassword!!))
                    } else {
                        call.respond(HttpStatusCode.NotFound, "User not found")
                    }
                } catch (e: Exception) {
                    application.log.error("Error in GET /users/email", e)
                    call.respond(HttpStatusCode.InternalServerError, "Error fetching user email")
                }
            }


            get("/status") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    if (principal == null) {
                        call.respond(HttpStatusCode.Unauthorized, "Not authenticated")
                        return@get
                    }

                    val userId = UserId(principal.payload.getClaim("userId").asInt())
                    val status = userService.getUserAccountStatusById(userId)

                    if (status != null) {
                        call.respond(HttpStatusCode.OK, status)
                    } else {
                        call.respond(HttpStatusCode.NotFound, "User not found or some other error")
                    }
                } catch (e: Exception) {
                    application.log.error("Error in GET /users/email", e)
                    call.respond(HttpStatusCode.InternalServerError, "Error fetching user email")
                }
            }


    }
}
@Serializable
data class Creds(
    val email: Email,
    val cred: String,
)