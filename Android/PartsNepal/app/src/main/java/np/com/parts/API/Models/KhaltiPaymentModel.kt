package np.com.parts.API.Models

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class KhaltiPaymentRequest(
    val return_url: String,
    val website_url: String,
    val amount: Int,
    val purchase_order_id: String,
    val purchase_order_name: String,
    val customer_info: KhaltiCustomerInfo,
    val amount_breakdown: List<KhaltiAmountBreakdown>,
    val product_details: List<KhaltiProductDetail>,
    val merchant_username: String,
    val merchant_extra: String
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class KhaltiCustomerInfo(
    val name: String,
    val email: String,
    val phone: String
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class KhaltiAmountBreakdown(
    val label: String,
    val amount: Int
)
@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class KhaltiProductDetail(
    val identity: String,
    val name: String,
    val total_price: Int,
    val quantity: Int,
    val unit_price: Int
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class KhaltiPaymentResponse(
    val pidx: String,                // Unique product index
    val payment_url: String,         // URL for the payment
    val expires_at: String,          // Expiration date and time in ISO format
    val expires_in: Int              // Expiration duration in seconds
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class PaymentRequestModel(
    val purchase_order_name: String,
)
