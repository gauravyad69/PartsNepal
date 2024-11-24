package np.com.parts.system

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTDecodeException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import np.com.parts.system.Services.ProductService
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import np.com.parts.system.Routes.Orders.adminOrderRoutes
import np.com.parts.system.Routes.Orders.authenticatedOrderRoutes
import np.com.parts.system.Routes.Products.adminProductRoutes
import np.com.parts.system.Routes.Products.authenticatedProductRoutes
import np.com.parts.system.Routes.Products.unauthenticatedProductRoutes
import np.com.parts.system.Routes.Cart.cartRoutes
import np.com.parts.system.Routes.User.adminUserRoutes
import np.com.parts.system.Routes.User.authenticatedUserRoutes
import np.com.parts.system.Services.OrderService
import np.com.parts.system.Services.UserService
import np.com.parts.system.auth.AuthenticationService
import np.com.parts.system.Services.CartService


fun Route.applicationRoutes(productService: ProductService, orderService: OrderService, userService: UserService, cartService: CartService) {
    val jwtConfig = AuthenticationService.JWTConfig(
        secret = environment?.config!!.property("jwt.secret").getString(),
        issuer = environment?.config!!.property("jwt.issuer").getString(),
        audience = environment?.config!!.property("jwt.audience").getString(),
        realm = environment?.config!!.property("jwt.realm").getString()
    )

    unauthenticatedProductRoutes(productService)

    authenticate("auth-jwt") {
        get("/protected") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal!!.payload.getClaim("userId").asInt()
            if (principal==null){
                return@get call.respond(HttpStatusCode.Unauthorized)
            }
            if (userId==null){
                return@get call.respond(HttpStatusCode.Unauthorized)
            }
            call.respond(HttpStatusCode.OK)

        }
        cartRoutes(cartService)
        
        adminProductRoutes(productService)
        authenticatedProductRoutes(productService)

        authenticatedOrderRoutes(orderService)
        adminOrderRoutes(orderService)

        authenticatedUserRoutes(userService)
    adminUserRoutes(userService)
    }



}

fun decodeUserId(jwtToken: String): Int? {
    return try {
        val decodedJWT = JWT.decode(jwtToken)
        decodedJWT.getClaim("userId").asInt()
    } catch (e: JWTDecodeException) {
        println("Error decoding the JWT token: ${e.message}")
        null
    } catch (e: Exception) {
        println("Unexpected error: ${e.message}")
        null
    }
}
