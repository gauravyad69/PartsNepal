package np.com.parts.API.Repository

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import np.com.parts.API.BASE_URL
import np.com.parts.API.Models.*
import np.com.parts.API.NetworkModule
import timber.log.Timber
import kotlin.coroutines.cancellation.CancellationException

sealed class CartError : Exception() {
    data class NetworkError(override val message: String) : CartError()
    data class ServerError(override val message: String?) : CartError()
    data class ValidationError(val errors: Map<String, String>) : CartError()
    data class UnknownError(override val message: String) : CartError()
}

class CartRepository(    private val client: HttpClient = NetworkModule.provideHttpClient()) {
    
    suspend fun getCart(): Result<Cart> {
        return try {
            val response = client.get("$BASE_URL/cart") {
                contentType(ContentType.Application.Json)
            }
            val cart: Cart = response.body()
            Result.success(cart)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get cart")
            Result.failure(mapError(e))
        }
    }

    suspend fun addToCart(productId: Int, quantity: Int): Result<Unit> =
        supervisorScope {  // Use supervisorScope instead of coroutineScope
            try {
                // Create a non-cancellable context for the network request
                withContext(NonCancellable + Dispatchers.IO) {
                    val response = client.post("$BASE_URL/cart/items") {
                        contentType(ContentType.Application.Json)
                        setBody(AddToCartRequest(productId, quantity))
                    }

                    when (response.status) {
                        HttpStatusCode.Created -> Result.success(Unit)
                        else -> Result.failure(Exception("Failed to add item to cart: ${response.status}"))
                    }
                }
            } catch (e: CancellationException) {
                println("Request was cancelled but continuing: ${e.message}")
                // Continue with the request despite cancellation
                Result.failure(e)
            } catch (e: Exception) {
                println("Error adding item to cart: ${e.message}")
                Result.failure(e)
            }
        }

    suspend fun removeFromCart(itemId: String): Result<Unit> {
        return try {
            client.delete("$BASE_URL/cart/items/$itemId") {
                contentType(ContentType.Application.Json)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to remove item from cart")
            Result.failure(mapError(e))
        }
    }

    suspend fun updateQuantity(itemId: String, quantity: Int): Result<Unit> {
        return try {
            require(quantity > 0) { "Quantity must be greater than 0" }
            
            val request = UpdateCartItemRequest(quantity)
            client.put("$BASE_URL/cart/items/$itemId") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to update quantity")
            Result.failure(mapError(e))
        }
    }

    suspend fun clearCart(): Result<Unit> {
        return try {
            client.delete("$BASE_URL/cart") {
                contentType(ContentType.Application.Json)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to clear cart")
            Result.failure(mapError(e))
        }
    }

    private fun mapError(error: Exception): CartError = when (error) {
        is io.ktor.client.network.sockets.ConnectTimeoutException -> 
            CartError.NetworkError("Connection timeout")
        is io.ktor.client.plugins.ResponseException ->
            CartError.ServerError(error.message)
        else -> CartError.UnknownError(error.message ?: "Unknown error occurred")
    }
}

@Serializable
private data class AddToCartRequest(
    val productId: Int,
    val quantity: Int
)

@Serializable
private data class UpdateCartItemRequest(
    val quantity: Int
)