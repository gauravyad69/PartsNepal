package np.com.parts.plugins.Auth

import io.ktor.server.auth.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable


@Serializable
data class UserSession(val userId: String, val username: String) : Principal

@Serializable
data class LoginRequest(val email: String, val password: String)
@Serializable
data class oldToken(val oldToken: String)

