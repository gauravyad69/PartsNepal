package np.com.parts.system.Routes.Cart

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import np.com.parts.system.Services.CartService
import kotlin.text.get



fun Route.cartRoutes(cartService: CartService) {
    route("/cart") {
        put("/sync") {
            try {
                println("PUT /cart/sync - Starting request")

                val principal = call.principal<JWTPrincipal>()
                if (principal == null) {
                    return@put call.respond(HttpStatusCode.Unauthorized, "User not authenticated")
                }

                val userId = principal.payload.getClaim("userId").asInt()
                    ?: return@put call.respond(HttpStatusCode.Unauthorized, "Invalid user ID")

                val request = call.receive<CartSyncRequest>()

                // Sync the cart
                val syncResult = cartService.syncCart(userId, request.items)
                call.respond(HttpStatusCode.OK, syncResult)

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
        get {
            try {
                println("GET /cart - Starting request")
                
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
                
                println("Fetching cart for user: $userId")
                val cart = cartService.getCart(userId)
                println("Cart retrieved: $cart")
                
                call.respond(HttpStatusCode.OK, cart)
                
            } catch (e: NoSuchElementException) {
                println("Cart not found error: ${e.message}")
                e.printStackTrace()
                call.respond(HttpStatusCode.NotFound, "Cart not found")
            } catch (e: Exception) {
                println("Unexpected error in GET /cart: ${e.message}")
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Failed to get cart: ${e.message}")
            }
        }

        // Add item to cart
        post("/items") {
            try {
                println("POST /cart/items - Starting request")
                
                val principal = call.principal<JWTPrincipal>()
                println("Principal: $principal")
                
                if (principal == null) {
                    println("Principal is null")
                    return@post call.respond(HttpStatusCode.Unauthorized, "User not authenticated")
                }
                
                val userId = principal.payload.getClaim("userId").asInt()
                println("UserId from token: $userId")
                
                if (userId == null) {
                    println("UserId is null")
                    return@post call.respond(HttpStatusCode.Unauthorized, "Invalid user ID")
                }
                
                val request = call.receive<AddToCartRequest>()
                
                // Validate quantity
                if (request.quantity <= 0) {
                    return@post call.respond(HttpStatusCode.BadRequest, "Quantity must be greater than 0")
                }
                
                try {
                    var created = cartService.addToCart(userId, request.productId, request.quantity)
                    call.respond(HttpStatusCode.Created, created)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
                } catch (e: NoSuchElementException) {
                    call.respond(HttpStatusCode.NotFound, "Product not found")
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Failed to add item: ${e.message}")
                }
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: NoSuchElementException) {
                call.respond(HttpStatusCode.NotFound, "Product not found")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Failed to add item: ${e.message}")
            }
        }

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
        }

        // Clear cart
        delete {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal!!.payload.getClaim("userId").asInt() ?: return@delete call.respond(HttpStatusCode.Unauthorized, "User not authenticated")
            
            try {
                cartService.clearCart(userId)
                call.respond(HttpStatusCode.OK, "Cart cleared")
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
            } catch (e: NoSuchElementException) {
                call.respond(HttpStatusCode.NotFound, "Cart not found")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Failed to clear cart: ${e.message}")
            }
        }
    }
}

@Serializable
data class AddToCartRequest(
    val productId: Int,
    val quantity: Int
)

@Serializable
data class UpdateCartItemRequest(
    val quantity: Int
)

data class UserPrincipal(val id: Int) : Principal


@Serializable
data class CartSyncRequest(
    val items: List<CartItemSync>,
    val lastSyncTimestamp: Long = System.currentTimeMillis()
)

@Serializable
data class CartItemSync(
    val id: String,
    val productId: Int,
    val quantity: Int,
    val lastModified: Long = System.currentTimeMillis()
)

@Serializable
data class CartSyncResponse(
    val success: Boolean,
    val syncedItems: List<CartItemSync>,
    val conflictedItems: List<CartItemConflict> = emptyList(),
    val serverTimestamp: Long = System.currentTimeMillis()
)

@Serializable
data class CartItemConflict(
    val clientItem: CartItemSync,
    val serverItem: CartItemSync
)