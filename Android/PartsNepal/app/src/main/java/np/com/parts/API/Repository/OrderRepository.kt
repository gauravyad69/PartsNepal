package np.com.parts.API.Repository

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import np.com.parts.API.BASE_URL
import np.com.parts.API.Models.CreateOrderRequest
import np.com.parts.API.Models.Order

class OrderRepository(private val client: HttpClient) {

    // Get all orders for the current user
    suspend fun getUserOrders(skip: Int = 0, limit: Int = 50): Result<List<Order>> {
        return try {
            val response = client.get("$BASE_URL/orders") {
                parameter("skip", skip)
                parameter("limit", limit)
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Create a new order
    suspend fun createOrder(orderRequest: CreateOrderRequest): Result<Boolean> {
        return try {
            val response = client.post("$BASE_URL/orders") {
                setBody(orderRequest)
            }
            Result.success(response.status.isSuccess())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get specific order details
    suspend fun getOrderDetails(orderNumber: String): Result<Order> {
        return try {
            val response = client.get("$BASE_URL/orders/$orderNumber")
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 