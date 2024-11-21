package np.com.parts.API.Repository

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import np.com.parts.API.BASE_URL
import np.com.parts.API.Models.OrderRef
import np.com.parts.API.Models.ReviewRef
import np.com.parts.API.Models.User
import np.com.parts.API.Models.UserPreferences

class UserRepository(private val client: HttpClient) {

    // Get current user's profile
    suspend fun getUserProfile(): Result<User> {
        return try {
            val response = client.get("$BASE_URL/users/profile")
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Update user profile
    suspend fun updateProfile(updates: Map<String, Any>): Result<Boolean> {
        return try {
            val response = client.put("$BASE_URL/users/profile") {
                setBody(updates)
            }
            Result.success(response.status.isSuccess())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Add order to user history
    suspend fun addOrder(order: OrderRef): Result<Boolean> {
        return try {
            val response = client.post("$BASE_URL/users/orders") {
                setBody(order)
            }
            Result.success(response.status.isSuccess())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Update user preferences
    suspend fun updatePreferences(preferences: UserPreferences): Result<Boolean> {
        return try {
            val response = client.put("$BASE_URL/users/preferences") {
                setBody(preferences)
            }
            Result.success(response.status.isSuccess())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Add review to user's history
    suspend fun addReview(review: ReviewRef): Result<Boolean> {
        return try {
            val response = client.post("$BASE_URL/users/reviews") {
                setBody(review)
            }
            Result.success(response.status.isSuccess())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 