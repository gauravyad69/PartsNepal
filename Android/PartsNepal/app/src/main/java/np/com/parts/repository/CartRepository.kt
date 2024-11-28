package np.com.parts.repository

import android.database.sqlite.SQLiteException
import io.ktor.client.*
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.Serializable
import np.com.parts.API.BASE_URL
import np.com.parts.API.Models.*
import np.com.parts.API.NetworkModule
import np.com.parts.database.AppDatabase
import np.com.parts.database.CartItem
import np.com.parts.database.SyncStatus
import timber.log.Timber
import java.util.*
import kotlin.concurrent.timer
import kotlin.coroutines.cancellation.CancellationException


sealed class CartError : Exception() {
    data class NetworkError(override val message: String) : CartError()
    data class ServerError(override val message: String?) : CartError()
    data class ValidationError(val errors: Map<String, String>) : CartError()
    data class UnknownError(override val message: String) : CartError()
}
class CartRepository(
    private val database: AppDatabase,
    private val client: HttpClient = NetworkModule.provideHttpClient()
) {
    private val cartDao = database.cartDao()

    // Get cart items as a Flow
    val cartItems = cartDao.getAllItems()
        .map { items -> items.map { it.toLineItem() } }

    // Get cart item count as a Flow
    val cartItemCount = cartDao.getTotalQuantity()

    // Add item to cart
    suspend fun addToCart(productId: Int, quantity: Int, productDetails: ProductModel): Result<Unit> {
        return try {
            val cartItem = CartItem(
                id = UUID.randomUUID().toString(), // Generate a unique ID
                productId = productId,
                name = productDetails.basic.productName,
                quantity = quantity,
                unitPrice = productDetails.basic.pricing.salePrice ?: productDetails.basic.pricing.regularPrice,
                imageUrl = productDetails.basic.inventory.mainImage,
                syncStatus = SyncStatus.PENDING
            )
            cartDao.insertItem(cartItem)
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to add item to cart")
            Result.failure(CartError.UnknownError(e.message ?: "Failed to add item to cart"))
        }
    }

    // Update item quantity
    suspend fun updateQuantity(itemId: String, quantity: Int): Result<Unit> {
        return try {
            cartDao.updateQuantity(itemId, quantity)
            cartDao.updateSyncStatus(itemId, SyncStatus.PENDING)
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to update quantity")
            Result.failure(CartError.UnknownError(e.message ?: "Failed to update quantity"))
        }
    }

    // Remove item from cart
    suspend fun removeFromCart(itemId: String): Result<Unit> {
        return try {
            cartDao.getItem(itemId)?.let { item ->
                cartDao.deleteItem(item)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to remove item from cart")
            Result.failure(CartError.UnknownError(e.message ?: "Failed to remove item"))
        }
    }

    // Clear cart
    suspend fun clearCart(): Result<Unit> {
        return try {
            cartDao.clearCart()
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to clear cart")
            Result.failure(CartError.UnknownError(e.message ?: "Failed to clear cart"))
        }
    }

    // Calculate cart summary
    suspend fun getCartSummary(): Flow<CartSummary> {
        return cartItems.map { items ->
            val subtotal = items.sumOf { it.unitPrice.amount * it.quantity }
            CartSummary(
                items = items,
                subtotal = Money(subtotal),
                total = Money(subtotal) // Add shipping cost calculation if needed
            )
        }
    }
    // Sync cart with remote server
    suspend fun syncCart(): Result<Unit> {
        Timber.i("syncCart() called in CartRepository")
        return try {
            val pendingItems = cartDao.getAllItems().first() // Get current items
            
            if (pendingItems.isEmpty()) {
                Timber.i("No items to sync")
                return Result.success(Unit)
            }

                val remoteCart = CartSyncRequest(
                    items = pendingItems.map { it.toCartItemSync() }
                )

                try {
                    val response = client.put("$BASE_URL/cart/sync") {
                        contentType(ContentType.Application.Json)
                        setBody(remoteCart)
                    }

                when (response.status) {
                    HttpStatusCode.Created, HttpStatusCode.OK -> {
                        // Update local items to SYNCED status
                        pendingItems.forEach { item ->
                            cartDao.updateSyncStatus(item.id, SyncStatus.SYNCED)
                        }
                        Result.success(Unit)
                    }
                    else -> Result.failure(CartError.ServerError("Failed to sync cart: ${response.status}"))
                }
            } catch (e: Exception) {
                Timber.e(e, "Network error during sync")
                Result.failure(mapError(e))
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to sync cart")
            Result.failure(CartError.UnknownError(e.message ?: "Failed to sync cart"))
        }
    }


    suspend fun createOrder(
        shippingDetails: ShippingDetails,
        paymentMethod: PaymentMethod,
        notes: String? = null
    ): CreateOrderRequest = withContext(NonCancellable) {
        Timber.d("Creating order request...")
        
        try {
            // Test database access first
            Timber.d("Testing database access...")
            testDatabaseAccess()
            
            // Get cart items directly using the sync method
            Timber.d("Fetching items from cart database...")
            val items = withContext(Dispatchers.IO) {
                try {
                    cartDao.getItemsSync().also { items ->
                        Timber.d("Successfully retrieved ${items.size} items from database")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Failed to fetch items from database")
                    throw e
                }
            }
            
            if (items.isEmpty()) {
                Timber.w("No items found in cart")
                throw IllegalStateException("Cannot create order with empty cart")
            }
            
            Timber.d("Converting ${items.size} cart items to line items...")
            val lineItems = items.map { cartItem ->
                cartItem.toLineItem().also {
                    Timber.d("Converted cart item ${cartItem.id} to line item")
                }
            }
            
            CreateOrderRequest(
                items = lineItems,
                paymentMethod = paymentMethod,
                shippingDetails = shippingDetails,
                notes = notes,
                source = OrderSource.MOBILE_APP
            ).also {
                Timber.d("Created order request with ${it.items.size} items")
            }
        } catch (e: Exception) {
            val errorMessage = when (e) {
                is TimeoutCancellationException -> "Database operation timed out"
                is SQLiteException -> "Database error: ${e.message}"
                is IllegalStateException -> e.message ?: "Invalid cart state"
                else -> "Error creating order request: ${e.message}"
            }
            Timber.e(e, errorMessage)
            throw e
        }
    }

//     suspend fun syncWithRemoteServer(cart: CartSyncRequest): Result<Unit> {
//        Timber.i("sync at cartRepository ,  this with remote is being called")
//
//           return try {
//                Timber.i("sync at cartRepository ,   remote/try is being called")
//
//                // Create a non-cancellable context for the network request
//
//            } catch (e: CancellationException) {
//                println("Request was cancelled but continuing: ${e.message}")
//                // Continue with the request despite cancellation
//                Result.failure(e)
//            } catch (e: Exception) {
//                println("Error adding item to cart: ${e.message}")
//                Result.failure(e)
//            }
//    }
     fun mapError(error: Exception): CartError = when (error) {
        is io.ktor.client.network.sockets.ConnectTimeoutException ->
            CartError.NetworkError("Connection timeout")
        is io.ktor.client.plugins.ResponseException ->
            CartError.ServerError(error.message)
        else -> CartError.UnknownError(error.message ?: "Unknown error occurred")
    }

    // Add this function to test database access
    suspend fun testDatabaseAccess() = withContext(Dispatchers.IO) {
        try {
            Timber.d("Testing database access - starting")
            val itemCount = cartDao.getTotalQuantity().first()
            Timber.d("Database access successful - total quantity: $itemCount")
            
            val items = cartDao.getAllItems().first()
            Timber.d("Successfully retrieved ${items.size} items from database")
        } catch (e: Exception) {
            Timber.e(e, "Database access test failed")
            throw e
        }
    }
}

data class CartSummary(
    val items: List<LineItem>,
    val subtotal: Money,
    val shippingCost: Money = Money(0),
    val total: Money
)



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


@Serializable
private data class AddToCartRequest(
    val productId: Int,
    val quantity: Int
)

@Serializable
private data class UpdateCartItemRequest(
    val quantity: Int
)