@file:SuppressLint("UnsafeOptInUsageError")
package np.com.parts.API.Models

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable


@Serializable
data class LoginRequest(
    val identifier: String,
    val password: String,
    val isPhoneLogin: Boolean
)

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
data class AuthResponse(
    val token: String,
    val user: Int,
    val expiresIn: Long = 3600
)


enum class AuthError {
    INVALID_CREDENTIALS,
    ACCOUNT_INACTIVE,
    DUPLICATE_USER,
    NETWORK_ERROR,
    INVALID_REQUEST,
    UNKNOWN_ERROR,
    USER_NOT_FOUND
}