package np.com.parts.API.Repository

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import np.com.parts.API.BASE_URL
import np.com.parts.API.Models.CreateOrderRequest
import np.com.parts.API.Models.OrderModel
import np.com.parts.API.Models.OrderSummary
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepository @Inject constructor(private val client: HttpClient) {

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
    suspend fun createOrder(orderRequest: CreateOrderRequest): Result<OrderModel> = withContext(NonCancellable) {
        try {
            Timber.d("Starting order creation with request: $orderRequest")
            
            val response = client.post("$BASE_URL/orders") {
                contentType(ContentType.Application.Json)
                setBody(orderRequest)
            }
            
            when (response.status) {
                HttpStatusCode.Created, HttpStatusCode.OK -> {
                    val orderResponse: OrderModel = response.body()
                    Timber.d("Order created successfully: ${orderResponse.orderNumber}")
                    Result.success(orderResponse)
                }
                HttpStatusCode.BadRequest -> {
                    Timber.e("Bad request when creating order")
                    Result.failure(IllegalArgumentException("Invalid order request"))
                }
                HttpStatusCode.Unauthorized -> {
                    Timber.e("Unauthorized when creating order")
                    Result.failure(IllegalStateException("User not authenticated"))
                }
                else -> {
                    Timber.e("Unexpected response: ${response.status}")
                    Result.failure(Exception("Server returned ${response.status}"))
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to create order with exception")
            Result.failure(e)
        }
    }

    // Get specific order details
    suspend fun getOrderDetails(orderNumber: String): Result<OrderModel> {
        return try {
            val response = client.get("$BASE_URL/orders/$orderNumber")
            Result.success(response.body())
        } catch (e: Exception) {
            Timber.e(e, "Failed to get order details")
            Result.failure(e)
        }
    }

    suspend fun getCartSummary(): Result<OrderSummary> {
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