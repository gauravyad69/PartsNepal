// AdminOrderRoutes.kt
package np.com.parts.system.Routes.Orders

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import np.com.parts.system.Models.*
import np.com.parts.system.Services.OrderService
import org.bson.conversions.Bson
import com.mongodb.client.model.Filters
import kotlinx.serialization.Serializable

// Request models for admin operations
@Serializable
data class UpdateOrderStatusRequest(
    val status: OrderStatus,
    val updatedBy: String,
    val note: String? = null
)

@Serializable
data class UpdatePaymentRequest(
    val status: PaymentStatus,
    val transactionId: String? = null,
    val updatedBy: String
)

@Serializable
data class OrderFilters(
    val status: OrderStatus? = null,
    val fromDate: Long? = null,
    val toDate: Long? = null,
    val customerId: Int? = null
)

fun Route.adminOrderRoutes(orderService: OrderService) {
    route("/admin/orders") {
        // GET - Get all orders with filtering and pagination
        get {
            try {
                val filters = OrderFilters(
                    status = call.parameters["status"]?.let { OrderStatus.valueOf(it.uppercase()) },
                    fromDate = call.parameters["fromDate"]?.toLongOrNull(),
                    toDate = call.parameters["toDate"]?.toLongOrNull(),
                    customerId = call.parameters["customerId"]?.toIntOrNull()
                )
                
                val skip = call.parameters["skip"]?.toIntOrNull() ?: 0
                val limit = call.parameters["limit"]?.toIntOrNull() ?: 50

                val mongoFilters = buildMongoFilters(filters)
                val orders = orderService.getFilteredOrders(mongoFilters, skip, limit)
                call.respond(HttpStatusCode.OK, orders)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, "Invalid filter parameters: ${e.message}")
            } catch (e: Exception) {
                application.log.error("Error fetching orders", e)
                call.respond(HttpStatusCode.InternalServerError, "Error fetching orders: ${e.message}")
            }
        }

        // GET - Get specific order
        get("{orderNumber}") {
            try {
                val orderNumber = call.parameters["orderNumber"] 
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Order number is required")

                val order = orderService.getOrderByNumber(orderNumber)
                if (order != null) {
                    call.respond(HttpStatusCode.OK, order)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Order not found")
                }
            } catch (e: Exception) {
                application.log.error("Error fetching order", e)
                call.respond(HttpStatusCode.InternalServerError, "Error fetching order: ${e.message}")
            }
        }

        // PATCH - Update order status
        patch("{orderNumber}/status") {
            try {
                val orderNumber = call.parameters["orderNumber"] 
                    ?: return@patch call.respond(HttpStatusCode.BadRequest, "Order number is required")
                
                val request = call.receive<UpdateOrderStatusRequest>()
                
                val result = orderService.updateOrderStatus(
                    orderNumber = orderNumber,
                    status = request.status,
                    updatedBy = request.updatedBy
                )

                result.fold(
                    onSuccess = { order -> 
                        call.respond(HttpStatusCode.OK, order)
                    },
                    onFailure = { error ->
                        when (error) {
                            is NoSuchElementException -> call.respond(HttpStatusCode.NotFound, "Order not found")
                            else -> call.respond(HttpStatusCode.BadRequest, error.message ?: "Failed to update status")
                        }
                    }
                )
            } catch (e: Exception) {
                application.log.error("Error updating order status", e)
                call.respond(HttpStatusCode.InternalServerError, "Error updating order status: ${e.message}")
            }
        }

        // PATCH - Update payment status
        patch("{orderNumber}/payment") {
            try {
                val orderNumber = call.parameters["orderNumber"] 
                    ?: return@patch call.respond(HttpStatusCode.BadRequest, "Order number is required")
                
                val request = call.receive<UpdatePaymentRequest>()
                
                val result = orderService.updatePaymentStatus(
                    orderNumber = orderNumber,
                    paymentStatus = request.status,
                    transactionId = request.transactionId
                )

                result.fold(
                    onSuccess = { order -> 
                        call.respond(HttpStatusCode.OK, order)
                    },
                    onFailure = { error ->
                        when (error) {
                            is NoSuchElementException -> call.respond(HttpStatusCode.NotFound, "Order not found")
                            else -> call.respond(HttpStatusCode.BadRequest, error.message ?: "Failed to update payment")
                        }
                    }
                )
            } catch (e: Exception) {
                application.log.error("Error updating payment status", e)
                call.respond(HttpStatusCode.InternalServerError, "Error updating payment: ${e.message}")
            }
        }

        // DELETE - Cancel order
        delete("{orderNumber}") {
            try {
                val orderNumber = call.parameters["orderNumber"] 
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, "Order number is required")
                
                val result = orderService.updateOrderStatus(
                    orderNumber = orderNumber,
                    status = OrderStatus.CANCELLED,
                    updatedBy = "admin"
                )
                
                result.fold(
                    onSuccess = { 
                        call.respond(HttpStatusCode.OK, "Order cancelled successfully")
                    },
                    onFailure = { error ->
                        when (error) {
                            is NoSuchElementException -> call.respond(HttpStatusCode.NotFound, "Order not found")
                            else -> call.respond(HttpStatusCode.BadRequest, error.message ?: "Failed to cancel order")
                        }
                    }
                )
            } catch (e: Exception) {
                application.log.error("Error cancelling order", e)
                call.respond(HttpStatusCode.InternalServerError, "Error cancelling order: ${e.message}")
            }
        }
    }
}

private fun buildMongoFilters(filters: OrderFilters): List<Bson> {
    return buildList {
        filters.status?.let { add(Filters.eq("status", it)) }
        filters.customerId?.let { add(Filters.eq("customer.id", it)) }
        
        if (filters.fromDate != null && filters.toDate != null) {
            add(Filters.and(
                Filters.gte("orderDate", filters.fromDate),
                Filters.lte("orderDate", filters.toDate)
            ))
        }
    }
}

