package np.com.parts.API.Repository

import android.annotation.SuppressLint
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import np.com.parts.API.Models.ProductModel
import np.com.parts.API.Models.BasicProductView
import kotlinx.serialization.Serializable
import np.com.parts.API.BASE_URL
import np.com.parts.API.PRODUCTS_PATH
import java.util.UUID
import javax.inject.Inject

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class Paste(
    val id: String, // Use UUID for unique IDs
    val title: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val version: Int = 1
)

class MiscRepository @Inject constructor(
    private val client: HttpClient
) {

    // Get all products
    suspend fun getAllPastes(
    ): Result<List<Paste>> = try {
        val response: List<Paste> = client.get("$BASE_URL/paste") {
        }.body()
        Result.success(response)
    } catch (e: Exception) {
        Result.failure(e)
    }


    // Get product by ID
    suspend fun getPasteById(
        pasteId: String
    ): Result<Paste> = try {
        println("Making request to: $BASE_URL/paste/$pasteId")  // Add logging

        val response: Paste= client.get("$BASE_URL/paste/$pasteId").body()


        Result.success(response)
    } catch (e: Exception) {
        println("Error in getPasteById: ${e.message}")  // Add error logging
        e.printStackTrace()
        Result.failure(e)
    }
}