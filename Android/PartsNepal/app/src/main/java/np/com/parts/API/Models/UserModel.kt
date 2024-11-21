package np.com.parts.API.Models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val userId: Int,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String?,
    val accountType: AccountType = AccountType.PERSONAL,
    val accountStatus: AccountStatus = AccountStatus.ACTIVE,
    val preferences: UserPreferences = UserPreferences(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val fullName: String
        get() = "$firstName $lastName"
}

@Serializable
data class UserPreferences(
    val language: String = "en",
    val timezone: String = "UTC",
    val notificationSettings: NotificationSettings = NotificationSettings()
)

@Serializable
data class NotificationSettings(
    val emailNotifications: Boolean = true,
    val pushNotifications: Boolean = true,
    val smsNotifications: Boolean = false
)

@Serializable
enum class AccountType {
    PERSONAL,
    BUSINESS,
    ENTERPRISE
}

@Serializable
enum class AccountStatus {
    ACTIVE,
    SUSPENDED,
    DEACTIVATED,
    PENDING_VERIFICATION
}


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

@Serializable
data class OrderRef(
    val orderId: String,
    val orderDate: Long = System.currentTimeMillis(),
    val status: OrderStatus = OrderStatus.PENDING
)

@Serializable
enum class OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}

@Serializable
data class ReviewRef(
    val productId: String,
    val rating: Int,
    val comment: String,
    val reviewDate: Long = System.currentTimeMillis()
)