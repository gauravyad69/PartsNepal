// Response Models
package np.com.parts.system.Routes.Products

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import kotlinx.serialization.Serializable
import np.com.parts.system.Services.ProductService
import np.com.parts.system.Models.*

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
data class ReviewRequest(
    val rating: Int,
    val comment: String
)

@Serializable
data class ErrorResponse(
    val message: String,
    val code: String? = null
)

// Unauthenticated Routes
fun Route.unauthenticatedProductRoutes(productService: ProductService) {
    route("/products") {
        // GET - Retrieve all products with pagination
        get {
            try {
                val page = call.parameters["page"]?.toIntOrNull() ?: 1
                val pageSize = call.parameters["pageSize"]?.toIntOrNull() ?: 20
                val sortBy = call.parameters["sortBy"] ?: "productId"
                val sortOrder = call.parameters["sortOrder"]?.lowercase() ?: "asc"

                productService.getAllProductsFlow(pageSize)
                    .collect { products ->
                        call.respond(
                            HttpStatusCode.OK,
                            ProductResponse(
                                data = products,
                                metadata = ResponseMetadata(
                                    page = page,
                                    itemsPerPage = pageSize
                                )
                            )
                        )
                    }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Error retrieving products", "PRODUCTS_RETRIEVAL_ERROR")
                )
            }
        }

        // Route to retrieve all main product details with pagination
        get("/main-product-details") {
            try {
                val page = call.parameters["page"]?.toIntOrNull() ?: 1
                val pageSize = call.parameters["pageSize"]?.toIntOrNull() ?: 20
                val skip = (page - 1) * pageSize

                // Fetch only main product details in batches
                val mainProducts = withContext(Dispatchers.IO) {
                    mainDetailsCollection.find()
                        .skip(skip)
                        .limit(pageSize)
                        .toList()
                }

                // Respond with the list of main product details
                call.respond(HttpStatusCode.OK, mainProducts)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Error retrieving main product details", "MAIN_PRODUCT_DETAILS_RETRIEVAL_ERROR")
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
                    filters["mainDetails.productSPPrice"] = it
                }
                call.parameters["productType"]?.let {
                    filters["mainDetails.productType"] = it
                }
                call.parameters["onSale"]?.toBoolean()?.let {
                    filters["mainDetails.isProductOnSale"] = it
                }

                productService.searchProducts(query, page, pageSize, filters)
                    .collect { products ->
                        call.respond(
                            HttpStatusCode.OK,
                            ProductResponse(
                                data = products,
                                metadata = ResponseMetadata(
                                    page = page,
                                    itemsPerPage = pageSize
                                )
                            )
                        )
                    }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Error searching products", "SEARCH_ERROR")
                )
            }
        }

        // Additional routes with improved response handling...
    }
}