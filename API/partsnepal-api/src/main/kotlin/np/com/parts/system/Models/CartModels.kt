package np.com.parts.system.Models

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class Cart(
    val userId: UserId,
    val items: List<LineItem>,
    val summary: OrderSummary
)

@Serializable
data class CartOperation(
    val productId: Int,
    val quantity: Int
)