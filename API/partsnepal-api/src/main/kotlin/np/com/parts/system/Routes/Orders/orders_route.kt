package np.com.parts.system.Routes.Orders

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import np.com.parts.system.Models.CreateOrderRequest
import np.com.parts.system.Services.OrderService

// AuthenticatedOrderRoutes.kt
fun Route.authenticatedOrderRoutes(orderService: OrderService) {
    get("/protected") {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal!!.payload.getClaim("userId").asInt() ?: return@get call.respond(HttpStatusCode.Unauthorized)

        call.respond(HttpStatusCode.OK)
    }
    route("/orders") {
        // GET - Get all orders for the current user
        get {
            try {
                val principal = call.principal<JWTPrincipal>()
                val customerId = principal!!.payload.getClaim("userId").asInt()
                val skip = call.parameters["skip"]?.toIntOrNull() ?: 0
                val limit = call.parameters["limit"]?.toIntOrNull() ?: 50

                val orders = orderService.getOrdersByCustomer(customerId)
                call.respond(HttpStatusCode.OK, orders)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error fetching orders: ${e.message}")
            }
        }

        // POST - Create a new order
        post {
            try {
                val principal = call.principal<JWTPrincipal>()
                val customerId = principal!!.payload.getClaim("userId").asInt()
                val orderRequest = call.receive<CreateOrderRequest>()

                // Ensure the order is created for the authenticated user
//                val order = orderRequest.toOrderModel(customerId)
                val created = orderService.createOrder(customerId,orderRequest)

                if (created.isSuccess) {
                    call.respond(HttpStatusCode.Created, created.getOrThrow())
                } else {
                    call.respond(HttpStatusCode.Conflict, "Order already exists")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error creating order: ${e.message}")
            }
        }

        // GET - Get specific order
        get("/{orderNumber}") {
            try {
                val principal = call.principal<JWTPrincipal>()
                val customerId = principal!!.payload.getClaim("userId").asInt()
                val orderNumber = call.parameters["orderNumber"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid order number")

                val order = orderService.getOrderByNumber(orderNumber)

                if (order != null) {
                    // Ensure the order belongs to the authenticated user
                    if (order.customer.id == customerId) {
                        call.respond(HttpStatusCode.OK, order)
                    } else {
                        call.respond(HttpStatusCode.Forbidden, "Access denied")
                    }
                } else {
                    call.respond(HttpStatusCode.NotFound, "Order not found")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error fetching order: ${e.message}")
            }
        }
    }
}