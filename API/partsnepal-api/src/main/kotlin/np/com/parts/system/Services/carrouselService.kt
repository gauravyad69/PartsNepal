package np.com.parts.system.Services

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import kotlinx.serialization.Serializable
import np.com.parts.system.Models.Transactions
import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.*
import java.util.*


@Serializable
data class Carrousel(
    @BsonId val id: String = UUID.randomUUID().toString(), // Use UUID for unique IDs
    val carrouselId: String,
    val imageUrl: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val version: Int = 1
)

@Serializable
data class CarrouselReq(
    val carrouselId: String?=null,
    val imageUrl: String?=null,
)


class CarrouselService(private val database: MongoDatabase) {
    private lateinit var collection: MongoCollection<Carrousel>

    init {
        try {
            database.createCollection("carrousel")
        } catch (e: Exception) {
            // Collection already exists
        }
        collection = database.getCollection<Carrousel>("carrousel")
    }

     fun createCarrousel(carrouselId: String,imageUrl: String): Carrousel {
        val Carrousel = Carrousel(carrouselId =carrouselId, imageUrl=imageUrl)
        collection.insertOne(Carrousel)
        return Carrousel
    }

     fun getCarrousel(id: String): Carrousel? {
        return collection.findOne(Carrousel::carrouselId eq id)
    }

    fun getAllCarrousel(): List<Carrousel> {
        return collection.find().toList()
    }



     fun updateTitle(id: String, newTitle: String): Boolean{
        val Carrousel = collection.findOneById(id) // Find the document
        return if (Carrousel != null) {
            val updates=  Updates.combine(
                Updates.set("carrouselId", newTitle),
                Updates.set("updatedAt", System.currentTimeMillis()),
                Updates.inc("version", 1),
            )
            collection.updateOneById(id, updates) // Update the document
            return true
        } else {
            false
        }
    }
    suspend fun updateCarrousel(carrouselId: String, newTitle: String, newContent:String): Boolean{
        val Carrousel = collection.findOne(Carrousel::carrouselId eq carrouselId) // Find the document
        return if (Carrousel != null) {
            val updates=  Updates.combine(
                Updates.set("carrouselId", newTitle),
                Updates.set("imageUrl", newContent),
                Updates.set("updatedAt", System.currentTimeMillis()),
                Updates.inc("version", 1),
            )
            collection.updateOne(Carrousel::carrouselId eq carrouselId, updates) // Update the document
            return true
        } else {
            false
        }
    }


    suspend fun updateCarrouselCContent(id: String, newContent:String): Boolean{
        val Carrousel = collection.findOneById(id) // Find the document
        return if (Carrousel != null) {
            val updates=  Updates.combine(
                Updates.set("carrouselId", newContent),
                Updates.set("updatedAt", System.currentTimeMillis()),
                Updates.inc("version", 1),
            )
            collection.updateOne(id, updates) // Update the document
            return true
        } else {
            false
        }
    }
     fun deleteCarrousel(id: String): Boolean {
        val result = collection.deleteOne(Carrousel::carrouselId eq id)
        return result.deletedCount > 0
    }
}
