package np.com.parts.API.Models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable




@Serializable
enum class CustomerType {
    @SerialName("INDIVIDUAL")
    INDIVIDUAL,
    
    @SerialName("BUSINESS")
    BUSINESS
}


@Serializable
enum class PaymentStatus {
    @SerialName("PENDING")
    PENDING,

    @SerialName("INITIATED")
    INITIATED,

    @SerialName("COMPLETED")
    COMPLETED,

    @SerialName("FAILED")
    FAILED,

    @SerialName("REFUNDED")
    REFUNDED,

    @SerialName("ON_HOLD")
    ON_HOLD
}

// Extension functions
fun OrderModel.formattedTotal(): String = summary.total.formatted()

fun OrderModel.formattedDate(): String =
    java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
        .format(java.util.Date(orderDate))