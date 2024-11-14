package com.example.plugins.User

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTDecodeException
import com.example.plugins.Auth.LoginRequest
import com.example.plugins.Auth.UserSession
import com.example.plugins.Auth.oldToken
import com.example.plugins.TelegramUserService
import com.example.plugins.User.Routes.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import java.util.*

fun Route.telegramAuthUserRoutes(telegramUserService: TelegramUserService) {

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
                .withClaim("userId", user.userId)
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

    // Login route
    post("/login") {
        val loginRequest = call.receive<LoginRequest>()
        val userId = loginRequest.userId
        val username = loginRequest.username
        if (telegramUserService.userExists(userId)) {
        call.sessions.set(UserSession(userId, username))
            VersionCheckResult.OK
            call.respond("success")

        } else {
        call.respond("failed")
    }
    }

    // Logout route
    get("/logout") {
        call.sessions.clear<UserSession>()
        call.respond("logged out")

   }

    post("/exists") {
        val user = call.receive<UserIdAndUsername>()

        if (user.userId != null) {
            val exists = telegramUserService.userExists(user.userId)
            if (exists){
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

    }

    authenticate("auth-jwt") {
//        get("/protected") {
//            val principal = call.principal<JWTPrincipal>()
//            val username = principal!!.payload.getClaim("username").asString()
//            call.respondText("Hello, $username!")
//        }
        telegramUserRoutes(telegramUserService)
        telegramUserUpdateRoutes(telegramUserService)
        telegramUserExtraRoutes(telegramUserService)
        telegramUserReferralRoutes(telegramUserService)
        telegramUserUpgradeInfoRoutes(telegramUserService)
        telegramUserBalanceInfoRoutes(telegramUserService)
    }




//    // Protected routes
//    authenticatedRoute {
//        telegramUserFirstTimeRoute(telegramUserService)
//        telegramUserUpdateRoutes(telegramUserService)
//        telegramUserExtraRoutes(telegramUserService)
//    }

}

// Middleware to check session before accessing protected routes
fun Route.withAuth(build: Route.() -> Unit): Route {
    return createChild(object : RouteSelector() {
        override fun evaluate(context: RoutingResolveContext, segmentIndex: Int) = RouteSelectorEvaluation.Constant
    }).apply {
        intercept(ApplicationCallPipeline.Features) {
            if (call.sessions.get<UserSession>() == null) {
                call.respondRedirect("/login")
                return@intercept finish()
            }
        }
        build()
    }
}
fun decodeUserId(jwtToken: String): Int? {
    return try {
        val decodedJWT = JWT.decode(jwtToken)
        decodedJWT.getClaim("userId").Int()
    } catch (e: JWTDecodeException) {
        println("Error decoding the JWT token: ${e.message}")
        null
    } catch (e: Exception) {
        println("Unexpected error: ${e.message}")
        null
    }
}
