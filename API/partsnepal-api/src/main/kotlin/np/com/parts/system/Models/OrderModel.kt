package np.com.parts.system.Models

import kotlinx.serialization.Serializable

@Serializable
data class OrderModel(
    val orderId: String,
    val orderDate: Long,  // Unix timestamp in milliseconds
    val orderStatus: String,  // e.g., "Pending", "Shipped", "Delivered"
    val customerId: String,
    val customerEmail: String,
    val products: List<MainProductDetailsModel>,
    val quantity: Int,
    val paymentDetails: PaymentDetails,
    val orderSummary: OrderSummary,
    val address: ShippingAddressModel,
    val deliveryDetails: DeliveryDetails?,
    val specialInstructions: String?,
    val createdAt: Long,  // Unix timestamp in milliseconds
    val updatedAt: Long   // Unix timestamp in milliseconds
)

@Serializable
data class PaymentDetails(
    val voucherUsed: String,
    val paymentMethod: String,  // e.g., "Credit Card", "Cash on Delivery"
    val transactionId: String?  // Optional, can be null if not available
)

@Serializable
data class OrderSummary(
    val pricePaid: Long,
    val pricePaidIncludingDeliveryCost: Long,
    val discountAmount: Long,
    val taxAmount: Long,
    val totalCost: Long
)

@Serializable
data class DeliveryDetails(
    val deliveryStatus: String?,  // e.g., "Out for Delivery", "Delivered"
    val trackingNumber: String?,
    val expectedDeliveryDate: Long?  // Unix timestamp in milliseconds
)

@Serializable
data class ShippingAddressModel(
    val province: String,
    val region: String,
    val district: String,
    val city: String,
    val address: String,
    val landmark: String?,
    val recipientsName: String,
    val recipientsPhoneNumber: String,
    val email: String?  // Optional
)





//@Serializable
//data class backupmodel(
//    val productId: Int,
//    val productSKU: String,
//    val productName: String,
//    val productDescription: String,
//    val productAddDate: Date,
//    val productType: String,
//    val productStock: Int,
//    val productMainPicture: String,
//    val productPicture: List<String>?,
//    val productReviews: List<String>?,
//    val productMPPrice: String,
//    val productSPPrice: String,
//    val isProductOnSale: Boolean,
//    val isProductAuthentic: Boolean,
//    val productSaleDiscount: String,
//    val hasWarranty: Boolean,
//)