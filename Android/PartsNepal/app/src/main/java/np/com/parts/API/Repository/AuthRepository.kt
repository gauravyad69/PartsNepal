package np.com.parts.API.Repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import android.content.Context
import np.com.parts.API.Auth.AuthResponse
import np.com.parts.API.Auth.LoginRequest
import np.com.parts.API.Auth.RegisterRequest
import np.com.parts.API.BASE_URL
import np.com.parts.API.Models.AccountType
import np.com.parts.API.TokenManager

class AuthRepository(
    private val client: HttpClient,
    private val context: Context
) {
    private val tokenManager = TokenManager.getInstance(context)
    
    suspend fun login(identifier: String, password: String, isPhoneLogin: Boolean): Result<AuthResponse> {
        return try {
            val response = client.post("$BASE_URL/auth/login") {
                setBody(
                    LoginRequest(
                    identifier = identifier,
                    password = password,
                    isPhoneLogin = isPhoneLogin
                )
                )
            }
            val authResponse = response.body<AuthResponse>()
            // Save the token
            tokenManager.saveToken(authResponse.token)
            Result.success(authResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(
        email: String?,
        phoneNumber: String?,
        password: String?,
        firstName: String,
        lastName: String,
        username: String,
        accountType: AccountType
    ): Result<AuthResponse> {
        return try {
            val response = client.post("$BASE_URL/auth/register") {
                setBody(
                    RegisterRequest(
                    email = email,
                    phoneNumber = phoneNumber,
                    password = password,
                    firstName = firstName,
                    lastName = lastName,
                    username = username,
                    accountType = accountType
                )
                )
            }
            val authResponse = response.body<AuthResponse>()
            // Save the token
            tokenManager.saveToken(authResponse.token)
            Result.success(authResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        tokenManager.clearToken()
    }
} 