package np.com.parts.API.Models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LineItem(
    val id: String,
    val productId: Int,
    val name: String,
    val quantity: Int,
    val unitPrice: Money,
    val totalPrice: Money = Money(unitPrice.amount * quantity),
    val discount: Discount? = null,
    val imageUrl: String? = null,
    val lastModified: Long = System.currentTimeMillis() // Add this field
)

@Serializable
data class OrderSummary(
    val subtotal: Money,
    val discount: Money? = null,
    val shippingCost: Money = Money(0),
    val tax: Money? = null,
    val total: Money
) {
    fun formattedSubtotal() = subtotal.formatted()
    fun formattedDiscount() = discount?.formatted() ?: "Rs. 0"
    fun formattedShippingCost() = shippingCost.formatted()
    fun formattedTax() = tax?.formatted() ?: "Rs. 0"
    fun formattedTotal() = total.formatted()
}

@Serializable
data class Cart(
    val userId: UserId,
    val items: List<LineItem>,
    val summary: OrderSummary
)

@Serializable
data class OrderModel(
    @SerialName("id")
    override val id: String,
    val orderNumber: String,
    val items: List<LineItem>,
    val customer: CustomerInfo,
    val payment: PaymentInfo,
    val shippingDetails: ShippingDetails,
    val summary: OrderSummary,
    val status: OrderStatus,
    val tracking: OrderTracking = OrderTracking(),
    val notes: String? = null,
    val source: OrderSource,
    val orderDate: Long = System.currentTimeMillis(),
    override val lastUpdated: Long = System.currentTimeMillis(),
    override val version: Int = 1
) : BaseModel


@Serializable
data class CustomerInfo(
    val id: Int,
    val name: String,
    val type: CustomerType
)

@Serializable
data class PaymentInfo(
    val method: PaymentMethod,
    val status: PaymentStatus,
    val transactionId: String? = null,
    val paidAmount: Money? = null,
    val paidDate: Long? = null
)


@Serializable
enum class OrderStatus {
    @SerialName("PENDING_PAYMENT")
    PENDING_PAYMENT,

    @SerialName("PAYMENT_CONFIRMED")
    PAYMENT_CONFIRMED,

    @SerialName("PROCESSING")
    PROCESSING,

    @SerialName("SHIPPED")
    SHIPPED,

    @SerialName("DELIVERED")
    DELIVERED,

    @SerialName("CANCELLED")
    CANCELLED
}


@Serializable
enum class OrderSource {
    @SerialName("MOBILE_APP")
    MOBILE_APP,
    
    @SerialName("WEB")
    WEB,
    
    @SerialName("POS")
    POS
}

@Serializable
enum class PaymentMethod {
    @SerialName("CASH_ON_DELIVERY")
    CASH_ON_DELIVERY,
    
    @SerialName("KHALTI")
    KHALTI,
    
    @SerialName("ESEWA")
    ESEWA
}

@Serializable
data class ShippingDetails(
    val address: ShippingAddress,
    val method: ShippingMethod,
    val cost: Money
)

@Serializable
data class ShippingAddress(
    val street: String,
    val city: String,
    val province: String,
    val landmark: String?,
    val district: String,
    val ward: Int,
    val recipient: RecipientInfo
)

@Serializable
data class RecipientInfo(
    val name: String,
    val phone: String
)

@Serializable
enum class ShippingMethod {
    @SerialName("STANDARD")
    STANDARD,
    
    @SerialName("EXPRESS")
    EXPRESS
}

@Serializable
data class CreateOrderRequest(
    val items: List<LineItem>,
    val paymentMethod: PaymentMethod,
    val shippingDetails: ShippingDetails,
    val notes: String? = null,
    val source: OrderSource = OrderSource.MOBILE_APP
)
@Serializable
data class TrackingEvent(
    val status: OrderStatus,
    val timestamp: Long = System.currentTimeMillis(),
    val location: String? = null,
    val description: String? = null,
    val updatedBy: String
)

@Serializable
data class OrderTracking(
    val events: List<TrackingEvent> = emptyList(),
    val currentStatus: OrderStatus = OrderStatus.PENDING_PAYMENT,
    val lastUpdated: Long = System.currentTimeMillis()
)