package np.com.parts.system.Services

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import np.com.parts.system.Models.*
import org.bson.conversions.Bson
import org.litote.kmongo.getCollection

class OrderService(
    private val database: MongoDatabase,
    private val userService: UserService,
    private val cartService: CartService,
    private val productService: ProductService
) {
    private val collection: MongoCollection<OrderModel>

    init {


        try {
            database.createCollection("orders")
        } catch (e: Exception) {
            // Collection already exists
        }

        collection = database.getCollection()
        setupIndexes()
    }

    private fun setupIndexes() {
        collection.createIndex(Indexes.ascending("orderNumber"), IndexOptions().unique(true))
        collection.createIndex(Indexes.ascending("customer.id"))
        collection.createIndex(Indexes.ascending("status"))
        collection.createIndex(Indexes.ascending("orderDate"))
        collection.createIndex(Indexes.ascending("payment.status"))
    }

    suspend fun createOrder(userId: Int, request: CreateOrderRequest): Result<OrderModel> =
        withContext(Dispatchers.IO) {
            try {
                val user = userService.getUserById(UserId(userId)) ?: 
                    return@withContext Result.failure(Exception("User not found"))
                
                val order = OrderModel(
                    orderNumber = generateOrderNumber(),
                    items = request.items,
                    customer = CustomerInfo(
                        id = userId,
                        name = user.fullName,
                        type = mapAccountTypeToCustomerType(user.accountType)
                    ),
                    payment = PaymentInfo(
                        method = request.paymentMethod,
                        status = PaymentStatus.PENDING
                    ),
                    shippingDetails = request.shippingDetails,
                    summary = calculateOrderSummary(request.items),
                    status = OrderStatus.PENDING_PAYMENT,
                    notes = request.notes,
                    source = request.source
                )

                collection.insertOne(order)
                cartService.clearCart(userId)
                Result.success(order)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getOrderByNumber(orderNumber: String): OrderModel? = withContext(Dispatchers.IO) {
        collection.find(Filters.eq("orderNumber", orderNumber)).firstOrNull()
    }

    suspend fun getOrdersByCustomer(customerId: Int, skip: Int = 0, limit: Int = 50): List<OrderModel> = 
        withContext(Dispatchers.IO) {
            collection.find(Filters.eq("customer.id", customerId))
                .sort(Sorts.descending("orderDate"))
                .skip(skip)
                .limit(limit)
                .toList()
        }

    suspend fun updateOrderStatus(
        orderNumber: String, 
        status: OrderStatus, 
        updatedBy: String
    ): Result<OrderModel> = withContext(Dispatchers.IO) {
        try {
            val order = getOrderByNumber(orderNumber) ?: 
                return@withContext Result.failure(Exception("Order not found"))

            val trackingEvent = TrackingEvent(
                status = status,
                timestamp = System.currentTimeMillis(),
                updatedBy = updatedBy
            )

            val result = collection.updateOne(
                Filters.eq("orderNumber", orderNumber),
                Updates.combine(
                    Updates.set("status", status),
                    Updates.push("tracking.history", trackingEvent),
                    Updates.set("lastUpdated", System.currentTimeMillis()),
                    Updates.inc("version", 1)
                )
            )

            if (result.modifiedCount > 0) {
                Result.success(getOrderByNumber(orderNumber)!!)
            } else {
                Result.failure(Exception("Failed to update order status"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePaymentStatus(
        orderNumber: String,
        paymentStatus: PaymentStatus,
        transactionId: String? = null
    ): Result<OrderModel> = withContext(Dispatchers.IO) {
        try {
            val order = getOrderByNumber(orderNumber) ?: 
                return@withContext Result.failure(Exception("Order not found"))

            val updates = mutableListOf(
                Updates.set("payment.status", paymentStatus),
                Updates.set("lastUpdated", System.currentTimeMillis()),
                Updates.inc("version", 1)
            )

            transactionId?.let { 
                updates.add(Updates.set("payment.transactionId", it))
                updates.add(Updates.set("payment.paidDate", System.currentTimeMillis()))
            }

            val result = collection.updateOne(
                Filters.eq("orderNumber", orderNumber),
                Updates.combine(updates)
            )

            if (result.modifiedCount > 0) {
                Result.success(getOrderByNumber(orderNumber)!!)
            } else {
                Result.failure(Exception("Failed to update payment status"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun calculateOrderSummary(items: List<LineItem>): OrderSummary {
        var subtotal = Money(0)
        var totalDiscount = Money(0)

        items.forEach { item ->
            subtotal = Money(subtotal.amount + item.totalPrice.amount)
            item.discount?.let { discount ->
                val discountAmount = when (discount.type) {
                    DiscountType.PERCENTAGE -> {
                        val percentage = discount.amount.amount.toDouble() / 100
                        Money((item.totalPrice.amount * percentage).toLong())
                    }
                    DiscountType.FIXED_AMOUNT -> discount.amount
                }
                totalDiscount = Money(totalDiscount.amount + discountAmount.amount)
            }
        }

        val shippingCost = Money(0) // Calculate based on your business logic
        val tax = Money((subtotal.amount * 0.13).toLong()) // 13% VAT for Nepal

        return OrderSummary(
            subtotal = subtotal,
            discount = if (totalDiscount.amount > 0) totalDiscount else null,
            shippingCost = shippingCost,
            tax = tax,
            total = Money(subtotal.amount - totalDiscount.amount + shippingCost.amount + tax.amount)
        )
    }

    private fun generateOrderNumber(): String {
        val timestamp = System.currentTimeMillis()
        val random = (1000..9999).random()
        return "ORD-${timestamp}-$random"
    }

    private fun mapAccountTypeToCustomerType(accountType: AccountType): CustomerType =
        when (accountType) {
            AccountType.BUSINESS -> CustomerType.BUSINESS
            else -> CustomerType.INDIVIDUAL
        }

    suspend fun updateShippingDetails(
        orderNumber: String,
        shippingDetails: ShippingDetails
    ): Result<OrderModel> = withContext(Dispatchers.IO) {
        try {
            val order = getOrderByNumber(orderNumber) ?: 
                return@withContext Result.failure(Exception("Order not found"))

            val result = collection.updateOne(
                Filters.eq("orderNumber", orderNumber),
                Updates.combine(
                    Updates.set("shippingDetails", shippingDetails),
                    Updates.set("lastUpdated", System.currentTimeMillis()),
                    Updates.inc("version", 1)
                )
            )

            if (result.modifiedCount > 0) {
                Result.success(getOrderByNumber(orderNumber)!!)
            } else {
                Result.failure(Exception("Failed to update shipping details"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOrderStats(customerId: Int): OrderStats = withContext(Dispatchers.IO) {
        val orders = getOrdersByCustomer(customerId)
        
        OrderStats(
            totalOrders = orders.size,
            pendingOrders = orders.count { it.status == OrderStatus.PENDING_PAYMENT },
            completedOrders = orders.count { it.status == OrderStatus.DELIVERED },
            totalSpent = Money(orders.sumOf { it.summary.total.amount })
        )
    }

    suspend fun getFilteredOrders(
        filters: List<Bson>,
        skip: Int = 0,
        limit: Int = 50
    ): Result<List<OrderModel>> = withContext(Dispatchers.IO) {
        try {
            val combinedFilter = if (filters.isEmpty()) {
                Filters.empty()
            } else {
                Filters.and(filters)
            }

            val orders = collection.find(combinedFilter)
                .sort(Sorts.descending("orderDate"))
                .skip(skip)
                .limit(limit)
                .toList()

            Result.success(orders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateOrderStatus(
        orderNumber: String,
        status: OrderStatus,
        updatedBy: String,
        location: String? = null,
        description: String? = null
    ): Result<OrderModel> = withContext(Dispatchers.IO) {
        try {
            val order = getOrderByNumber(orderNumber) ?:
                return@withContext Result.failure(Exception("Order not found"))

            val trackingEvent = TrackingEvent(
                status = status,
                timestamp = System.currentTimeMillis(),
                location = location,
                description = description,
                updatedBy = updatedBy
            )

            val result = collection.updateOne(
                Filters.eq("orderNumber", orderNumber),
                Updates.combine(
                    Updates.set("status", status),
                    Updates.push("tracking.events", trackingEvent),
                    Updates.set("tracking.currentStatus", status),
                    Updates.set("tracking.lastUpdated", System.currentTimeMillis()),
                    Updates.set("lastUpdated", System.currentTimeMillis()),
                    Updates.inc("version", 1)
                )
            )

            if (result.modifiedCount > 0) {
                Result.success(getOrderByNumber(orderNumber)!!)
            } else {
                Result.failure(Exception("Failed to update order status"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOrderTracking(orderNumber: String): Result<OrderTracking> = withContext(Dispatchers.IO) {
        try {
            val order = getOrderByNumber(orderNumber) ?:
                return@withContext Result.failure(Exception("Order not found"))
            
            Result.success(order.tracking)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class OrderStats(
    val totalOrders: Int,
    val pendingOrders: Int,
    val completedOrders: Int,
    val totalSpent: Money
)