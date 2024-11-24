package np.com.parts.repository

import io.ktor.client.*
import kotlinx.coroutines.flow.*
import np.com.parts.API.Models.*
import np.com.parts.API.NetworkModule
import np.com.parts.API.Repository.CartError
import np.com.parts.database.AppDatabase
import np.com.parts.database.CartItem
import np.com.parts.database.SyncStatus
import timber.log.Timber
import java.util.*

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
        return try {
            val pendingItems = cartDao.getItemsByStatus(SyncStatus.PENDING)
            if (pendingItems.isEmpty()) {
                return Result.success(Unit)
            }

            // Convert local cart items to remote format
            val remoteCart = Cart(
                userId = UserId(0), // Get from user session
                items = pendingItems.map { it.toLineItem() },
                summary = OrderSummary(
                    subtotal = Money(pendingItems.sumOf { it.unitPrice.amount * it.quantity }),
                    shippingCost = Money(0),
                    total = Money(pendingItems.sumOf { it.unitPrice.amount * it.quantity })
                )
            )

            // Sync with remote server
            val result = syncWithRemoteServer(remoteCart)
            
            // Update sync status for successful items
            if (result.isSuccess) {
                pendingItems.forEach { item ->
                    cartDao.updateSyncStatus(item.id, SyncStatus.SYNCED)
                }
            }

            result
        } catch (e: Exception) {
            Timber.e(e, "Failed to sync cart")
            Result.failure(CartError.UnknownError(e.message ?: "Failed to sync cart"))
        }
    }

    private suspend fun syncWithRemoteServer(cart: Cart): Result<Unit> {
        // Implement the actual remote sync logic here using your existing API
        // This is just a placeholder
        return try {
            // Your existing remote cart operations
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class CartSummary(
    val items: List<LineItem>,
    val subtotal: Money,
    val shippingCost: Money = Money(0),
    val total: Money
) 