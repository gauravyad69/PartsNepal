package np.com.parts.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import np.com.parts.API.Models.LineItem
import np.com.parts.API.Models.Money
import np.com.parts.API.Repository.CartItemSync
import java.lang.System

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey
    val id: String,
    val productId: Int,
    val name: String,
    val quantity: Int,
    val unitPrice: Money,
    val imageUrl: String?,
    val syncStatus: SyncStatus = SyncStatus.PENDING
) {
    fun toLineItem() = LineItem(
        id = id,
        productId = productId,
        name = name,
        quantity = quantity,
        unitPrice = unitPrice,
        imageUrl = imageUrl
    )

    fun toCartItemSync() = CartItemSync(
        id = id,
        productId = productId,
        quantity = quantity,
        lastModified = System.currentTimeMillis()
    )
    companion object {
        fun fromLineItem(item: LineItem) = CartItem(
            id = item.id,
            productId = item.productId,
            name = item.name,
            quantity = item.quantity,
            unitPrice = item.unitPrice,
            imageUrl = item.imageUrl
        )
    }
}

enum class SyncStatus {
    SYNCED,
    PENDING,
    FAILED
} 