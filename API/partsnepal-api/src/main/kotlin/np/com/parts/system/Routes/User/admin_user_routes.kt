package np.com.parts.system.Routes.User


import np.com.parts.system.Services.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import np.com.parts.system.Models.AccountStatus
import np.com.parts.system.Models.UserId

fun Route.adminUserRoutes(userService: UserService) {

        // Admin-only routes
            // GET - Get all users (with pagination)
            get {
                try {
                    val skip = call.parameters["skip"]?.toIntOrNull() ?: 0
                    val limit = call.parameters["limit"]?.toIntOrNull() ?: 50

                    val users = userService.getAllUsers(skip, limit)
                    call.respond(HttpStatusCode.OK, users)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error fetching users")
                }
            }

            // GET - Get user by ID
            get("/{userId}") {
                try {
                    val userId = UserId(
                        call.parameters["userId"]?.toIntOrNull() 
                            ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid user ID")
                    )

                    val user = userService.getUserById(userId)
                    if (user != null) {
                        call.respond(HttpStatusCode.OK, user)
                    } else {
                        call.respond(HttpStatusCode.NotFound, "system not found")
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error fetching user")
                }
            }

            // PUT - Update account status
            put("/{userId}/status") {
                try {
                    val userId = UserId(
                        call.parameters["userId"]?.toIntOrNull()
                            ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid user ID")
                    )
                    val status = AccountStatus.valueOf(call.receive<String>())

                    val updated = userService.updateAccountStatus(userId, status)
                    if (updated) {
                        call.respond(HttpStatusCode.OK, "Status updated successfully")
                    } else {
                        call.respond(HttpStatusCode.NotFound, "system not found")
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error updating status")
                }
            }

            // DELETE - Delete user
            delete("/{userId}") {
                try {
                    val userId = UserId(
                        call.parameters["userId"]?.toIntOrNull()
                            ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid user ID")
                    )

                    val deleted = userService.deleteUser(userId)
                    if (deleted) {
                        call.respond(HttpStatusCode.OK, "system deleted successfully")
                    } else {
                        call.respond(HttpStatusCode.NotFound, "system not found")
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error deleting user")
                }
            }

            // GET - Get business accounts
//            get("/business") {
//                try {
//                    val businessUsers = userService.getBusinessAccounts()
//                    call.respond(HttpStatusCode.OK, businessUsers)
//                } catch (e: Exception) {
//                    call.respond(HttpStatusCode.InternalServerError, "Error fetching business accounts")
//                }
//            }

}