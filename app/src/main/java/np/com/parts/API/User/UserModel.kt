package np.com.parts.API.User

import np.com.parts.API.Product.MainProductDetailsModel

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
    val totalReviews: Int,
    val reviewHistory: List<String>,
    val toReceive: List<String>
)


data class OrderModel(
    val mainDetails: List<MainProductDetailsModel>,
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