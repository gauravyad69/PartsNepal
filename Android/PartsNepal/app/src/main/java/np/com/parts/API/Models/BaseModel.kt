package np.com.parts.API.Models
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
interface BaseModel {
    val id: String
    val lastUpdated: Long
    val version: Int
}

@Serializable
data class Money(
    val amount: Double,
    val currency: String = "NPR"
)

@Serializable
data class Discount(
    val amount: Money,
    val type: DiscountType,
    val description: String? = null
)

@Serializable
enum class DiscountType {
    @SerialName("PERCENTAGE")
    PERCENTAGE,

    @SerialName("FIXED_AMOUNT")
    FIXED_AMOUNT
}

@Serializable
data class WarrantyInfo(
    val duration: Int, // in months
    val type: String,
    val description: String? = null
)