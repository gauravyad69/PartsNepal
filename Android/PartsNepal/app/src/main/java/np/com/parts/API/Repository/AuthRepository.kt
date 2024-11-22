package np.com.parts.API.Repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import android.content.Context
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.errors.IOException
import np.com.parts.API.Auth.AuthError
import np.com.parts.API.Auth.AuthResponse
import np.com.parts.API.Auth.ErrorResponse
import np.com.parts.API.Auth.LoginRequest
import np.com.parts.API.Auth.RegisterRequest
import np.com.parts.API.BASE_URL
import np.com.parts.API.Models.AccountType
import np.com.parts.API.TokenManager
import timber.log.Timber
import java.util.Timer

class AuthRepository(
    private val client: HttpClient,
    private val context: Context
) {
    private val tokenManager = TokenManager.getInstance(context)

    sealed class AuthResult<out T> {
        data class Success<T>(val data: T) : AuthResult<T>()
        data class Error(val error: AuthError, val message: String) : AuthResult<Nothing>()
    }

    suspend fun login(
        identifier: String,
        password: String,
        isPhoneLogin: Boolean
    ): AuthResult<AuthResponse> {
        return try {
            val response = client.post("$BASE_URL/auth/login") {
                setBody(LoginRequest(
                    identifier = identifier,
                    password = password,
                    isPhoneLogin = isPhoneLogin
                ))
            }

            when (response.status.value) {
                HttpStatusCode.OK.value -> {
                    val authResponse = response.body<AuthResponse>()
                    tokenManager.saveToken(authResponse.token)
                    AuthResult.Success(authResponse)
                }
                HttpStatusCode.Unauthorized.value -> {
                    val error = response.body<ErrorResponse>()
                    AuthResult.Error(
                        AuthError.INVALID_CREDENTIALS,
                        error.message
                    )
                }
                HttpStatusCode.Forbidden.value -> {
                    val error = response.body<ErrorResponse>()
                    AuthResult.Error(
                        AuthError.ACCOUNT_INACTIVE,
                        error.message
                    )
                }
                else -> {
                    val error = response.body<ErrorResponse>()
                    AuthResult.Error(
                        AuthError.UNKNOWN_ERROR,
                        error.message
                    )
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Login failed")
            when (e) {
                is IOException -> AuthResult.Error(
                    AuthError.NETWORK_ERROR,
                    "Network error occurred. Please check your connection."
                )
                else -> AuthResult.Error(
                    AuthError.UNKNOWN_ERROR,
                    e.message ?: "An unexpected error occurred"
                )
            }
        }
    }

    suspend fun register(
        email: String? = null,
        phoneNumber: String,
        password: String,
        firstName: String,
        lastName: String,
        username: String,
        accountType: AccountType
    ): AuthResult<AuthResponse> {
        return try {
            val response = client.post("$BASE_URL/auth/register") {
                setBody(RegisterRequest(
                    email = email,
                    phoneNumber = phoneNumber,
                    password = password,
                    firstName = firstName,
                    lastName = lastName,
                    username = username,
                    accountType = accountType
                ))
            }

            when (response.status.value) {
                HttpStatusCode.Created.value -> {
                    val authResponse = response.body<AuthResponse>()
                    tokenManager.saveToken(authResponse.token)
                    AuthResult.Success(authResponse)
                }
                HttpStatusCode.BadRequest.value -> {
                    val error = response.body<ErrorResponse>()
                    when (error.code) {
                        "DUPLICATE_USER" -> AuthResult.Error(
                            AuthError.DUPLICATE_USER,
                            error.message
                        )
                        "INVALID_REQUEST" -> AuthResult.Error(
                            AuthError.INVALID_REQUEST,
                            error.message
                        )
                        else -> AuthResult.Error(
                            AuthError.UNKNOWN_ERROR,
                            error.message
                        )
                    }
                }
                else -> {
                    val error = response.body<ErrorResponse>()
                    AuthResult.Error(
                        AuthError.UNKNOWN_ERROR,
                        error.message
                    )
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Registration failed $e")
            when (e) {
                is IOException -> AuthResult.Error(
                    AuthError.NETWORK_ERROR,
                    "Network error occurred. Please check your connection."
                )
                else -> AuthResult.Error(
                    AuthError.UNKNOWN_ERROR,
                    e.message ?: "An unexpected error occurred"
                )
            }
        }
    }

    fun logout() {
        tokenManager.clearToken()
    }

    companion object {
        private const val TAG = "AuthRepository"
    }
}