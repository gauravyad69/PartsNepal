package np.com.parts.system.Models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
data class KhaltiPaymentRequestAsClient(
    val return_url: String = "https://example.com/payment/",
    val website_url: String="https://example.com/",
    val amount: Int,
    val purchase_order_id: String,
    val purchase_order_name: String,
    val customer_info: KhaltiCustomerInfoAsClient,
    val amount_breakdown: List<KhaltiAmountBreakdownAsClient>,
    val product_details: List<KhaltiProductDetailAsClient>,
    val merchant_username: String = "Parts Nepal",
    val merchant_extra: String ="Car auto parts shop - parts.com.np"
)

@Serializable
data class KhaltiCustomerInfoAsClient(
    val name: String,
    val email: String,
    val phone: String
)

@Serializable
data class KhaltiAmountBreakdownAsClient(
    val label: String,
    val amount: Int

)
@Serializable
data class KhaltiProductDetailAsClient(
    val identity: String,
    val name: String,
    val total_price: Int,
    val quantity: Int,
    val unit_price: Int
)

@Serializable
data class KhaltiPaymentResponse(
    val pidx: String,                // Unique product index
    val payment_url: String,         // URL for the payment
    val expires_at: String,          // Expiration date and time in ISO format
    val expires_in: Int              // Expiration duration in seconds
)


@Serializable
data class PaymentRequestModel(
    val purchase_order_name: String,
    )


@Serializable
data class Transactions(
    val userId: UserId,
    val phoneNumber: PhoneNumber,
    val addDate: Long = System.currentTimeMillis(),
    val paidTransactions: List<PaidTransactions>,
    val pidxCreated: List<PidxCreated>
    )
@Serializable
data class PaidTransactions(
    val pidx: String,                // Unique product index
    val amount: Int,
    val amount_breakdown: List<KhaltiAmountBreakdownAsClient>? = null,
    val items: List<LineItem>,
    val addDate: Long = System.currentTimeMillis(),
    val description: String? = null,
)

@Serializable
data class PidxCreated(
    val pidx: String,                // Unique product index
    val orderName: String,
    val addDate: Long = System.currentTimeMillis(),
    val description: String?= null,
)
