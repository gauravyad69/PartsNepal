package np.com.parts.system.Models
import kotlinx.serialization.Serializable


@Serializable
data class UserModel(
    val userId: Int,  // Core user identifier
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val isBusinessAccount: Boolean
)

@Serializable
data class FullUserDetails(
    val user: UserModel,  // Reference to the UserModel, so you can access basic user details
    val totalReviews: Int,
    val reviewHistory: List<String>,
    val orders: List<OrderModel>,  // Assuming OrderModel is defined elsewhere
    val totalOrders: Int,  // Number of total orders
    val preferences: Map<String, String>,  // system preferences in key-value pairs
    val accountStatus: String,  // Account status: "ACTIVE", "SUSPENDED", etc.
    val totalTimeSpent: Long,  // Time spent on the platform
    val engagementScore: Int?,  // system activity score
)


//val supportTickets: List<TicketModel>?  // List of support tickets submitted by the user
//@Serializable
//data class TicketModel(
//    val ticketId: Int
//    )





