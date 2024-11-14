package com.example.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.plugins.Auth.UserSession
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.application.ApplicationCallPipeline.ApplicationPhase.Plugins
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import java.util.*

fun Application.configureSecurity() {

    val secret = environment.config.property("jwt.secret").getString()
    val issuer = environment.config.property("jwt.issuer").getString()
    val audience = environment.config.property("jwt.audience").getString()
    val myRealm = environment.config.property("jwt.realm").getString()


    install(Sessions) {
        cookie<UserSession>("USER_SESSION") {
            cookie.path = "/"
            cookie.maxAgeInSeconds = 3600 // 1 hour
            cookie.secure = true // for HTTPS
            cookie.httpOnly = true // for security
            cookie.extensions["SameSite"] = "None"
        }
    }

    install(Authentication) {
        jwt("auth-jwt") {
            realm = myRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(secret))
                    .withAudience(audience)
                    .withIssuer(issuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(audience)) JWTPrincipal(credential.payload) else null
            }
        }


        session<UserSession>("USER_SESSION") {
            validate { session ->
                // Validate the session here (e.g., check if the user still exists in the database)
                if (session.username.isNotEmpty()) {
                    session
                } else {
                    null
                }
            }
//            challenge {
//                call.respondRedirect("/login")
//            }
        }
    }

    routing {
        authenticate("auth-jwt") {
            get("/protected") {
                val principal = call.principal<JWTPrincipal>()
                val username = principal!!.payload.getClaim("username").asString()
                call.respondText("Hello, $username!")
            }
        }

    }
}

//this is used authenticate the routes, gets the sessions and checks if the session exists in the server if it does, it enables the routes
fun Route.authenticatedRoute(build: Route.() -> Unit): Route {
    return createChild(object : RouteSelector() {
        override fun evaluate(context: RoutingResolveContext, segmentIndex: Int) = RouteSelectorEvaluation.Constant
    }).apply {
        intercept(Plugins) {
            val session = call.sessions.get<UserSession>()
            if (session == null) {
                call.respondText("Unauthorized", status = HttpStatusCode.Unauthorized)
                finish()
            }
        }
        build()
    }
}

