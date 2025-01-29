@file:SuppressLint("UnsafeOptInUsageError")

package np.com.parts.API.Repository

import android.annotation.SuppressLint
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import np.com.parts.API.Models.ProductModel
import np.com.parts.API.Models.BasicProductView
import kotlinx.serialization.Serializable
import np.com.parts.API.Models.Money
import np.com.parts.API.Models.ProductResponse
import np.com.parts.API.PRODUCTS_PATH
import np.com.parts.Presentation.Adapter.CarouselImage
import np.com.parts.Presentation.Adapter.Deal
import timber.log.Timber
import javax.inject.Inject

/*

@Serializable
data class ProductResponse<T>(
    val data: T,
    val message: String? = null,
    val metadata: ResponseMetadata? = null,
    val error: ErrorResponse? = null  // Add this to handle error responses
)

@Serializable
data class ResponseMetadata(
    val page: Int? = null,
    val totalPages: Int? = null,
    val totalItems: Int? = null,
    val itemsPerPage: Int? = null
)

@Serializable
data class ErrorResponse(
    val message: String,
    val code: String? = null,
    val debug: String? = null
)
*/

@Serializable
data class ReviewRequest(
    val rating: Int,
    val comment: String
)


class ProductRepository @Inject constructor(
    private val client: HttpClient
) {

    // Get all products
    suspend fun getAllProducts(
        page: Int = 0,
        pageSize: Int = 20
    ): Result<ProductResponse<List<ProductModel>>> = try {
        val response: ProductResponse<List<ProductModel>> = client.get(PRODUCTS_PATH) {
            parameter("page", page)
            parameter("pageSize", pageSize)
        }.body()
        Result.success(response)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Get basic product details
    // In ProductRepository.kt
    suspend fun getBasicProducts(
        page: Int = 0,
        pageSize: Int = 20
    ): Result<ProductResponse<List<BasicProductView>>> = try {
        println("Making request to: $PRODUCTS_PATH/basic with page=$page&pageSize=$pageSize")

        val response = client.get("$PRODUCTS_PATH/basic") {
            parameter("page", page)
            parameter("pageSize", pageSize)
        }

        // Log the raw response
        val body = response.body<ProductResponse<List<BasicProductView>>>()


        // Try parsing
        val productResponse = body
        Result.success(productResponse)
    } catch (e: Exception) {
        println("Error in getBasicProducts: ${e.message}")
        e.printStackTrace()
        Result.failure(e)
    }

    // Get product by ID
    // Get product by ID
    suspend fun getProductById(
        productId: Int
    ): Result<ProductResponse<ProductModel>> = try {
        println("Making request to: $PRODUCTS_PATH/$productId")  // Add logging

        val response: ProductResponse<ProductModel> = client.get("$PRODUCTS_PATH/$productId").body()


        Result.success(response)
    } catch (e: Exception) {
        println("Error in getProductById: ${e.message}")  // Add error logging
        e.printStackTrace()
        Result.failure(e)
    }
    // Search products
    suspend fun searchProducts(
        query: String = "",
        page: Int = 0,
        pageSize: Int = 20,
        minPrice: Long? = null,
        productType: String? = null,
        onSale: Boolean? = null
    ): Result<ProductResponse<List<ProductModel>>> = try {
        val response: ProductResponse<List<ProductModel>> =
            client.get("$PRODUCTS_PATH/search") {
                parameter("q", query)
                parameter("page", page)
                parameter("pageSize", pageSize)
                minPrice?.let { parameter("minPrice", it) }
                productType?.let { parameter("productType", it) }
                onSale?.let { parameter("onSale", it) }
            }.body()
        Result.success(response)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Get products on sale
    suspend fun getProductsOnSale(
        page: Int = 0,
        pageSize: Int = 20
    ): Result<ProductResponse<List<ProductModel>>> = try {
        val response: ProductResponse<List<ProductModel>> =
            client.get("$PRODUCTS_PATH/on-sale") {
                parameter("page", page)
                parameter("pageSize", pageSize)
            }.body()
        Result.success(response)
    } catch (e: Exception) {
        Result.failure(e)
    }


    // Get product by ID
    suspend fun sendReview(
        productId: Int,
        rating: Int,
        review: String
    ): Result<Boolean> {
        return try {
            println("Making request to /review")
            val requestBody = ReviewRequest(
                rating = rating,
                comment = review
            )
            val response = client.post("$PRODUCTS_PATH/$productId/review"){
                setBody(requestBody)
            }

            val responseBody = response.body<Boolean>()

            return if (responseBody==true){
                Result.success(responseBody)
            } else {
                Result.failure(Exception("The returned data is false, dictating failure, /review"))
            }
        } catch (e: Exception) {
            println("Error in /review: ${e.message}")  // Add error logging
            e.printStackTrace()
            Result.failure(e)
        }
    }


     fun getDeals(): Result<List<Deal>> = try {

        val response = listOf(
            Deal("1", "Gaming Mouse", Money(29990), Money(20), "https://example.com/mouse.jpg"),
            Deal("2", "Mechanical Keyboard", Money(59990), Money(15), "https://example.com/keyboard.jpg"),
            Deal("3", "Gaming Headset", Money(39990), Money(25), "https://example.com/headset.jpg")
        )
//        val response = api.getDeals()
        Result.success(response)
    } catch (e: Exception) {
        Result.failure(e)
    }

     fun getCarouselImages(): Result<List<CarouselImage>> = try {

        val response = listOf(
            CarouselImage("1", "https://example.com/image1.jpg"),
            CarouselImage("2", "https://example.com/image2.jpg"),
            CarouselImage("3", "https://example.com/image3.jpg")
        )


//        val response = api.getCarouselImages()
        Result.success(response)
    } catch (e: Exception) {
        Result.failure(e)
    }
}