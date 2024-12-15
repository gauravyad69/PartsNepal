package np.com.parts.system.Models
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.time.Instant
import kotlin.time.Duration


// Core value objects for better type safety
@JvmInline
@Serializable
value class UserId(val value: Int)

@JvmInline
@Serializable
value class Email(val value: String?) {
    init {
        value?.let { require(it.matches(Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))) { "Invalid email format" } }
    }
}

@JvmInline
@Serializable
value class PhoneNumber(val value: String) {
    init {
        require(value.matches(Regex("^\\+?[1-9]\\d{1,14}$"))) { "Invalid phone number format" }
    }
}

// Enums for type safety and better maintainability
enum class AccountType {
    PERSONAL,
    BUSINESS,
    ENTERPRISE
}

enum class AccountStatus {
    ACTIVE,
    SUSPENDED,
    DEACTIVATED,
    PENDING_VERIFICATION
}

@Serializable
data class UserCredentials(
    val hashedPassword: String?,
    val lastPasswordChange: Long, // Unix timestamp in milliseconds
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
    val totalTimeSpentMs: Long, // Duration in milliseconds
    val lastActive: Long, // Unix timestamp in milliseconds
    val engagementScore: Int?,
    val loginHistory: List<Long> = emptyList() // List of Unix timestamps
)

@Serializable
data class UserModel(
    val userId: UserId,
    val username: String,
    val email: Email,
    val firstName: String,
    val lastName: String,
    val phoneNumber: PhoneNumber,
    val accountType: AccountType = AccountType.PERSONAL,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    init {
        require(username.isNotBlank()) { "Username cannot be blank" }
        require(firstName.isNotBlank()) { "First name cannot be blank" }
        require(lastName.isNotBlank()) { "Last name cannot be blank" }
    }
    
    val fullName: String
        get() = "$firstName $lastName"
}

@Serializable
data class FullUserDetails(
    val user: UserModel,
    val credentials: UserCredentials,
    val preferences: UserPreferences = UserPreferences(),
    val engagement: UserEngagement,
    val accountStatus: AccountStatus = AccountStatus.ACTIVE,

    // Activity-related fields
    val reviews: UserReviews = UserReviews(),
    val orders: UserOrders = UserOrders(),

    // Audit fields
    val lastModifiedBy: UserId? = null,
    val lastModifiedAt: Long = System.currentTimeMillis() // Unix timestamp in milliseconds
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
    val totalSpent: Double = 0.0, // Changed from BigDecimal to Double
    val orderHistory: List<OrderRef> = emptyList()
)

@Serializable
data class ReviewRef(
    val reviewId: String,
    val createdAt: Long =System.currentTimeMillis()// Unix timestamp in milliseconds
)

@Serializable
data class OrderRef(
    val orderId: String,
    val amount: Double, // Changed from BigDecimal to Double
    val createdAt: Long = System.currentTimeMillis()// Unix timestamp in milliseconds
)


//indipendent of the user model

@Serializable
data class UpdateProfileRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val username: String? = null
) {
    fun isValid(): Boolean {
        // Validate email if provided
        email?.let {
            if (!it.matches(Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))) {
                return false
            }
        }
        
        // Validate phone if provided
        phoneNumber?.let {
            if (!it.matches(Regex("^\\+?[1-9]\\d{1,14}$"))) {
                return false
            }
        }
        
        // Validate username if provided
        username?.let {
            if (it.length < 3) return false
        }
        
        return true
    }
}