package np.com.parts.system.Models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

// Base Order Model
@Serializable
data class OrderModel(
    @BsonId
    override val id: String = ObjectId().toString(),
    val orderNumber: String, // Human-readable order number (e.g., ORD-2024-0001)
    val orderDate: Long,
    val status: OrderStatus,
    val customer: CustomerInfo,
    val items: List<OrderItem>,
    val payment: PaymentDetails,
    val summary: OrderSummary,
    val shipping: ShippingDetails,
    val tracking: OrderTracking,
    val metadata: OrderMetadata,
    override val lastUpdated: Long = System.currentTimeMillis(),
    override val version: Int = 1
) : BaseModel

// Order Status with validation
@Serializable
enum class OrderStatus {
    @SerialName("PENDING_PAYMENT")
    PENDING_PAYMENT,

    @SerialName("PAYMENT_CONFIRMED")
    PAYMENT_CONFIRMED,

    @SerialName("PROCESSING")
    PROCESSING,

    @SerialName("READY_TO_SHIP")
    READY_TO_SHIP,

    @SerialName("SHIPPED")
    SHIPPED,

    @SerialName("OUT_FOR_DELIVERY")
    OUT_FOR_DELIVERY,

    @SerialName("DELIVERED")
    DELIVERED,

    @SerialName("CANCELLED")
    CANCELLED,

    @SerialName("RETURNED")
    RETURNED,

    @SerialName("REFUNDED")
    REFUNDED
}

// Customer Information
@Serializable
data class CustomerInfo(
    val id: Int,
    val email: String?,
    val phone: String,
    val name: String,
    val type: CustomerType = CustomerType.INDIVIDUAL
)

@Serializable
enum class CustomerType {
    @SerialName("INDIVIDUAL")
    INDIVIDUAL,

    @SerialName("BUSINESS")
    BUSINESS,

    @SerialName("WHOLESALE")
    WHOLESALE
}

// Order Items with Product Information
@Serializable
data class OrderItem(
    val productId: Int,
    val sku: String,
    val name: String,
    val quantity: Int,
    val unitPrice: Money,
    val discount: Discount? = null,
    val finalPrice: Money,
    val warranty: WarrantyInfo? = null,
    val status: OrderItemStatus = OrderItemStatus.PROCESSING
)

@Serializable
enum class OrderItemStatus {
    @SerialName("PROCESSING")
    PROCESSING,

    @SerialName("READY")
    READY,

    @SerialName("SHIPPED")
    SHIPPED,

    @SerialName("DELIVERED")
    DELIVERED,

    @SerialName("CANCELLED")
    CANCELLED,

    @SerialName("RETURNED")
    RETURNED
}

// Payment Details
@Serializable
data class PaymentDetails(
    val method: PaymentMethod,
    val status: PaymentStatus,
    val transactionId: String? = null,
    val voucher: VoucherInfo? = null,
    val paymentDate: Long? = null,
    val gateway: String? = null,
)

@Serializable
enum class PaymentMethod {
    @SerialName("CASH_ON_DELIVERY")
    CASH_ON_DELIVERY,

    @SerialName("BANK_TRANSFER")
    BANK_TRANSFER,

    @SerialName("CREDIT_CARD")
    CREDIT_CARD,

    @SerialName("ESEWA")
    ESEWA,

    @SerialName("KHALTI")
    KHALTI
}

@Serializable
enum class PaymentStatus {
    @SerialName("PENDING")
    PENDING,

    @SerialName("PROCESSING")
    PROCESSING,

    @SerialName("COMPLETED")
    COMPLETED,

    @SerialName("FAILED")
    FAILED,

    @SerialName("REFUNDED")
    REFUNDED
}

@Serializable
data class VoucherInfo(
    val code: String,
    val discountAmount: Money,
    val type: VoucherType
)

@Serializable
enum class VoucherType {
    @SerialName("PERCENTAGE")
    PERCENTAGE,

    @SerialName("FIXED_AMOUNT")
    FIXED_AMOUNT
}


// Order Summary
@Serializable
data class OrderSummary(
    val subtotal: Money,
    val discount: Money,
    val tax: TaxInfo,
    val shipping: Money,
    val total: Money,
    val totalItems: Int,
    val totalQuantity: Int
)

@Serializable
data class TaxInfo(
    val amount: Money,
    val rate: Double,
    val breakdown: Map<String, Money> = emptyMap() // Different types of taxes
)

// Shipping Details
@Serializable
data class ShippingDetails(
    val address: ShippingAddress,
    val method: ShippingMethod,
    val cost: Money,
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
    val type: AddressType = AddressType.RESIDENTIAL,
    val recipient: RecipientInfo
)

@Serializable
data class GeoLocation(
    val latitude: Double,
    val longitude: Double
)

@Serializable
enum class AddressType {
    @SerialName("RESIDENTIAL")
    RESIDENTIAL,

    @SerialName("BUSINESS")
    BUSINESS
}

@Serializable
data class RecipientInfo(
    val name: String,
    val phone: String,
    val alternatePhone: String? = null,
    val email: String? = null
)

@Serializable
enum class ShippingMethod {
    @SerialName("STANDARD")
    STANDARD,

    @SerialName("EXPRESS")
    EXPRESS,

    @SerialName("PICKUP")
    PICKUP
}

// Order Tracking
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
    val description: String? = null,
    val updatedBy: String? = null
)

// Order Metadata
@Serializable
data class OrderMetadata(
    val source: OrderSource,
    val ipAddress: String? = null,
    val userAgent: String? = null,
    val notes: List<OrderNote> = emptyList(),
    val tags: Set<String> = emptySet(),
    val createdAt: Long = System.currentTimeMillis(),
    val createdBy: String? = null
)

@Serializable
enum class OrderSource {
    @SerialName("WEB")
    WEB,

    @SerialName("MOBILE_APP")
    MOBILE_APP,

    @SerialName("IN_STORE")
    IN_STORE,

    @SerialName("PHONE")
    PHONE
}

@Serializable
data class OrderNote(
    val id: String = ObjectId().toString(),
    val message: String,
    val type: NoteType,
    val createdAt: Long = System.currentTimeMillis(),
    val createdBy: String
)

@Serializable
enum class NoteType {
    @SerialName("CUSTOMER")
    CUSTOMER,

    @SerialName("INTERNAL")
    INTERNAL,

    @SerialName("SYSTEM")
    SYSTEM
}