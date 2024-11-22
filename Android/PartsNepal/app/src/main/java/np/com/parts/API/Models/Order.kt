package np.com.parts.API.Models


import kotlinx.serialization.Serializable
import java.util.UUID


@Serializable
data class Order(
    val id: String = UUID.randomUUID().toString(), // Using UUID instead of ObjectId
    val orderNumber: String,
    val orderDate: Long = System.currentTimeMillis(),
    val status: OrderStatus = OrderStatus.PENDING_PAYMENT,
    val customer: CustomerInfo,
    val items: List<OrderItem>,
    val payment: PaymentDetails,
    val summary: OrderSummary,
    val shipping: ShippingDetails,
    val tracking: OrderTracking = OrderTracking(),
    val metadata: OrderMetadata = OrderMetadata()
)

@Serializable
data class CustomerInfo(
    val id: String,
    val email: String,
    val phone: String,
    val name: String,
    val type: CustomerType = CustomerType.INDIVIDUAL
)

@Serializable
enum class CustomerType {
    INDIVIDUAL,
    BUSINESS,
    WHOLESALE
}

@Serializable
data class OrderItem(
    val productId: Int,
    val sku: String,
    val name: String,
    val quantity: Int,
    val unitPrice: Double,
    val discount: Double? = null,
    val finalPrice: Double,
    val status: OrderItemStatus = OrderItemStatus.PROCESSING
)

@Serializable
enum class OrderItemStatus {
    PROCESSING,
    READY,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    RETURNED
}

@Serializable
data class PaymentDetails(
    val method: PaymentMethod,
    val status: PaymentStatus,
    val transactionId: String? = null,
    val paymentDate: Long? = null,
    val gateway: String? = null
)

@Serializable
enum class PaymentMethod {
    CASH_ON_DELIVERY,
    BANK_TRANSFER,
    CREDIT_CARD,
    ESEWA,
    KHALTI
}

@Serializable
enum class PaymentStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    REFUNDED
}

@Serializable
data class OrderSummary(
    val subtotal: Double,
    val discount: Double = 0.0,
    val tax: Double,
    val shipping: Double,
    val total: Double,
    val totalItems: Int,
    val totalQuantity: Int
)

@Serializable
data class ShippingDetails(
    val address: ShippingAddress,
    val method: ShippingMethod,
    val cost: Double,
    val estimatedDeliveryDate: Long? = null,
    val instructions: String? = null
)

@Serializable
data class ShippingAddress(
    val province: String,
    val district: String,
    val city: String,
    val street: String,
    val ward: Int,
    val landmark: String? = null,
    val coordinates: GeoLocation? = null,
    val recipient: RecipientInfo
)

@Serializable
data class GeoLocation(
    val latitude: Double,
    val longitude: Double
)

@Serializable
data class RecipientInfo(
    val name: String,
    val phone: String,
    val alternatePhone: String? = null,
    val email: String? = null
)

@Serializable
enum class ShippingMethod {
    STANDARD,
    EXPRESS,
    PICKUP
}

@Serializable
data class OrderTracking(
    val trackingNumber: String? = null,
    val carrier: String? = null,
    val history: List<TrackingEvent> = emptyList(),
    val currentLocation: String? = null,
    val lastUpdated: Long? = null
)

@Serializable
data class TrackingEvent(
    val status: OrderStatus,
    val timestamp: Long,
    val location: String? = null,
    val description: String? = null
)

@Serializable
data class OrderMetadata(
    val source: OrderSource = OrderSource.MOBILE_APP,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
enum class OrderSource {
    WEB,
    MOBILE_APP,
    IN_STORE,
    PHONE
}

@Serializable
enum class OrderStatus {
    PENDING_PAYMENT,
    PAYMENT_CONFIRMED,
    PROCESSING,
    READY_TO_SHIP,
    OUT_FOR_DELIVERY,
    RETURNED,
    REFUNDED,
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}

@Serializable
data class CreateOrderRequest(
    val items: List<OrderItem>,
    val shippingAddress: ShippingAddress,
    val paymentMethod: PaymentMethod,
    val shippingMethod: ShippingMethod,
    val voucher: String? = null,
    val notes: String? = null
)