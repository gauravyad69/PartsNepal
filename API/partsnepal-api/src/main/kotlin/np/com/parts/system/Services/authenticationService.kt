package np.com.parts.system.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import np.com.parts.system.Models.*
import np.com.parts.system.Services.UserService
import java.util.*

// Request/Response Models
@Serializable
data class RegisterRequest(
    val email: Email,
    val phoneNumber: PhoneNumber?,
    val password: String,
    val firstName: String,
    val lastName: String,
    val username: String,
    val accountType: AccountType = AccountType.PERSONAL
)

@Serializable
data class LoginRequest(
    val identifier: String, // email or phone
    val password: String,
    val isPhoneLogin: Boolean // true for phone login, false for email login
)

@Serializable
data class AuthResponse(
    val token: String,
    val user: Int,
    val expiresIn: Long = 3600 // Token expiration in seconds
)

@Serializable
data class ErrorResponse(
    val message: String,
    val code: String
)

class AuthenticationService(
    private val userService: UserService,
    private val jwtConfig: JWTConfig
) {
    // Add this data class for JWT configuration
    data class JWTConfig(
        val secret: String,
        val issuer: String,
        val audience: String,
        val realm: String,
        val expirationInMillis: Long = 3600000 // 1 hour by default
    )

    private fun generateToken(user: UserModel): String {
        return JWT.create()
            .withAudience(jwtConfig.audience)
            .withIssuer(jwtConfig.issuer)
            .withClaim("userId", user.userId.value)
            .withClaim("email", user.email.value)
            .withClaim("username", user.username)
            .withClaim("accountType", user.accountType.name)
            .withExpiresAt(Date(System.currentTimeMillis() + jwtConfig.expirationInMillis))
            .sign(Algorithm.HMAC256(jwtConfig.secret))
    }

    suspend fun register(request: RegisterRequest): Result<AuthResponse> {
        try {
            // Check for existing email
            userService.getUserByEmail(request.email)?.let {
                return Result.failure(Exception("Email already registered"))
            }

            // Check for existing phone if provided
            request.phoneNumber?.let { phone ->
                userService.getUserByPhone(phone)?.let {
                    return Result.failure(Exception("Phone number already registered"))
                }
            }

            val userId = UserId(UUID.randomUUID().hashCode())
            val user = UserModel(
                userId = userId,
                username = request.username,
                email = request.email,
                phoneNumber = request.phoneNumber,
                firstName = request.firstName,
                lastName = request.lastName,
                accountType = request.accountType
            )

            val credentials = UserCredentials(
                hashedPassword = userService.hashPassword(request.password),
                lastPasswordChange = System.currentTimeMillis()
            )

            val fullUserDetails = FullUserDetails(
                user = user,
                credentials = credentials,
                engagement = UserEngagement(
                    totalTimeSpentMs = 0,
                    lastActive = System.currentTimeMillis(),
                    engagementScore = null
                ),
                accountStatus = AccountStatus.PENDING_VERIFICATION
            )

            return if (userService.createUser(fullUserDetails)) {
                Result.success(AuthResponse(
                    token = generateToken(user),
                    user = user.userId.value,
                    expiresIn = jwtConfig.expirationInMillis / 1000 // Convert to seconds
                ))
            } else {
                Result.failure(Exception("Failed to create user"))
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    suspend fun login(request: LoginRequest): Result<AuthResponse> {
        try {
            val user = if (request.isPhoneLogin) {
                userService.loginWithPhoneAndPassword(
                    PhoneNumber(request.identifier),
                    request.password
                )
            } else {
                val email = Email(request.identifier)
                userService.getUserByEmail(email)?.let { user ->
                    if (user.credentials.hashedPassword == userService.hashPassword(request.password)) {
                        userService.updateLoginActivity(user.user.userId)
                        user
                    } else null
                }
            }

            return user?.let {
                if (user.accountStatus == AccountStatus.ACTIVE) {
                    Result.success(AuthResponse(
                        token = generateToken(user.user),
                        user = user.user.userId.value,
                        expiresIn = jwtConfig.expirationInMillis / 1000 // Convert to seconds
                    ))
                } else {
                    Result.failure(Exception("Account is ${user.accountStatus}"))
                }
            } ?: Result.failure(Exception("Invalid credentials"))

        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
}
