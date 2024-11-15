package np.com.parts.system.Routes.Products

import np.com.parts.system.Services.ProductService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import np.com.parts.system.Models.Review

// Authenticated Routes
fun Route.authenticatedProductRoutes(productService: ProductService) {
    route("/products") {
        // POST - Add product review
        post("/{productId}/review") {
            try {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("userId").asString()
                val productId = call.parameters["productId"]?.toIntOrNull()
                    ?: return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Invalid product ID", "INVALID_PRODUCT_ID")
                    )

                val reviewRequest = call.receive<ReviewRequest>()

                // Validate rating
                if (reviewRequest.rating !in 1..5) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Rating must be between 1 and 5", "INVALID_RATING")
                    )
                }

                val review = Review(
                    userId = userId,
                    rating = reviewRequest.rating,
                    comment = reviewRequest.comment
                )

                val added = productService.addReview(productId, review)

                if (added) {
                    call.respond(
                        HttpStatusCode.OK,
                        ProductResponse(data = true, message = "Review added successfully")
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
                    ErrorResponse("Error adding review", "REVIEW_ERROR")
                )
            }
        }
    }
}