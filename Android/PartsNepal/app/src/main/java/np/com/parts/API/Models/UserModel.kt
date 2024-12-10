package np.com.parts.API.Models

import kotlinx.serialization.Serializable

@Serializable
data class FullUserDetails(
    val user: UserModel,
    val credentials: UserCredentials,
    val preferences: UserPreferences = UserPreferences(),
    val engagement: UserEngagement,
    val accountStatus: AccountStatus = AccountStatus.ACTIVE,
    val reviews: UserReviews = UserReviews(),
    val orders: UserOrders = UserOrders(),
    val lastModifiedBy: UserId? = null,
    val lastModifiedAt: Long = System.currentTimeMillis()
)

@Serializable
@JvmInline
value class UserId(val value: Int)

@Serializable
@JvmInline
value class Email(val value: String?)

@Serializable
@JvmInline
value class PhoneNumber(val value: String)

@Serializable
data class UserModel(
    val userId: UserId,
    val username: String,
    val email: Email? = null,
    val firstName: String,
    val lastName: String,
    val phoneNumber: PhoneNumber,
    val accountType: AccountType = AccountType.PERSONAL,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val fullName: String
        get() = "$firstName $lastName"
}

@Serializable
data class UserCredentials(
    val hashedPassword: String?,
    val lastPasswordChange: Long,
    val mfaEnabled: Boolean = false,
    val mfaSecret: String? = null
)

@Serializable
data class UserPreferences(
    val language: String = "en",
    val timezone: String = "UTC",
    val marketingConsent: Boolean = false,
    val notificationSettings: NotificationSettings = NotificationSettings(),
    val customPreferences: Map<String, String> = emptyMap()
)

@Serializable
data class NotificationSettings(
    val emailNotifications: Boolean = true,
    val pushNotifications: Boolean = true,
    val smsNotifications: Boolean = false
)

@Serializable
data class UserEngagement(
    val totalTimeSpentMs: Long,
    val lastActive: Long,
    val engagementScore: Int?,
    val loginHistory: List<Long> = emptyList()
)

@Serializable
data class UserReviews(
    val totalReviews: Int = 0,
    val averageRating: Double? = null,
    val reviewHistory: List<ReviewRef> = emptyList()
)

@Serializable
data class UserOrders(
    val totalOrders: Int = 0,
    val totalSpent: Double = 0.0,
    val orderHistory: List<OrderRef> = emptyList()
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

// Keep your existing response models
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
data class ReviewRef(
    val reviewId: String,
    val createdAt: Long
)

@Serializable
data class OrderRef(
    val orderId: String,
    val amount: Long,
    val createdAt: Long
)

@Serializable
data class UpdateProfileRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val username: String? = null,
    val accountType: AccountType? = null
)

@Serializable
data class ErrorResponse(
    val message: String,
    val code: String? = null,
    val debug: String? = null
)