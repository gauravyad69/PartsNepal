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
import np.com.parts.system.Models.UserId

private val log = org.slf4j.LoggerFactory.getLogger("Products")

// Authenticated Routes
fun Route.authenticatedProductRoutes(productService: ProductService) {
    route("/products") {
        // POST - Add product review

        // POST - Add product review
        post("/{productId}/review") {
            try {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("userId").asInt() ?: return@post call.respond(HttpStatusCode.Unauthorized)


                val productId = call.parameters["productId"]?.toIntOrNull()
                if (productId == null) {
                    log.warn("Invalid or missing product ID in request")
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Invalid product ID", "INVALID_PRODUCT_ID")
                    )
                }

                val reviewRequest = try {
                    call.receive<ReviewRequest>()
                } catch (e: Exception) {
                    log.error("Error parsing review request: ${e.message}", e)
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Invalid review request payload", "INVALID_PAYLOAD")
                    )
                }

                // Validate rating
                if (reviewRequest.rating !in 1..5) {
                    log.warn("Invalid rating: ${reviewRequest.rating}")
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Rating must be between 1 and 5", "INVALID_RATING")
                    )
                }

                val review = Review(
                    userId = UserId(userId),
                    rating = reviewRequest.rating,
                    comment = reviewRequest.comment
                )

                val added = productService.addReview(productId, review)

                if (added) {
                    log.info("Successfully added review for product ID: $productId by user ID: $userId")
                    call.respond(HttpStatusCode.OK, true)
                } else {
                    log.warn("Product not found for ID: $productId")
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("Product not found", "PRODUCT_NOT_FOUND")
                    )
                }
            } catch (e: Exception) {
                log.error("Unhandled exception while adding review: ${e.message}", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Error adding review", "REVIEW_ERROR", e.localizedMessage)
                )
            }
        }
    }
}