package np.com.parts.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items")
    fun getAllItems(): Flow<List<CartItem>>
    
    @Query("SELECT * FROM cart_items")
    suspend fun getItemsSync(): List<CartItem>
    
    @Query("SELECT SUM(quantity) FROM cart_items")
    fun getTotalQuantity(): Flow<Int>
    
    @Query("SELECT SUM(quantity) FROM cart_items")
    suspend fun getTotalQuantitySync(): Int
    
    @Query("SELECT * FROM cart_items WHERE id = :itemId")
    suspend fun getItem(itemId: String): CartItem?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: CartItem)
    
    @Update
    suspend fun updateItem(item: CartItem)
    
    @Delete
    suspend fun deleteItem(item: CartItem)
    
    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
    
    @Query("UPDATE cart_items SET quantity = :quantity WHERE id = :itemId")
    suspend fun updateQuantity(itemId: String, quantity: Int)
    
    @Query("UPDATE cart_items SET syncStatus = :status WHERE id = :itemId")
    suspend fun updateSyncStatus(itemId: String, status: SyncStatus)
    
    @Query("SELECT COUNT(*) FROM cart_items")
    fun getItemCount(): Flow<Int>


    @Query("SELECT * FROM cart_items WHERE syncStatus = :status")
    suspend fun getItemsByStatus(status: SyncStatus): List<CartItem>
} 