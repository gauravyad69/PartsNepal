// AdminOrderRoutes.kt
package np.com.parts.system.Routes.Orders

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import np.com.parts.system.Models.*
import np.com.parts.system.Services.OrderService
import kotlinx.serialization.Serializable

// Data classes for requests
@Serializable
data class UpdateOrderStatusRequest(
    val status: OrderStatus,
    val location: String? = null,
    val description: String? = null,
    val updatedBy: String
)

@Serializable
data class UpdatePaymentRequest(
    val status: PaymentStatus,
    val transactionId: String? = null
)

@Serializable
data class UpdateTrackingRequest(
    val trackingNumber: String,
    val carrier: String
)

@Serializable
data class CreateOrderRequest(
    val items: List<OrderItem>,
    val shipping: ShippingDetails,
    val payment: PaymentDetails
) {
    fun toOrderModel(customerId: String): OrderModel {
        val now = System.currentTimeMillis()
        
        // Calculate order summary
        val subtotal = items.sumOf { it.finalPrice.amount }.toBigDecimal().toLong()
        val shippingCost = shipping.cost.amount.toBigDecimal().toLong()
        val totalQuantity = items.sumOf { it.quantity }
        
        return OrderModel(
            orderNumber = generateOrderNumber(), // You'll need to implement this
            orderDate = now,
            status = OrderStatus.PENDING_PAYMENT,
            customer = CustomerInfo(
                id = customerId,
                email = shipping.address.recipient.email ?: "",
                phone = shipping.address.recipient.phone,
                name = shipping.address.recipient.name,
                type = CustomerType.INDIVIDUAL // Default to INDIVIDUAL, can be updated later
            ),
            items = items,
            payment = payment,
            summary = OrderSummary(
                subtotal = Money(subtotal.toLong()),
                discount = Money(0.toBigDecimal().toLong()), // Initial discount is 0
                tax = TaxInfo(
                    amount = Money(0.toBigDecimal().toLong()), // You might want to calculate tax based on your business logic
                    rate = 0.0
                ),
                shipping = shipping.cost,
                total = Money(subtotal + shippingCost),
                totalItems = items.size,
                totalQuantity = totalQuantity
            ),
            shipping = shipping,
            tracking = OrderTracking(
                history = listOf(
                    TrackingEvent(
                        status = OrderStatus.PENDING_PAYMENT,
                        timestamp = now,
                        description = "Order created",
                        updatedBy = "SYSTEM"
                    )
                )
            ),
            metadata = OrderMetadata(
                source = OrderSource.WEB, // You might want to make this configurable
                createdAt = now
            )
        )
    }
}

// Helper function to generate order number (you'll need to implement this)
private fun generateOrderNumber(): String {
    // Example implementation:
    val timestamp = System.currentTimeMillis()
    val random = (1000..9999).random()
    return "ORD-${timestamp}-${random}"
}

fun Route.adminOrderRoutes(orderService: OrderService) {
    route("/admin/orders") {
        // GET - Get all orders (with pagination and sorting)
        get {
            try {
                val skip = call.parameters["skip"]?.toIntOrNull() ?: 0
                val limit = call.parameters["limit"]?.toIntOrNull() ?: 50
                val sortBy = call.parameters["sortBy"] ?: "orderDate"
                val descending = call.parameters["descending"]?.toBoolean() ?: true

                val orders = orderService.getAllOrders(skip, limit, sortBy, descending)
                call.respond(HttpStatusCode.OK, orders)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error fetching orders: ${e.message}")
            }
        }

        // GET - Get order by number
        get("/{orderNumber}") {
            try {
                val orderNumber = call.parameters["orderNumber"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid order number")
                val order = orderService.getOrderByNumber(orderNumber)
                if (order != null) {
                    call.respond(HttpStatusCode.OK, order)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Order not found")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error fetching order: ${e.message}")
            }
        }

        // PUT - Update order status
        put("/{orderNumber}/status") {
            try {
                val orderNumber = call.parameters["orderNumber"]
                    ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid order number")
                val request = call.receive<UpdateOrderStatusRequest>()

                val updated = orderService.updateOrderStatus(
                    orderNumber = orderNumber,
                    newStatus = request.status,
                    location = request.location,
                    description = request.description,
                    updatedBy = request.updatedBy
                )

                if (updated) {
                    call.respond(HttpStatusCode.OK, "Order status updated successfully")
                } else {
                    call.respond(HttpStatusCode.NotFound, "Order not found")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error updating order status: ${e.message}")
            }
        }

        // PUT - Update payment status
        put("/{orderNumber}/payment") {
            try {
                val orderNumber = call.parameters["orderNumber"]
                    ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid order number")
                val request = call.receive<UpdatePaymentRequest>()

                val updated = orderService.updatePaymentStatus(
                    orderNumber = orderNumber,
                    paymentStatus = request.status,
                    transactionId = request.transactionId
                )

                if (updated) {
                    call.respond(HttpStatusCode.OK, "Payment status updated successfully")
                } else {
                    call.respond(HttpStatusCode.NotFound, "Order not found")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error updating payment status: ${e.message}")
            }
        }

        // PUT - Update shipping details
        put("/{orderNumber}/shipping") {
            try {
                val orderNumber = call.parameters["orderNumber"]
                    ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid order number")
                val shippingDetails = call.receive<ShippingDetails>()
                val updated = orderService.updateShippingDetails(orderNumber, shippingDetails)
                if (updated) {
                    call.respond(HttpStatusCode.OK, "Shipping details updated successfully")
                } else {
                    call.respond(HttpStatusCode.NotFound, "Order not found")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error updating shipping details: ${e.message}")
            }
        }

        // POST - Add order note
        post("/{orderNumber}/notes") {
            try {
                val orderNumber = call.parameters["orderNumber"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest, "Invalid order number")
                val note = call.receive<OrderNote>()
                val updated = orderService.addOrderNote(orderNumber, note)
                if (updated) {
                    call.respond(HttpStatusCode.Created, "Note added successfully")
                } else {
                    call.respond(HttpStatusCode.NotFound, "Order not found")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error adding note: ${e.message}")
            }
        }

        // PUT - Update tracking information
        put("/{orderNumber}/tracking") {
            try {
                val orderNumber = call.parameters["orderNumber"]
                    ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid order number")
                val request = call.receive<UpdateTrackingRequest>()

                val updated = orderService.updateTracking(
                    orderNumber = orderNumber,
                    trackingNumber = request.trackingNumber,
                    carrier = request.carrier
                )

                if (updated) {
                    call.respond(HttpStatusCode.OK, "Tracking information updated successfully")
                } else {
                    call.respond(HttpStatusCode.NotFound, "Order not found")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error updating tracking: ${e.message}")
            }
        }

        // GET - Search orders
        get("/search") {
            try {
                val customerName = call.parameters["customerName"]
                val customerEmail = call.parameters["customerEmail"]
                val orderStatus = call.parameters["orderStatus"]?.let { OrderStatus.valueOf(it) }
                val paymentStatus = call.parameters["paymentStatus"]?.let { PaymentStatus.valueOf(it) }
                val startDate = call.parameters["startDate"]?.toLongOrNull()
                val endDate = call.parameters["endDate"]?.toLongOrNull()
                val skip = call.parameters["skip"]?.toIntOrNull() ?: 0
                val limit = call.parameters["limit"]?.toIntOrNull() ?: 50

                val orders = orderService.searchOrders(
                    customerName = customerName,
                    customerEmail = customerEmail,
                    orderStatus = orderStatus,
                    paymentStatus = paymentStatus,
                    startDate = startDate,
                    endDate = endDate,
                    skip = skip,
                    limit = limit
                )

                call.respond(HttpStatusCode.OK, orders)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error searching orders: ${e.message}")
            }
        }

        // GET - Get orders by status
        get("/status/{status}") {
            try {
                val status = call.parameters["status"]?.let {
                    OrderStatus.valueOf(it)
                } ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid order status")

                val orders = orderService.getOrdersByStatus(status)
                call.respond(HttpStatusCode.OK, orders)
            } catch (e:IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, "Invalid order status value")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error fetching orders by status: ${e.message}")
            }
        }

        // GET - Get orders by customer type
        get("/customer-type/{type}") {
            try {
                val customerType = call.parameters["type"]?.let {
                    CustomerType.valueOf(it)
                } ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid customer type")

                val orders = orderService.getOrdersByCustomerType(customerType)
                call.respond(HttpStatusCode.OK, orders)
            } catch (e:IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, "Invalid customer type value")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error fetching orders by customer type: ${e.message}")
            }
        }
    }
}

