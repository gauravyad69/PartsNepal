package np.com.parts.system.Routes.Products

import np.com.parts.system.Services.ProductService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import np.com.parts.system.Models.BasicProductInfo
import np.com.parts.system.Models.PricingInfo
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
                    ErrorResponse("Error creating product", "PRODUCT_CREATION_ERROR", e.toString())
                )
            }
        }

        // PATCH - Update basic product info
        patch("/{productId}/basic") {
            try {
                val productId = call.parameters["productId"]?.toIntOrNull()
                    ?: return@patch call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Invalid product ID", "INVALID_PRODUCT_ID")
                    )

//                val updates = call.receive<Map<String, Any>>()
                val updates = call.receive<BasicProductInfo>()
                val updated = productService.updateBasicInfo(productId, updates)

                if (updated) {
                    call.respond(
                        HttpStatusCode.OK,
                        ProductResponse(data = true, message = "Basic product info updated successfully")
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
                    ErrorResponse("Error updating product", "UPDATE_ERROR", debug="$e")
                )
            }
        }

        // PATCH - Update detailed product info
        patch("/{productId}/details") {
            try {
                val productId = call.parameters["productId"]?.toIntOrNull()
                    ?: return@patch call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Invalid product ID", "INVALID_PRODUCT_ID")
                    )

                val updates = call.receive<Map<String, Any>>()
                val updated = productService.updateDetailedInfo(productId, updates)

                if (updated) {
                    call.respond(
                        HttpStatusCode.OK,
                        ProductResponse(data = true, message = "Detailed product info updated successfully")
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

        // PATCH - Update product inventory
        patch("/{productId}/inventory") {
            try {
                val productId = call.parameters["productId"]?.toIntOrNull()
                    ?: return@patch call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Invalid product ID", "INVALID_PRODUCT_ID")
                    )

                val stock = call.receive<Map<String, Int>>()["stock"]
                    ?: return@patch call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Invalid stock value", "INVALID_STOCK")
                    )

                val updated = productService.updateStock(productId, stock)

                if (updated) {
                    call.respond(
                        HttpStatusCode.OK,
                        ProductResponse(data = true, message = "Product inventory updated successfully")
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
                    ErrorResponse("Error updating inventory", "INVENTORY_UPDATE_ERROR")
                )
            }
        }

        // PATCH - Update product pricing
        patch("/{productId}/pricing") {
            try {
                val productId = call.parameters["productId"]?.toIntOrNull()
                    ?: return@patch call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Invalid product ID", "INVALID_PRODUCT_ID")
                    )

                val pricing = call.receive<PricingInfo>()
                val updated = productService.updatePricing(productId, pricing)

                if (updated) {
                    call.respond(
                        HttpStatusCode.OK,
                        ProductResponse(data = true, message = "Product pricing updated successfully")
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
                    ErrorResponse("Error updating pricing", "PRICING_UPDATE_ERROR")
                )
            }
        }
    }
}
