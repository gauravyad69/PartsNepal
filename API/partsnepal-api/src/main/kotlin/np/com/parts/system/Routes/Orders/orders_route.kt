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

fun Route.authenticatedOrderRoutes(orderService: OrderService) {
        route("/orders") {
            // GET - Get all orders for the current user
            get {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val customerId = principal!!.payload.getClaim("userId").asString()
                    val orders = orderService.getOrdersByCustomer(customerId)
                    call.respond(HttpStatusCode.OK, orders)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error fetching orders")
                }
            }

            // POST - Create a new order
            post {
                try {
                    val order = call.receive<OrderModel>()
                    val created = orderService.createOrder(order)
                    if (created) {
                        call.respond(HttpStatusCode.Created, "Order created successfully")
                    } else {
                        call.respond(HttpStatusCode.Conflict, "Order already exists")
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error creating order")
                }
            }
        }
    }

