package np.com.parts.system.Services

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import np.com.parts.system.Models.*
import np.com.parts.system.Routes.Cart.CartItemConflict
import np.com.parts.system.Routes.Cart.CartItemSync
import np.com.parts.system.Routes.Cart.CartSyncResponse
import org.bson.types.ObjectId
import org.litote.kmongo.getCollection

class CartService(
    private val database: MongoDatabase,
    private val productService: ProductService
) {
    private val collection: MongoCollection<Cart>

    init {
        try {
            database.createCollection("carts")
        } catch (e: Exception) {
            // Collection already exists
        }
        collection = database.getCollection<Cart>("carts")
        
        // Create indexes
        collection.createIndex(Indexes.ascending("userId"))
        collection.createIndex(
            Indexes.compoundIndex(
                Indexes.ascending("userId"),
                Indexes.ascending("items.productId")
            )
        )
    }








// Add these functions to your existing CartService class

    suspend fun syncCart(userId: Int, clientItems: List<CartItemSync>): CartSyncResponse = withContext(Dispatchers.IO) {
        try {
            println("Syncing cart for user $userId with ${clientItems.size} items")

            // Get current server cart
            val serverCart = getCart(userId)
            val conflicts = mutableListOf<CartItemConflict>()
            val syncedItems = mutableListOf<CartItemSync>()

            // Process client items
            clientItems.forEach { clientItem ->
                try {
                    val serverItem = serverCart.items.find { it.id == clientItem.id }
                    val product = productService.getProductById(clientItem.productId)
                        ?: throw NoSuchElementException("Product not found: ${clientItem.productId}")

                    if (serverItem == null) {
                        // New item - add to cart
                        println("Adding new item to cart: ${clientItem.id}")
                        val success = addToCart(userId, clientItem.productId, clientItem.quantity)
                        if (success) {
                            syncedItems.add(clientItem)
                        }
                    } else {
                        // Existing item - check for conflicts
                        if (serverItem.lastModified > clientItem.lastModified) {
                            // Server has newer version
                            conflicts.add(
                                CartItemConflict(
                                    clientItem = clientItem,
                                    serverItem = CartItemSync(
                                        id = serverItem.id,
                                        productId = serverItem.productId,
                                        quantity = serverItem.quantity,
                                        lastModified = serverItem.lastModified
                                    )
                                )
                            )
                        } else {
                            // Client has newer version - update server
                            println("Updating existing item: ${clientItem.id}")
                            val success = updateQuantity(userId, clientItem.id, clientItem.quantity)
                            if (success) {
                                syncedItems.add(clientItem)
                            }
                        }
                    }
                } catch (e: Exception) {
                    println("Error processing item ${clientItem.id}: ${e.message}")
                    // Continue with next item
                }
            }

            // Remove items that exist on server but not in client
            val itemsToRemove = serverCart.items.filter { serverItem ->
                clientItems.none { it.id == serverItem.id }
            }

            itemsToRemove.forEach { item ->
                println("Removing item from server: ${item.id}")
                removeFromCart(userId, item.id)
            }

            // Update cart summary
            updateCartSummary(userId)

            CartSyncResponse(
                success = true,
                syncedItems = syncedItems,
                conflictedItems = conflicts,
                serverTimestamp = System.currentTimeMillis()
            )

        } catch (e: Exception) {
            println("Error in syncCart: ${e.message}")
            e.printStackTrace()
            CartSyncResponse(
                success = false,
                syncedItems = emptyList(),
                conflictedItems = emptyList(),
                serverTimestamp = System.currentTimeMillis()
            )
        }
    }






    suspend fun getCart(userId: Int): Cart = withContext(Dispatchers.IO) {
        try {
            println("CartService.getCart - Fetching cart for userId: $userId")
            val cart = collection.find(Filters.eq("userId", userId)).firstOrNull()
            
            if (cart != null) {
                println("Found existing cart: $cart")
                return@withContext cart
            }
            
            // Create new cart if none exists
            println("No existing cart found, creating new one")
            val newCart = Cart(
                userId = UserId(userId),
                items = emptyList(),
                summary = OrderSummary(
                    subtotal = Money(0),
                    total = Money(0)
                ),
            )
            
            // Insert the new cart
            collection.insertOne(newCart)
            println("Created new cart: $newCart")
            
            return@withContext newCart
            
        } catch (e: Exception) {
            println("Error in CartService.getCart: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    suspend fun addToCart(userId: Int, productId: Int, quantity: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            println("Adding to cart - userId: $userId, productId: $productId, quantity: $quantity")
            
            // Get the product
            val product = productService.getProductById(productId) 
                ?: throw NoSuchElementException("Product not found: $productId")
            
            // Create line item
            val lineItem = createLineItem(product, quantity)
            println("Created line item: $lineItem")
            
            // Get existing cart or create new one
            val existingCart = collection.find(Filters.eq("userId", UserId(userId))).firstOrNull()
            
            if (existingCart == null) {
                // Create new cart
                println("Creating new cart for user $userId")
                val newCart = Cart(
                    userId = UserId(userId),
                    items = listOf(lineItem),
                    summary = calculateOrderSummary(listOf(lineItem))
                )
                
                val insertResult = collection.insertOne(newCart)
                return@withContext insertResult.wasAcknowledged()
            } else {
                // Update existing cart
                println("Updating existing cart for user $userId")
                val updateResult = collection.updateOne(
                    Filters.eq("userId", UserId(userId)),
                    Updates.combine(
                        Updates.push("items", lineItem),
                        Updates.set("summary", calculateOrderSummary(existingCart.items + lineItem))
                    )
                )
                
                return@withContext updateResult.wasAcknowledged()
            }
        } catch (e: Exception) {
            println("Error in addToCart: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    suspend fun updateQuantity(userId: Int, itemId: String, quantity: Int): Boolean = withContext(Dispatchers.IO) {
        val result = collection.updateOne(
            Filters.and(
                Filters.eq("userId", userId),
                Filters.eq("items.id", itemId)
            ),
            Updates.combine(
                Updates.set("items.$.quantity", quantity),
            )
        )
        
        updateCartSummary(userId)
        result.modifiedCount > 0
    }

    suspend fun removeFromCart(userId: Int, itemId: String): Boolean = withContext(Dispatchers.IO) {
        val result = collection.updateOne(
            Filters.eq("userId", userId),
            Updates.combine(
                Updates.pull("items", Filters.eq("id", itemId)),
            )
        )
        
        updateCartSummary(userId)
        result.modifiedCount > 0
    }

    suspend fun clearCart(userId: Int): Boolean = withContext(Dispatchers.IO) {
        val result = collection.deleteOne(Filters.eq("userId", userId))
        result.deletedCount > 0
    }

    private suspend fun updateCartSummary(userId: Int) {
        val cart = getCart(userId)
        val summary = calculateOrderSummary(cart.items)
        
        collection.updateOne(
            Filters.eq("userId", userId),
            Updates.set("summary", summary)
        )
    }

    private fun createEmptyOrderSummary() = OrderSummary(
        subtotal = Money(0),
        total = Money(0)
    )

    private fun createLineItem(product: ProductModel, quantity: Int): LineItem {
        val price = product.basic.pricing.salePrice ?: product.basic.pricing.regularPrice
        return LineItem(
            id = ObjectId().toString(),
            productId = product.productId,
            name = product.basic.productName,
            quantity = quantity,
            unitPrice = price,
            totalPrice = Money(price.amount * quantity),
            imageUrl = product.basic.inventory.mainImage,
            discount = product.basic.pricing.discount
        )
    }

    private fun calculateOrderSummary(items: List<LineItem>): OrderSummary {
        val subtotal = Money(items.sumOf { it.totalPrice.amount })
        return OrderSummary(
            subtotal = subtotal,
            total = subtotal // Add shipping, tax, etc. as needed
        )
    }

    private fun generateItemId(): String = 
        java.util.UUID.randomUUID().toString()
}

data class CartItemConflict(
    val clientItem: CartItemSync,
    val serverItem: CartItemSync
)