package np.com.parts.API.Repository

import android.annotation.SuppressLint
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import android.content.Context
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.errors.IOException
import kotlinx.serialization.Serializable
import np.com.parts.API.Auth.AuthError
import np.com.parts.API.Auth.AuthResponse
import np.com.parts.API.Auth.ErrorResponse
import np.com.parts.API.Auth.LoginRequest
import np.com.parts.API.Auth.RegisterRequest
import np.com.parts.API.BASE_URL
import np.com.parts.API.Models.AccountStatus
import np.com.parts.API.Models.AccountType
import np.com.parts.API.Models.Email
import np.com.parts.API.TokenManager
import timber.log.Timber
import java.util.Timer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val client: HttpClient,
    private val tokenManager: TokenManager
) {


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

    suspend fun getEmail(): Result<Creds>{
        try {

            val response = client.get("$BASE_URL/users/email")

            when (response.status.value) {
                HttpStatusCode.OK.value -> {

                }
                HttpStatusCode.Unauthorized.value -> {
                    val error = response.body<ErrorResponse>()
                    AuthResult.Error(
                        AuthError.INVALID_CREDENTIALS,
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
            return Result.success(response.body<Creds>())
        } catch (e: Exception) {
            Timber.e(e, "Failed to get email")
            return Result.failure(e)
        }
    }

    suspend fun updateAccountStatus(): Result<kotlin.Boolean> {
        try {

            val response = client.patch("$BASE_URL/users/status"){
                parameter("status", AccountStatus.ACTIVE)
            }

            when (response.status.value) {
                HttpStatusCode.OK.value -> {
                    Result.success(true)
                }
                HttpStatusCode.Unauthorized.value -> {
                    Result.success(false)
                }
                else -> {
                    val error = response.body<ErrorResponse>()
                    AuthResult.Error(
                        AuthError.UNKNOWN_ERROR,
                        error.message
                    )
                }
            }
            return if (response.status== HttpStatusCode.OK) Result.success(true) else Result.success(false)
        } catch (e: Exception) {
            Timber.e(e, "Failed to update account status")
            return Result.failure(e)
        }
    }


    fun logout() {
        tokenManager.clearToken()
    }

    companion object {
        private const val TAG = "AuthRepository"
    }
}

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class Creds(
    val email: Email,
    val cred: String,
)