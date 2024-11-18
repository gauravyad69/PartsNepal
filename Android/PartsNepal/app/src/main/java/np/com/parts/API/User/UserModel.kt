package np.com.parts.API.User

import kotlinx.serialization.Serializable
import np.com.parts.system.models.BasicProductView

@Serializable
data class UserModel(
    val userId: Int,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val isBusinessAccount: Boolean,
)

data class FullUserDetails(
    val user: UserModel,
    val totalReviews: Int,
    val reviewHistory: List<String>,
    val toReceive: List<OrderModel>
)


data class OrderModel(
    val mainDetails: List<BasicProductView>,
    val quantity: Int,
    val contactNumber: String,
    val voucherUsed: String,
    val pricePaid: Long,
    val pricePaidIncludingDeliveryCost: Long,
    val address: ShippingAddressModel
)


data class ShippingAddressModel(
    val province: String,
    val region: String,
    val district: String,
    val city: String,
    val address: String,
    val landmark: String?,
    val recipientsName: String,
    val recipientsPhoneNumber: String,
)


// Response Models
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
    val code: String? = null,
    val debug: String? = null

)