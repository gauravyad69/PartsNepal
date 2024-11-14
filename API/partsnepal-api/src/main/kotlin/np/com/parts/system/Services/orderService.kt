package np.com.parts.system.Services

import com.mongodb.ErrorCategory
import com.mongodb.MongoCommandException
import com.mongodb.MongoWriteException
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import np.com.parts.system.Models.DeliveryDetails
import np.com.parts.system.Models.OrderModel
import org.litote.kmongo.getCollection

class OrderService(private val database: MongoDatabase) {
    private lateinit var collection: MongoCollection<OrderModel>

    init {
        try {
            database.createCollection("orders")
        } catch (e: MongoCommandException) {
            // Collection already exists, ignore the error
        }
        collection = database.getCollection<OrderModel>("orders")

        // Create indexes for better query performance
        collection.createIndex(Indexes.ascending("orderId"))
        collection.createIndex(Indexes.ascending("customerId"))
        collection.createIndex(Indexes.ascending("orderStatus"))
        collection.createIndex(Indexes.ascending("orderDate"))
    }

    // Create a new order
    suspend fun createOrder(order: OrderModel): Boolean = withContext(Dispatchers.IO) {
        try {
            collection.insertOne(order)
            true
        } catch (e: MongoWriteException) {
            if (e.error.category == ErrorCategory.DUPLICATE_KEY) {
                false // Order already exists
            } else {
                throw e // Rethrow other errors
            }
        }
    }

    // Read an order by orderId
    suspend fun getOrderById(orderId: String): OrderModel? = withContext(Dispatchers.IO) {
        collection.find(Filters.eq("orderId", orderId)).firstOrNull()
    }

    // Read all orders for a customer
    suspend fun getOrdersByCustomer(customerId: String): List<OrderModel> = withContext(Dispatchers.IO) {
        collection.find(Filters.eq("customerId", customerId)).toList()
    }

    // Read all orders with optional pagination
    suspend fun getAllOrders(skip: Int = 0, limit: Int = 50): List<OrderModel> = withContext(Dispatchers.IO) {
        collection.find()
            .skip(skip)
            .limit(limit)
            .toList()
    }

    // Update order status
    suspend fun updateOrderStatus(orderId: String, newStatus: String): Boolean = withContext(Dispatchers.IO) {
        val result = collection.updateOne(
            Filters.eq("orderId", orderId),
            Updates.set("orderStatus", newStatus)
        )
        result.modifiedCount > 0
    }

    // Update delivery details
    suspend fun updateDeliveryDetails(orderId: String, deliveryDetails: DeliveryDetails): Boolean = withContext(Dispatchers.IO) {
        val result = collection.updateOne(
            Filters.eq("orderId", orderId),
            Updates.set("deliveryDetails", deliveryDetails)
        )
        result.modifiedCount > 0
    }

    // Delete an order
    suspend fun deleteOrder(orderId: String): Boolean = withContext(Dispatchers.IO) {
        val result = collection.deleteOne(Filters.eq("orderId", orderId))
        result.deletedCount > 0
    }

    // Get orders by status
    suspend fun getOrdersByStatus(status: String): List<OrderModel> = withContext(Dispatchers.IO) {
        collection.find(Filters.eq("orderStatus", status)).toList()
    }

    // Get orders within a date range
    suspend fun getOrdersByDateRange(startDate: Long, endDate: Long): List<OrderModel> = withContext(Dispatchers.IO) {
        collection.find(
            Filters.and(
                Filters.gte("orderDate", startDate),
                Filters.lte("orderDate", endDate)
            )
        ).toList()
    }
}