package np.com.parts.API.Models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Money(
    val amount: Long,
    val currency: String = "NPR"
)

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