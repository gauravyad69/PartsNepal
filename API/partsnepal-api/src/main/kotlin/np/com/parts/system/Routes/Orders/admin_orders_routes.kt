package np.com.parts.system.Routes.Orders

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import np.com.parts.system.Models.DeliveryDetails
import np.com.parts.system.Models.OrderModel
import np.com.parts.system.Services.OrderService

fun Route.adminOrderRoutes(orderService: OrderService) {

        route("/orders") {
            // GET - Get all orders (with pagination)
            get {
                try {
                    val skip = call.parameters["skip"]?.toIntOrNull() ?: 0
                    val limit = call.parameters["limit"]?.toIntOrNull() ?: 50
                    val orders = orderService.getAllOrders(skip, limit)
                    call.respond(HttpStatusCode.OK, orders)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error fetching orders")
                }
            }

            // GET - Get order by ID
            get("/{orderId}") {
                try {
                    val orderId = call.parameters["orderId"]
                        ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid order ID")
                    val order = orderService.getOrderById(orderId)
                    if (order != null) {
                        call.respond(HttpStatusCode.OK, order)
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Order not found")
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error fetching order")
                }
            }

            // PUT - Update order status
            put("/{orderId}/status") {
                try {
                    val orderId = call.parameters["orderId"]
                        ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid order ID")
                    val newStatus = call.receive<String>()
                    val updated = orderService.updateOrderStatus(orderId, newStatus)
                    if (updated) {
                        call.respond(HttpStatusCode.OK, "Order status updated successfully")
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Order not found")
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error updating order status")
                }
            }

            // PUT - Update delivery details
            put("/{orderId}/delivery") {
                try {
                    val orderId = call.parameters["orderId"]
                        ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid order ID")
                    val deliveryDetails = call.receive<DeliveryDetails>()
                    val updated = orderService.updateDeliveryDetails(orderId, deliveryDetails)
                    if (updated) {
                        call.respond(HttpStatusCode.OK, "Delivery details updated successfully")
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Order not found")
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error updating delivery details")
                }
            }

            // DELETE - Delete an order
            delete("/{orderId}") {
                try {
                    val orderId = call.parameters["orderId"]
                        ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid order ID")
                    val deleted = orderService.deleteOrder(orderId)
                    if (deleted) {
                        call.respond(HttpStatusCode.OK, "Order deleted successfully")
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Order not found")
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error deleting order")
                }
            }

            // GET - Get orders by status
            get("/status/{status}") {
                try {
                    val status = call.parameters["status"]
                        ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid order status")
                    val orders = orderService.getOrdersByStatus(status)
                    call.respond(HttpStatusCode.OK, orders)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error fetching orders by status")
                }
            }

            // GET - Get orders by date range
            get("/date-range") {
                try {
                    val startDate = call.parameters["startDate"]?.toLongOrNull()
                    val endDate = call.parameters["endDate"]?.toLongOrNull()
                    if (startDate == null || endDate == null) {
                        return@get call.respond(HttpStatusCode.BadRequest, "Invalid date range")
                    }
                    val orders = orderService.getOrdersByDateRange(startDate, endDate)
                    call.respond(HttpStatusCode.OK, orders)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error fetching orders by date range")
                }
            }
        }

}