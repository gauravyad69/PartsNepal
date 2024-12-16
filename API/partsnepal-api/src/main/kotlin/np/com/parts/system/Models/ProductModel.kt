package np.com.parts.system.Models

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import kotlinx.serialization.SerialName
import org.bson.types.ObjectId

// Base interface for all models with common fields

// Sealed interface for product-related models
@Serializable
sealed interface ProductRelated {
    val productId: Int
}

// Core product information interface
@Serializable
sealed interface IProductInfo : ProductRelated {
    val productSKU: String
    val productName: String
    val categoryId: String
}

@Serializable
data class ProductModel(
    @BsonId
    override val id: String = ObjectId().toString(),
    val basic: BasicProductInfo,
    val details: DetailedProductInfo,
    override val lastUpdated: Long = System.currentTimeMillis(),
    override val version: Int = 1
) : BaseModel, ProductRelated {
    override val productId: Int
        get() = basic.productId
}

@Serializable
data class BasicProductInfo(
    override val productId: Int,
    override val productSKU: String,
    override val productName: String,
    override val categoryId: String,
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

// Rest of your data classes remain the same, just add @Serializable to each
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
    val averageRating: Double
        get() = summary.averageRating
}

@Serializable
data class Review(
    @BsonId
    override val id: String = ObjectId().toString(),
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

// Extension functions for common operations
fun ProductModel.toBasicView() = BasicProductView(
    id = id,
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

// Utility extension functions for timestamp handling
fun Long.toFormattedDate(): String{
    return java.time.format.DateTimeFormatter
        .ISO_INSTANT
        .format(java.time.Instant.ofEpochMilli(this))
}

fun Long.toSeconds(): Long {
    return this / 1000
}

fun String.toTimestamp(): Long {
    return java.time.Instant.parse(this).toEpochMilli()
}