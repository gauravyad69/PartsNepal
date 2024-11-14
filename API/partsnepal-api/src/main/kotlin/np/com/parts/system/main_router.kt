package np.com.parts.system

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTDecodeException
import np.com.parts.system.Services.ProductService
import np.com.parts.plugins.Auth.LoginRequest
import np.com.parts.plugins.Auth.UserSession
import np.com.parts.plugins.Auth.oldToken
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import np.com.parts.system.Routes.Orders.adminOrderRoutes
import np.com.parts.system.Routes.Orders.authenticatedOrderRoutes
import np.com.parts.system.Routes.Products.adminProductRoutes
import np.com.parts.system.Routes.Products.authenticatedProductRoutes
import np.com.parts.system.Routes.Products.unauthenticatedProductRoutes
import np.com.parts.system.Routes.User.adminUserRoutes
import np.com.parts.system.Routes.User.authenticatedUserRoutes
import np.com.parts.system.Services.OrderService
import np.com.parts.system.Services.UserService
import java.util.*



fun Route.applicationRoutes(productService: ProductService, orderService: OrderService, userService: UserService) {

    val secret = environment!!.config.property("jwt.secret").getString()
    val issuer = environment!!.config.property("jwt.issuer").getString()
    val audience = environment!!.config.property("jwt.audience").getString()
    val myRealm = environment!!.config.property("jwt.realm").getString()

//we probably don't need this
    post("/register") {
        val user = call.receive<LoginRequest>()
        if (user!=null) {
            val token = JWT.create()
                .withAudience(audience)
                .withIssuer(issuer)
                .withClaim("email", user.email)
                .withExpiresAt(Date(System.currentTimeMillis() + 3600000))
                .sign(Algorithm.HMAC256(secret))
            call.respond(hashMapOf("token" to token))
        } else {
            call.respondText("Invalid credentials", status = HttpStatusCode.Unauthorized)
        }
    }


    //we probably do need this
    post("/refresh") {
        val oldToken = call.receive<oldToken>()
        val userId = decodeUserId(oldToken.toString())
        if (userId!=null) {
            val token = JWT.create()
                .withAudience(audience)
                .withIssuer(issuer)
                .withClaim("userId", userId)
                .withExpiresAt(Date(System.currentTimeMillis() + 3600000))
                .sign(Algorithm.HMAC256(secret))
            call.respond(hashMapOf("token" to token))
        } else {
            call.respondText("Invalid credentials", status = HttpStatusCode.Unauthorized)
        }
    }


    unauthenticatedProductRoutes(productService)

//    authenticate("auth-jwt") {
//        get("/protected") {
//            val principal = call.principal<JWTPrincipal>()
//            val username = principal!!.payload.getClaim("username").asString()
//            call.respondText("Hello, $username!")
//        }
        adminProductRoutes(productService)
        authenticatedProductRoutes(productService)

        authenticatedOrderRoutes(orderService)
        adminOrderRoutes(orderService)

        authenticatedUserRoutes(userService)
    adminUserRoutes(userService)
//    }



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
