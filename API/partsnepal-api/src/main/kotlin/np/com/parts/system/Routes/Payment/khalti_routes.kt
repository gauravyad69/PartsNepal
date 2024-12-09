package np.com.parts.system.Routes.Cart

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import np.com.parts.system.Models.PaymentRequestModel
import np.com.parts.system.Services.PaymentService


fun Route.khaltiRoutes(paymentService: PaymentService) {
    route("/khalti") {
        post("/start") {
            try {
                println("POST /khalti/start - Starting request")

                val principal = call.principal<JWTPrincipal>()
                if (principal == null) {
                    return@post call.respond(HttpStatusCode.Unauthorized, "User not authenticated")
                }

                val userId = principal.payload.getClaim("userId").asInt()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, "Invalid user ID")

                val request = call.receive<PaymentRequestModel>()

                // Sync the cart
                val result = paymentService.startKhalti(userId, request)
                call.respond(HttpStatusCode.OK, result)

            } catch (e: Exception) {
                println("Error in PUT /cart/sync: ${e.message}")
                e.printStackTrace()
                call.respond(
                    HttpStatusCode.InternalServerError,
                    "Failed to sync cart: ${e.message}"
                )
            }
        }
     // Get cart
        get("/verify") {
            try {
                println("GET /verify - Starting request")

                val principal = call.principal<JWTPrincipal>()
                println("Principal: $principal")

                if (principal == null) {
                    println("Principal is null")
                    return@get call.respond(HttpStatusCode.Unauthorized, "User not authenticated")
                }

                val userId = principal.payload.getClaim("userId").asInt()
                println("UserId from token: $userId")

                if (userId == null) {
                    println("UserId is null")
                    return@get call.respond(HttpStatusCode.Unauthorized, "Invalid user ID")
                }

                val pidx = call.parameters["pidx"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "pidx is required")
                 val orderNumber = call.parameters["orderNumber"]
                        ?: return@get call.respond(HttpStatusCode.BadRequest, "orderNumber is required")


                val pika = paymentService.verificationOfPayment(userId, pidx, orderNumber)
                println("verification retrieved: $pika")

               if(pika) call.respond(HttpStatusCode.OK)

            } catch (e: NoSuchElementException) {
                println("pidx not found error: ${e.message}")
                e.printStackTrace()
                call.respond(HttpStatusCode.NotFound, "pix not found")
            } catch (e: Exception) {
                println("Unexpected error in GET /pidx: ${e.message}")
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Failed to get pidx: ${e.message}")
            }
        }

        get("/verificationHook") {
            try {
                println("GET /verificationHook - Starting request")

                val principal = call.principal<JWTPrincipal>()
                println("Principal: $principal")

                if (principal == null) {
                    println("Principal is null, as it should be")
                }



                val pidx = call.parameters["pidx"] ?: ""
                val txnId = call.parameters["txnId"] ?: ""
                val amount = call.parameters["amount"]?.toIntOrNull() ?: 0
                val totalAmount = call.parameters["total_amount"]?.toIntOrNull() ?: 0
                val status = call.parameters["status"] ?: ""
                val mobile = call.parameters["mobile"] ?: ""
                val tidx = call.parameters["tidx"] ?: ""
                val purchaseOrderId = call.parameters["purchase_order_id"] ?: ""
                val purchaseOrderName = call.parameters["purchase_order_name"] ?: ""
                val transactionId = call.parameters["transaction_id"] ?: ""


                val pika = paymentService.verificationOfPaymentFromKhalti(
                    pidx, txnId, amount, totalAmount, status, mobile, tidx, purchaseOrderId, purchaseOrderName, transactionId
                )
                println("verification retrieved: $pika")

                call.respond(HttpStatusCode.OK)

            } catch (e: NoSuchElementException) {
                println("pidx not found error: ${e.message}")
                e.printStackTrace()
                call.respond(HttpStatusCode.NotFound, "pix not found")
            } catch (e: Exception) {
                println("Unexpected error in GET /pidx: ${e.message}")
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Failed to get pidx: ${e.message}")
            }
        }

/*
        // Update item quantity
        put("/items/{itemId}") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal!!.payload.getClaim("userId").asInt() ?: return@put call.respond(HttpStatusCode.Unauthorized, "User not authenticated")

            val itemId = call.parameters["itemId"]
                ?: return@put call.respond(HttpStatusCode.BadRequest, "Item ID is required")

            val request = call.receive<UpdateCartItemRequest>()

            // Validate quantity
            if (request.quantity <= 0) {
                return@put call.respond(HttpStatusCode.BadRequest, "Quantity must be greater than 0")
            }

            try {
                cartService.updateQuantity(userId, itemId, request.quantity)
                call.respond(HttpStatusCode.OK, "Quantity updated")
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: NoSuchElementException) {
                call.respond(HttpStatusCode.NotFound, "Item not found")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Failed to update quantity: ${e.message}")
            }
        }

        // Remove item from cart
        delete("/items/{itemId}") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal!!.payload.getClaim("userId").asInt() ?: return@delete call.respond(HttpStatusCode.Unauthorized, "User not authenticated")

            val itemId = call.parameters["itemId"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "Item ID is required")

            try {
                cartService.removeFromCart(userId, itemId)
                call.respond(HttpStatusCode.OK, "Item removed from cart")
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: NoSuchElementException) {
                call.respond(HttpStatusCode.NotFound, "Item not found")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Failed to remove item: ${e.message}")
            }
        }*/

    }
}
