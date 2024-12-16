package np.com.parts.system

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTDecodeException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import np.com.parts.system.Routes.Orders.adminOrderRoutes
import np.com.parts.system.Routes.Orders.authenticatedOrderRoutes
import np.com.parts.system.Routes.Products.adminProductRoutes
import np.com.parts.system.Routes.Products.authenticatedProductRoutes
import np.com.parts.system.Routes.Products.unauthenticatedProductRoutes
import np.com.parts.system.Routes.Cart.cartRoutes
import np.com.parts.system.Routes.Cart.khaltiRoutes
import np.com.parts.system.Routes.Orders.adminDiscountRoutes
import np.com.parts.system.Routes.User.adminUserRoutes
import np.com.parts.system.Routes.User.authenticatedUserRoutes
import np.com.parts.system.Routes.pastebinApi
import np.com.parts.system.Services.*
import np.com.parts.system.auth.AuthenticationService


fun Route.applicationRoutes(productService: ProductService, orderService: OrderService, userService: UserService, cartService: CartService, paymentService: PaymentService, pasteService: PasteService) {
    val jwtConfig = AuthenticationService.JWTConfig(
        secret = environment?.config!!.property("jwt.secret").getString(),
        issuer = environment?.config!!.property("jwt.issuer").getString(),
        audience = environment?.config!!.property("jwt.audience").getString(),
        realm = environment?.config!!.property("jwt.realm").getString()
    )

    unauthenticatedProductRoutes(productService)


    authenticate("auth-jwt") {
        pastebinApi(pasteService)
        khaltiRoutes(paymentService)

        cartRoutes(cartService)
        
        adminProductRoutes(productService)
        authenticatedProductRoutes(productService)

        authenticatedOrderRoutes(orderService)
        adminDiscountRoutes(orderService)
        adminOrderRoutes(orderService)

        authenticatedUserRoutes(userService)
    adminUserRoutes(userService)
    }



        get("/payment-success") {
            call.respondText(
                """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Payment Verification</title>
                    <style>
                        body {
                            display: flex;
                            justify-content: center;
                            align-items: center;
                            height: 100vh;
                            background-color: #f0f0f0;
                            font-family: Arial, sans-serif;
                        }
                        h1 {
                            color: #c1c400;
                        }
                    </style>
                </head>
                <body>
                    <h1>Please wait, Your payment is being verified!</h1>
                </body>
                </html>
                """.trimIndent(),
                ContentType.Text.Html
            )
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
