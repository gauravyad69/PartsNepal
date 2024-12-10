package np.com.parts.system.Routes.Auth

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import np.com.parts.system.Services.UserService
import np.com.parts.system.auth.AuthenticationService
import np.com.parts.system.auth.ErrorResponse
import np.com.parts.system.auth.LoginRequest
import np.com.parts.system.auth.RegisterRequest


fun Application.configureAuthRoutes(userService: UserService) {
    val jwtConfig = AuthenticationService.JWTConfig(
        secret = environment.config.property("jwt.secret").getString(),
        issuer = environment.config.property("jwt.issuer").getString(),
        audience = environment.config.property("jwt.audience").getString(),
        realm = environment.config.property("jwt.realm").getString()
    )
    val authService = AuthenticationService(userService, jwtConfig)

    routing {
        post("/auth/register") {
            try {
                val request = call.receive<RegisterRequest>()
                val result = authService.register(request)

                result.fold(
                        onSuccess = { response ->
                                call.respond(HttpStatusCode.Created, response)
                        },
                        onFailure = { error ->
                                val code = when {
                                error.message?.contains("already registered") == true -> "DUPLICATE_USER"
                            else -> "REGISTRATION_ERROR"
                        }
                call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(error.message ?: "Registration failed", code)
                        )
                    }
                )
            } catch (e: Exception) {
                call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(e.message ?: "Invalid request", "INVALID_REQUEST")
                )
            }
        }

        post("/auth/login") {
            try {
                val request = call.receive<LoginRequest>()
                val result = authService.login(request)

                result.fold(
                        onSuccess = { response ->
                                call.respond(HttpStatusCode.OK, response)
                        },
                        onFailure = { error ->
                                val (status, code) = when {
                                error.message?.contains("Invalid credentials") == true ->
                HttpStatusCode.Unauthorized to "INVALID_CREDENTIALS"
                error.message?.contains("Account is") == true ->
                HttpStatusCode.Forbidden to "ACCOUNT_INACTIVE"
                            else -> HttpStatusCode.InternalServerError to "LOGIN_ERROR"
                        }
                call.respond(
                        status,
                        ErrorResponse(error.message ?: "Login failed", code)
                        )
                    }
                )
            } catch (e: Exception) {
                call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(e.message ?: "Invalid request", "INVALID_REQUEST")
                )
            }
        }
    }


}