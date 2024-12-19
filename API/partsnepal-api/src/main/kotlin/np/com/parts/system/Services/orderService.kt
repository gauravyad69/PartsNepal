package np.com.parts.system.Services

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import np.com.parts.system.Models.*
import org.bson.conversions.Bson
import org.litote.kmongo.getCollection
import org.litote.kmongo.inc
import org.litote.kmongo.json
import org.litote.kmongo.updateOne

class OrderService(
    private val database: MongoDatabase,
    private val userService: UserService,
    private val cartService: CartService,
) {
    private lateinit var collection: MongoCollection<OrderModel>
    private lateinit var discountCollection: MongoCollection<DiscountModel>

    init {


        try {
            database.createCollection("orders")
            database.createCollection("discount_collection")
        } catch (e: Exception) {
            // Collection already exists
        }

        collection = database.getCollection<OrderModel>("orders")
        discountCollection = database.getCollection<DiscountModel>("discount_collection")
        setupIndexes()
    }

    private fun setupIndexes() {
        collection.createIndex(Indexes.ascending("orderNumber"), IndexOptions().unique(true))
        collection.createIndex(Indexes.ascending("customer.id"))
        collection.createIndex(Indexes.ascending("status"))
        collection.createIndex(Indexes.ascending("orderDate"))

        discountCollection.createIndex(Indexes.ascending("discountCode"))
    }

    suspend fun createOrder(userId: Int, request: CreateOrderRequest): Result<OrderModel> =
        withContext(Dispatchers.IO) {
            try {
                val user = userService.getUserById(UserId(userId)) ?: 
                    return@withContext Result.failure(Exception("User not found"))


                var summary = calculateOrderSummary(request.items, request.discountCode)



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
                    summary = summary,
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

    suspend fun getDiscountAmountWithCode(code: String, minimumAmount:Money): Result<DiscountModelAmount> = withContext(Dispatchers.IO) {
        try {
            val discount = discountCollection.find(Filters.eq("discountCode", code)).firstOrNull()

            if (discount != null) {
                if (discount.discountAmount.minimumAmount!=minimumAmount) return@withContext Result.failure(CancellationException("Minimum amount doesn't meet"))
                Result.success(DiscountModelAmount(
                    maximumAmount = discount.discountAmount.maximumAmount,
                    minimumAmount = discount.discountAmount.maximumAmount,
                    forShipping = discount.discountAmount.forShipping,
                    forSubtotal = discount.discountAmount.forSubtotal
                ))
            } else {
                Result.failure(NoSuchElementException("Discount code not found: $code"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun getDiscountWithCode(code: String): Result<DiscountModel> = withContext(Dispatchers.IO) {
        try {
            val discount = discountCollection.find(Filters.eq("discountCode", code)).firstOrNull()
            if (discount != null) {
                println("Discount model fetched")
                Result.success(discount)
            } else {
                Result.failure(NoSuchElementException("Discount code not found: $code"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateOrderStatus(
        orderNumber: String,
        status: OrderStatus, 
        updatedBy: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
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
                Result.success(true)
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

    private suspend fun calculateOrderSummary(items: List<LineItem>, discountCode: String?): OrderSummary {
        var subtotal = Money(0)
        var totalDiscount = Money(0)
        val shippingCostBase = Money(10000) // Default shipping cost
        var shippingCost = shippingCostBase

        // Calculate subtotal and discounts
        items.forEach { item ->
            subtotal = Money(subtotal.amount + item.totalPrice.amount)

            // Calculate item-level discount
            item.discount?.let { discount ->
                val discountAmount = when (discount.type) {
                    DiscountType.PERCENTAGE -> Money((item.totalPrice.amount * (discount.amount.amount.toDouble() / 100)).toLong())
                    DiscountType.FIXED_AMOUNT -> discount.amount
                }

                // Apply both item and discount-code level discounts
                totalDiscount = Money(totalDiscount.amount + discountAmount.amount)
            }
        }
        // Retrieve discount amount for the discount code
        val qudoDiscount = discountCode?.let { getDiscountWithCode(it) }!!


        // Apply discount code discount if successful
        if (qudoDiscount.isSuccess) {
            totalDiscount = Money(totalDiscount.amount.plus(qudoDiscount.getOrThrow().discountAmount.forShipping.amount).plus(qudoDiscount.getOrThrow().discountAmount.forSubtotal.amount))
        }

        // Calculate tax (13% VAT)
        val tax = Money((subtotal.amount * 0.13).toLong())

        // Make shipping free if subtotal exceeds threshold
        if (subtotal.amount >= 500000) {
            shippingCost = Money(0)
        }

        // Calculate the total amount
        val total = Money(subtotal.amount - totalDiscount.amount + shippingCost.amount ) //todo calc  this : + tax.amount

        // Construct the order summary
        val orderSummary = OrderSummary(
            subtotal = subtotal,
            discount = if (totalDiscount.amount > 0) totalDiscount else null,
            discountCode = discountCode,
            shippingCost = shippingCost,
            tax = tax,
            total = total
        )

        println("Total Discount: $totalDiscount")
        println("Calculated Order Summary: $orderSummary")

        return orderSummary
    }

    private fun generateOrderNumber(): String {
        val timestamp = System.currentTimeMillis().toSeconds()
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


        //this is seprate from discountcode collection,
    suspend fun updateDiscountInOrder(
        orderNumber: String,
        discount: Money? = null,
        discountCode: String? = null,
        isShippingFree: Boolean
    ): Result<OrderModel> = withContext(Dispatchers.IO) {

        try {
            val order = getOrderByNumber(orderNumber) ?:
            return@withContext Result.failure(Exception("Order not found"))

            val subTotal=order.summary.subtotal.amount

            val newDiscount = Money(discount?.amount ?: 0)
            val shipping = if (isShippingFree) Money(0) else order.summary.shippingCost

            val newOrderSummary= OrderSummary(
                subtotal= Money(order.summary.subtotal.amount),
                discount = newDiscount,
                shippingCost = shipping,
                discountCode = discountCode,
                total = Money(subTotal.minus(newDiscount.amount)+shipping.amount)
            )

            val result = collection.updateOne(
                Filters.eq("orderNumber", orderNumber),
                Updates.combine(
                    Updates.set("orderSummary", newOrderSummary),
                    Updates.set("lastUpdated", System.currentTimeMillis()),
                    Updates.inc("version", 1)
                )
            )

            if (result.modifiedCount > 0) {
                Result.success(getOrderByNumber(orderNumber)!!)
            } else {
                Result.failure(Exception("Failed to update discount and freeshipping"))
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

    suspend fun updateOrderTrackingStatus(
        orderNumber: String,
        status: OrderStatus,
        updatedBy: String,
        location: String? = null,
        description: String? = null
    ): Result<Boolean> = withContext(Dispatchers.IO) {
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
                Result.success(true)
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
    suspend fun getDiscountCodeDetailsByCode(code: String): DiscountModel? = withContext(Dispatchers.IO) {
        discountCollection.find(Filters.eq("discountCode", code)).firstOrNull()
    }

    suspend fun getAllDiscountCodes(): List<DiscountModel> = withContext(Dispatchers.IO) {
        discountCollection.find()
            .sort(Sorts.descending("lastUpdated"))
            .toList()
    }

    suspend fun createDiscountCode(userId: Int, request: DiscountModel): Result<DiscountModel> =
        withContext(Dispatchers.IO) {
            try {
                val user = userService.getUserById(UserId(userId)) ?:
                return@withContext Result.failure(Exception("User not found"))

                discountCollection.insertOne(request)
                Result.success(request)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

suspend fun updateDiscountCode( request: DiscountModel): Result<DiscountModel> =
    withContext(Dispatchers.IO) {
        try {
            discountCollection.updateOne(
                Filters.eq("discountCode", request.discountCode),
                Updates.combine(
                    // Update fields dynamically based on the request object
                    Updates.set("discountAmount", request.discountAmount),
                    Updates.set("discountDetails", request.discountDetails),
                    Updates.set("discountCode", request.discountCode),
                    Updates.set("lastUpdated", System.currentTimeMillis()), // Always update timestamp
                    Updates.inc("version", 1) // Increment version
                )
            )

            Result.success(request)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun deleteDiscountCodeDetails(code: String): Boolean = withContext(Dispatchers.IO) {
        val result = discountCollection.deleteOne(Filters.eq("discountCode", code))
        result.deletedCount > 0
    }


}

data class OrderStats(
    val totalOrders: Int,
    val pendingOrders: Int,
    val completedOrders: Int,
    val totalSpent: Money
)

data class DiscountStats(
    val discount: Money?,
    val discountForSubtotal: Money?,
    val discountForShipping: Money?,
)