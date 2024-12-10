package np.com.parts.API.Repository

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import np.com.parts.API.Models.ProductModel
import np.com.parts.API.Models.BasicProductView
import kotlinx.serialization.Serializable
import np.com.parts.API.PRODUCTS_PATH
import javax.inject.Inject



class MiscRepository @Inject constructor(
    private val client: HttpClient

) {

    // Get all products
    suspend fun getAllMiscs(
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
}