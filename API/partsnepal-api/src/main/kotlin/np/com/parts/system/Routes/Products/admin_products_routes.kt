package np.com.parts.system.Routes.Products

import np.com.parts.system.Services.ProductService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import np.com.parts.system.Models.ProductModel


// Admin Routes
fun Route.adminProductRoutes(productService: ProductService) {
    route("/admin/products") {
        // POST - Create a new product
        post {
            try {
                val product = call.receive<ProductModel>()
                val created = productService.createProduct(product)

                if (created) {
                    call.respond(
                        HttpStatusCode.Created,
                        ProductResponse(data = product, message = "Product created successfully")
                    )
                } else {
                    call.respond(
                        HttpStatusCode.Conflict,
                        ErrorResponse("Product already exists", "PRODUCT_EXISTS")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Error creating product", "PRODUCT_CREATION_ERROR")
                )
            }
        }

        // PATCH - Update main product details
        patch("/{productId}/main") {
            try {
                val productId = call.parameters["productId"]?.toIntOrNull()
                    ?: return@patch call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Invalid product ID", "INVALID_PRODUCT_ID")
                    )

                val updates = call.receive<Map<String, Any>>()
                val updated = productService.updateMainDetails(productId, updates)

                if (updated) {
                    call.respond(
                        HttpStatusCode.OK,
                        ProductResponse(data = true, message = "Product main details updated successfully")
                    )
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("Product not found", "PRODUCT_NOT_FOUND")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Error updating product", "UPDATE_ERROR")
                )
            }
        }

        // PATCH - Update full product details
        patch("/{productId}/full") {
            try {
                val productId = call.parameters["productId"]?.toIntOrNull()
                    ?: return@patch call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Invalid product ID", "INVALID_PRODUCT_ID")
                    )

                val updates = call.receive<Map<String, Any>>()
                val updated = productService.updateFullDetails(productId, updates)

                if (updated) {
                    call.respond(
                        HttpStatusCode.OK,
                        ProductResponse(data = true, message = "Product full details updated successfully")
                    )
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("Product not found", "PRODUCT_NOT_FOUND")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Error updating product", "UPDATE_ERROR")
                )
            }
        }

        // Additional admin routes...
    }
}