package np.com.parts.system.Routes.Products

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.Serializable
import np.com.parts.system.Services.ProductService
import np.com.parts.system.Models.*

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

@Serializable
data class ReviewRequest(
    val rating: Int,
    val comment: String
)


// Unauthenticated Routes
fun Route.unauthenticatedProductRoutes(productService: ProductService) {
    route("/products") {
        // GET - Retrieve all products with pagination
        get {
            try {
                val page = call.parameters["page"]?.toIntOrNull() ?: 0
                val pageSize = call.parameters["pageSize"]?.toIntOrNull() ?: 20
                val productsFlow = productService.getAllProductsFlow(pageSize)
                val products = productsFlow.toList()

                call.respond(
                    HttpStatusCode.OK,
                    ProductResponse(
                        data = products,
                        metadata = ResponseMetadata(
                            page = page,
                            itemsPerPage = pageSize,
                            totalItems = products.size
                        )
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Error retrieving products", "PRODUCTS_RETRIEVAL_ERROR", e.toString())
                )
            }
        }

        // GET - Retrieve basic product details with pagination
        get("/basic") {
            try {
                val page = call.parameters["page"]?.toIntOrNull() ?: 0
                val pageSize = call.parameters["pageSize"]?.toIntOrNull() ?: 10
                val products = productService.getAllBasicProductsFlow(pageSize).toList()

                val response = ProductResponse(
                    data = products,
                    metadata = ResponseMetadata(
                        page = page,
                        itemsPerPage = pageSize,
                        totalItems = products.size
                    )
                )

                call.respond(HttpStatusCode.OK, response)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Error retrieving basic product details", "BASIC_PRODUCTS_RETRIEVAL_ERROR", e.toString()
                    )
                )
            }
        }

        // GET - Retrieve a specific product by ID
        get("/{productId}") {
            try {
                val productId = call.parameters["productId"]?.toIntOrNull()
                    ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Invalid product ID", "INVALID_PRODUCT_ID")
                    )

                val product = productService.getProductById(productId)
                if (product != null) {
                    call.respond(HttpStatusCode.OK, ProductResponse(data = product))
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("Product not found", "PRODUCT_NOT_FOUND")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Error retrieving product", "PRODUCT_RETRIEVAL_ERROR")
                )
            }
        }

        // GET - Search products with advanced filtering
        get("/search") {
            try {
                val query = call.parameters["q"] ?: ""
                val page = call.parameters["page"]?.toIntOrNull() ?: 0
                val pageSize = call.parameters["pageSize"]?.toIntOrNull() ?: 20
                val filters = mutableMapOf<String, Any>()

                // Add optional filters
                call.parameters["minPrice"]?.toLongOrNull()?.let {
                    filters["basic.pricing.regularPrice.amount"] = it
                }
                call.parameters["productType"]?.let {
                    filters["basic.productType"] = it
                }
                call.parameters["onSale"]?.toBoolean()?.let {
                    filters["basic.pricing.isOnSale"] = it
                }

                val products = productService.searchProducts(query, page, pageSize, filters).toList()
                call.respond(
                    HttpStatusCode.OK,
                    ProductResponse(
                        data = products,
                        metadata = ResponseMetadata(
                            page = page,
                            itemsPerPage = pageSize,
                            totalItems = products.size
                        )
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Error searching products", "SEARCH_ERROR")
                )
            }
        }

        // GET - Retrieve products on sale
        get("/on-sale") {
            try {
                val page = call.parameters["page"]?.toIntOrNull() ?: 0
                val pageSize = call.parameters["pageSize"]?.toIntOrNull() ?: 20
                val products = productService.getProductsOnSale(page, pageSize).toList()

                call.respond(
                    HttpStatusCode.OK,
                    ProductResponse(
                        data = products,
                        metadata = ResponseMetadata(
                            page = page,
                            itemsPerPage = pageSize,
                            totalItems = products.size
                        )
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Error retrieving products on sale", "SALE_PRODUCTS_RETRIEVAL_ERROR")
                )
            }
        }
    }
}

