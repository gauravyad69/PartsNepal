package np.com.parts.API.Models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
sealed interface ProductRelated {

    val productId: Int
}

@Serializable
sealed interface IProductInfo : ProductRelated {
    val productSKU: String
    val productName: String
    val productType: String
}

@Serializable
data class ProductModel(
    @SerialName("id")  // Add this
    override val id: String,
    val basic: BasicProductInfo,
    val details: DetailedProductInfo,
    override val lastUpdated: Long = System.currentTimeMillis(),
    override val version: Int = 1
) : BaseModel, ProductRelated {
    @Transient
    override val productId: Int
        get() = basic.productId
}
@Serializable
data class BasicProductInfo(
    override val productId: Int,
    override val productSKU: String,
    override val productName: String,
    override val productType: String,
    val inventory: InventoryInfo,
    val pricing: PricingInfo
) : IProductInfo

@Serializable
data class DetailedProductInfo(
    override val productId: Int,
    val description: String,
    val addDate: Long = System.currentTimeMillis(),
    val features: Features,
    val delivery: DeliveryInfo,
    val warranty: WarrantyInfo
) : ProductRelated

@Serializable
data class InventoryInfo(
    val stock: Int,
    val mainImage: String,
    val isAvailable: Boolean = stock > 0
)

@Serializable
data class PricingInfo(
    val regularPrice: Money,
    val salePrice: Money? = null,
    val discount: Discount? = null
) {
    @Transient
    val isOnSale: Boolean
        get() = salePrice != null && discount != null
}


@Serializable
data class Features(
    val highlights: List<String> = emptyList(),
    val images: List<ProductImage> = emptyList(),
    val reviews: Reviews = Reviews()
)

@Serializable
data class ProductImage(
    val url: String,
    val alt: String,
    val isPrimary: Boolean = false,
    val order: Int = 0
)

@Serializable
data class Reviews(
    val items: List<Review> = emptyList(),
    val summary: ReviewSummary = ReviewSummary()
) {
    @Transient
    val averageRating: Double
        get() = summary.averageRating
}

@Serializable
data class Review(
    @SerialName("id")
    override val id: String,
    val userId: String,
    val rating: Int,
    val comment: String,
    override val lastUpdated: Long = System.currentTimeMillis(),
    override val version: Int = 1
) : BaseModel

@Serializable
data class ReviewSummary(
    val averageRating: Double = 0.0,
    val totalCount: Int = 0,
    val distribution: Map<Int, Int> = emptyMap()
)

@Serializable
data class DeliveryInfo(
    val options: Set<DeliveryOption> = emptySet(),
    val estimatedDays: Int = 3,
    val shippingCost: Money = Money(0)
)

@Serializable
enum class DeliveryOption {
    STORE_PICKUP,
    STANDARD_DELIVERY,
    EXPRESS_DELIVERY,
    INTERNATIONAL_SHIPPING
}

@Serializable
data class WarrantyInfo(
    val isReturnable: Boolean = false,
    val returnPeriodDays: Int = 0,
    val warrantyMonths: Int = 0,
    val terms: List<String> = emptyList()
)

// Helper extension for basic view
fun ProductModel.toBasicView() = BasicProductView(
    id = id ?: "",
    basic = basic,
    lastUpdated = lastUpdated,
    version = version
)

@Serializable
data class BasicProductView(
    override val id: String,
    val basic: BasicProductInfo,
    override val lastUpdated: Long,
    override val version: Int
) : BaseModel

// Utility functions for Android
object DateUtils {
    fun Long.toFormattedDate(): String {
        return android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss", this).toString()
    }

    fun String.toTimestamp(): Long {
        return try {
            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
                .parse(this)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}