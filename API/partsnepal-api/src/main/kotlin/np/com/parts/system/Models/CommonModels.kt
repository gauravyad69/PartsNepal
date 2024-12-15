package np.com.parts.system.Models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface BaseModel {
    val id: String
    val lastUpdated: Long
    val version: Int
}

@Serializable
data class Money(
    var amount: Long,
    val currency: String = "NPR"
) {
    fun formatted(): String = "Rs. ${java.text.NumberFormat.getNumberInstance().format(amount)}"


}

@Serializable
data class Discount(
    val amount: Money,
    val type: DiscountType,
    val description: String? = null,
    val startDate: Long? = null,
    val endDate: Long? = null
) {
    val isActive: Boolean
        get() = when {
            startDate == null && endDate == null -> true
            startDate == null -> System.currentTimeMillis() <= endDate!!
            endDate == null -> System.currentTimeMillis() >= startDate
            else -> System.currentTimeMillis() in startDate..endDate
        }
}

@Serializable
enum class DiscountType {
    @SerialName("PERCENTAGE")
    PERCENTAGE,

    @SerialName("FIXED_AMOUNT")
    FIXED_AMOUNT
}


fun Money.formatted(): String {
    return "Rs. ${java.text.NumberFormat.getNumberInstance().format(amount)}"
}

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

fun Long.toKhaltiAmount(): Int {
    var amount= Money(this).amount
    return amount.toInt()
}