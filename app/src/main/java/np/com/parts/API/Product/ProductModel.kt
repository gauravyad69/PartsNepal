package np.com.parts.API.Product

import kotlinx.serialization.Serializable
import java.sql.Date



@Serializable
data class MainProductDetailsModel(
    val productId: Int,
    val productSKU: String,
    val productName: String,
    val productType: String,
    val productStock: Int,
    val productMainPicture: String,
    val productSPPrice: Long,
    val isProductOnSale: Boolean,
    val productSaleDiscount: String,
)

@Serializable
data class FullProductDetailsModel(
    val mainDetails: MainProductDetailsModel,
    val productDescription: String,
    val productAddDate: Date,
    val productFeatures: List<String>?,
    val productPicture: List<String>?,
    val productReviews: List<String>?,
    val productRating: Long,
    val productManufacturer: String,
    val productMPPrice: Long,
    val isProductAuthentic: Boolean,
    val hasWarranty: Boolean,
    val hasStorePickup: Boolean,
    val hasCourierDelivery: Boolean,
    val hasAirCourierDelivery: Boolean,
    val isReturnable: Boolean,
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