package np.com.parts.system.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.Android
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import np.com.parts.system.models.ProductModel
import np.com.parts.system.models.BasicProductView
import kotlinx.serialization.Serializable
import np.com.parts.API.NetworkModule


class ProductApiClient(
    private val client: HttpClient = NetworkModule.provideHttpClient()

) {
    companion object {
        private const val BASE_URL = "http://192.168.0.7:9090"
        private const val PRODUCTS_PATH = "$BASE_URL/products"
    }

    // Response wrapper classes matching the API
    @Serializable
    data class ProductResponse<T>(
        val data: T,
        val message: String? = null,
        val metadata: ResponseMetadata? = null
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
    suspend fun getBasicProducts(
        page: Int = 0,
        pageSize: Int = 20
    ): Result<ProductResponse<List<BasicProductView>>> = try {
        val response: ProductResponse<List<BasicProductView>> =
            client.get("$PRODUCTS_PATH/basic") {
                parameter("page", page)
                parameter("pageSize", pageSize)
            }.body()
        Result.success(response)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Get product by ID
    suspend fun getProductById(
        productId: Int
    ): Result<ProductResponse<ProductModel>> = try {
        val response: ProductResponse<ProductModel> =
            client.get("$PRODUCTS_PATH/$productId").body()
        Result.success(response)
    } catch (e: Exception) {
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