package np.com.parts.API.Repository

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import np.com.parts.API.BASE_URL
import np.com.parts.API.Models.Cart
import np.com.parts.API.Models.CreateOrderRequest
import np.com.parts.API.Models.OrderModel
import np.com.parts.API.Models.OrderSummary
import np.com.parts.API.NetworkModule
import timber.log.Timber

class OrderRepository(    private val client: HttpClient = NetworkModule.provideHttpClient()
) {

    // Get all orders for the current user
    suspend fun getUserOrders(skip: Int = 0, limit: Int = 50): Result<List<OrderModel>> {
        return try {
            val response = client.get("$BASE_URL/orders") {
                parameter("skip", skip)
                parameter("limit", limit)
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Timber.e(e, "Failed to get user orders")
            Result.failure(e)
        }
    }

    // Create a new order
    suspend fun createOrder(orderRequest: CreateOrderRequest): Result<OrderModel> {
        return try {
            val response = client.post("$BASE_URL/orders") {
                contentType(ContentType.Application.Json)
                setBody(orderRequest)
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Timber.e(e, "Failed to create order")
            Result.failure(e)
        }
    }

    // Get specific order details
    suspend fun getOrderDetails(orderId: String): Result<OrderModel> {
        return try {
            val response = client.get("$BASE_URL/orders/$orderId")
            Result.success(response.body())
        } catch (e: Exception) {
            Timber.e(e, "Failed to get order details")
            Result.failure(e)
        }
    }

    suspend fun getCartSummary(): Result<Cart> {
        return try {
            val response = client.get("$BASE_URL/cart") {
                contentType(ContentType.Application.Json)
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Timber.e(e, "Failed to get cart summary")
            Result.failure(e)
        }
    }

}