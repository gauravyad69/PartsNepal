package np.com.parts.system.Services

import com.mongodb.ErrorCategory
import com.mongodb.MongoCommandException
import com.mongodb.MongoWriteException
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import np.com.parts.system.Models.*
import org.bson.conversions.Bson
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
        collection.createIndex(Indexes.ascending("orderNumber"))
        collection.createIndex(Indexes.ascending("customer.id"))
        collection.createIndex(Indexes.ascending("status"))
        collection.createIndex(Indexes.ascending("orderDate"))
        collection.createIndex(Indexes.ascending("payment.status"))
        collection.createIndex(Indexes.ascending("metadata.createdAt"))
        collection.createIndex(Indexes.ascending("tracking.trackingNumber"))
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

    // Read an order by orderNumber
    suspend fun getOrderByNumber(orderNumber: String): OrderModel? = withContext(Dispatchers.IO) {
        collection.find(Filters.eq("orderNumber", orderNumber)).firstOrNull()
    }

    // Read all orders for a customer
    suspend fun getOrdersByCustomer(customerId: Int): List<OrderModel> = withContext(Dispatchers.IO) {
        collection.find(Filters.eq("customer.id", customerId)).toList()
    }

    // Read all orders with optional pagination and sorting
    suspend fun getAllOrders(
        skip: Int = 0,
        limit: Int = 50,
        sortBy: String = "orderDate",
        descending: Boolean = true
    ): List<OrderModel> = withContext(Dispatchers.IO) {
        collection.find()
            .skip(skip)
            .limit(limit)
            .sort(if (descending) Sorts.descending(sortBy) else Sorts.ascending(sortBy))
            .toList()
    }

    // Update order status and add tracking event
    suspend fun updateOrderStatus(
        orderNumber: String,
        newStatus: OrderStatus,
        location: String? = null,
        description: String? = null,
        updatedBy: String
    ): Boolean = withContext(Dispatchers.IO) {
        val trackingEvent = TrackingEvent(
            status = newStatus,
            timestamp = System.currentTimeMillis(),
            location = location,
            description = description,
            updatedBy = updatedBy
        )

        val result = collection.updateOne(
            Filters.eq("orderNumber", orderNumber),
            Updates.combine(
                Updates.set("status", newStatus),
                Updates.push("tracking.history", trackingEvent),
                Updates.set("lastUpdated", System.currentTimeMillis())
            )
        )
        result.modifiedCount > 0
    }

    // Update payment status
    suspend fun updatePaymentStatus(
        orderNumber: String,
        paymentStatus: PaymentStatus,
        transactionId: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        val updates = mutableListOf(
            Updates.set("payment.status", paymentStatus),
            Updates.set("lastUpdated", System.currentTimeMillis())
        )

        if (transactionId != null) {
            updates.add(Updates.set("payment.transactionId", transactionId))
            updates.add(Updates.set("payment.paymentDate", System.currentTimeMillis()))
        }

        val result = collection.updateOne(
            Filters.eq("orderNumber", orderNumber),
            Updates.combine(updates)
        )
        result.modifiedCount > 0
    }

    // Update shipping details
    suspend fun updateShippingDetails(orderNumber: String, shipping: ShippingDetails): Boolean = withContext(Dispatchers.IO) {
        val result = collection.updateOne(
            Filters.eq("orderNumber", orderNumber),
            Updates.combine(
                Updates.set("shipping", shipping),
                Updates.set("lastUpdated", System.currentTimeMillis())
            )
        )
        result.modifiedCount > 0
    }

    // Add order note
    suspend fun addOrderNote(orderNumber: String, note: OrderNote): Boolean = withContext(Dispatchers.IO) {
        val result = collection.updateOne(
            Filters.eq("orderNumber", orderNumber),
            Updates.combine(
                Updates.push("metadata.notes", note),
                Updates.set("lastUpdated", System.currentTimeMillis())
            )
        )
        result.modifiedCount > 0
    }

    // Update tracking information
    suspend fun updateTracking(
        orderNumber: String,
        trackingNumber: String,
        carrier: String
    ): Boolean = withContext(Dispatchers.IO) {
        val result = collection.updateOne(
            Filters.eq("orderNumber", orderNumber),
            Updates.combine(
                Updates.set("tracking.trackingNumber", trackingNumber),
                Updates.set("tracking.carrier", carrier),
                Updates.set("tracking.lastUpdated", System.currentTimeMillis()),
                Updates.set("lastUpdated", System.currentTimeMillis())
            )
        )
        result.modifiedCount > 0
    }

    // Get orders by status
    suspend fun getOrdersByStatus(status: OrderStatus): List<OrderModel> = withContext(Dispatchers.IO) {
        collection.find(Filters.eq("status", status)).toList()
    }

    // Get orders by payment status
    suspend fun getOrdersByPaymentStatus(paymentStatus: PaymentStatus): List<OrderModel> = withContext(Dispatchers.IO) {
        collection.find(Filters.eq("payment.status", paymentStatus)).toList()
    }

    // Get orders within a date range
    suspend fun getOrdersByDateRange(
        startDate: Long,
        endDate: Long,
        status: OrderStatus? = null
    ): List<OrderModel> = withContext(Dispatchers.IO) {
        val filters = mutableListOf(
            Filters.gte("orderDate", startDate),
            Filters.lte("orderDate", endDate)
        )

        if (status != null) {
            filters.add(Filters.eq("status", status))
        }

        collection.find(Filters.and(filters)).toList()
    }

    // Get orders by customer type
    suspend fun getOrdersByCustomerType(customerType: CustomerType): List<OrderModel> = withContext(Dispatchers.IO) {
        collection.find(Filters.eq("customer.type", customerType)).toList()
    }

    // Search orders by multiple criteria
    suspend fun searchOrders(
        customerName: String? = null,
        customerEmail: String? = null,
        orderStatus: OrderStatus? = null,
        paymentStatus: PaymentStatus? = null,
        startDate: Long? = null,
        endDate: Long? = null,
        skip: Int = 0,
        limit: Int = 50
    ): List<OrderModel> = withContext(Dispatchers.IO) {
        val filters = mutableListOf<Bson>()

        customerName?.let { filters.add(Filters.regex("customer.name", it, "i")) }
        customerEmail?.let { filters.add(Filters.eq("customer.email", it)) }
        orderStatus?.let { filters.add(Filters.eq("status", it)) }
        paymentStatus?.let { filters.add(Filters.eq("payment.status", it)) }

        if (startDate != null && endDate != null) {
            filters.add(Filters.and(
                Filters.gte("orderDate", startDate),
                Filters.lte("orderDate", endDate)
            ))
        }

        val query = if (filters.isEmpty()) {
            Filters.empty()
        } else {
            Filters.and(filters)
        }

        collection.find(query)
            .skip(skip)
            .limit(limit)
            .sort(Sorts.descending("orderDate"))
            .toList()
    }
}