package np.com.parts.API.Auth

import kotlinx.serialization.Serializable
import np.com.parts.API.Models.AccountType

@Serializable
data class RegisterRequest(
    val email: String?,
    val phoneNumber: String?,
    val password: String?,
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
