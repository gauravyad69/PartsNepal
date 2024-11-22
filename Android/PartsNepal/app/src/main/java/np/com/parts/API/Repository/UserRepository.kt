package np.com.parts.API.Repository

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import np.com.parts.API.BASE_URL
import np.com.parts.API.Models.FullUserDetails
import np.com.parts.API.Models.OrderRef
import np.com.parts.API.Models.ReviewRef
import np.com.parts.API.Models.UpdateProfileRequest
import np.com.parts.API.Models.UserModel
import np.com.parts.API.Models.UserPreferences
import timber.log.Timber

class UserRepository(private val client: HttpClient) {

    suspend fun updateProfile(request: UpdateProfileRequest): Result<Boolean> {
        return try {
            val response = client.put("$BASE_URL/users/profile") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            Result.success(response.status.isSuccess())
        } catch (e: Exception) {
            Timber.e(e, "Profile update error")
            Result.failure(e)
        }
    }

    suspend fun updatePreferences(preferences: UserPreferences): Result<Boolean> {
        return try {
            val response = client.put("$BASE_URL/users/preferences") {
                contentType(ContentType.Application.Json)
                setBody(preferences)
            }

            Result.success(response.status.isSuccess())
        } catch (e: Exception) {
            Timber.e(e, "Preferences update error")
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(): Result<UserModel> {
        return try {
            val response = client.get("$BASE_URL/users/profile")
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to load profile"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error loading profile")
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