package np.com.parts.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import np.com.parts.system.Services.UserService
import np.com.parts.system.auth.AuthenticationService
import java.util.*

fun Application.configureSecurity() {
    val jwtConfig = AuthenticationService.JWTConfig(
        secret = environment.config.property("jwt.secret").getString(),
        issuer = environment.config.property("jwt.issuer").getString(),
        audience = environment.config.property("jwt.audience").getString(),
        realm = environment.config.property("jwt.realm").getString()
    )

    install(Authentication) {
        jwt("auth-jwt") {
            realm = jwtConfig.realm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtConfig.secret))
                    .withAudience(jwtConfig.audience)
                    .withIssuer(jwtConfig.issuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("userId").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}

//fun Application.configureAuthRoutes(userService: UserService) {
//    val jwtConfig = AuthenticationService.JWTConfig(
//        secret = environment.config.property("jwt.secret").getString(),
//        issuer = environment.config.property("jwt.issuer").getString(),
//        audience = environment.config.property("jwt.audience").getString(),
//        realm = environment.config.property("jwt.realm").getString()
//    )
//
//    val authService = AuthenticationService(userService, jwtConfig)
//    // ... rest of the routing configuration
//}