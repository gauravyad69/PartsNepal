package np.com.parts.system.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import kotlinx.serialization.Serializable
import np.com.parts.system.Models.*
import np.com.parts.system.Services.UserService
import java.util.*

// Request/Response Models
@Serializable
data class RegisterRequest(
    val email: String? = null,
    val phoneNumber: String,
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
            .withClaim("phoneNumber", user.phoneNumber.value)
            .withClaim("username", user.username)
            .withClaim("accountType", user.accountType.name)
            .withExpiresAt(Date(System.currentTimeMillis() + jwtConfig.expirationInMillis))
            .sign(Algorithm.HMAC256(jwtConfig.secret))
    }

    suspend fun register(request: RegisterRequest): Result<AuthResponse> {
        try {
            println("Starting registration for username: ${request.username}")
            
            // Validate phone number format
            val phoneNumber = try {
                PhoneNumber(request.phoneNumber)
            } catch (e: Exception) {
                println("Phone number validation failed: ${e.message}")
                return Result.failure(Exception("Invalid phone number format"))
            }

            // Validate email if provided
            val email = request.email?.let {
                try {
                    Email(it)
                } catch (e: Exception) {
                    println("Email validation failed: ${e.message}")
                    return Result.failure(Exception("Invalid email format"))
                }
            }

            val user = UserModel(
                userId = UserId(0),
                username = request.username,
                email = email,
                phoneNumber = phoneNumber,
                firstName = request.firstName,
                lastName = request.lastName,
                accountType = request.accountType
            )

            val fullUserDetails = FullUserDetails(
                user = user,
                credentials = UserCredentials(
                    hashedPassword = userService.hashPassword(request.password),
                    lastPasswordChange = System.currentTimeMillis()
                ),
                engagement = UserEngagement(
                    totalTimeSpentMs = 0,
                    lastActive = System.currentTimeMillis(),
                    engagementScore = null
                ),
                accountStatus = AccountStatus.PENDING_VERIFICATION
            )

            return if (userService.createUser(fullUserDetails)) {
                println("User created successfully")
                val createdUser = userService.getUserByPhone(phoneNumber)
                    ?: return Result.failure(Exception("Failed to retrieve created user"))
                
                Result.success(AuthResponse(
                    token = generateToken(createdUser.user),
                    user = createdUser.user.userId.value,
                    expiresIn = jwtConfig.expirationInMillis / 1000
                ))
            } else {
                println("User creation failed - user might already exist")
                Result.failure(Exception("User already exists"))
            }
        } catch (e: Exception) {
            println("Registration failed with error: ${e.message}")
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
