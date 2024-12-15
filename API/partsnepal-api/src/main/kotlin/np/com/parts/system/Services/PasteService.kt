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
data class Paste(
    @BsonId val id: String = UUID.randomUUID().toString(), // Use UUID for unique IDs
    val title: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val version: Int = 1
)

@Serializable
data class PasteReq(
    val title: String?=null,
    val content: String?=null,
)


class PasteService(private val database: MongoDatabase) {
    private lateinit var collection: MongoCollection<Paste>

    init {
        try {
            database.createCollection("pastes")
        } catch (e: Exception) {
            // Collection already exists
        }
        collection = database.getCollection<Paste>("pastes")
    }

     fun createPaste(title: String,content: String): Paste {
        val paste = Paste(title =title, content=content)
        collection.insertOne(paste)
        return paste
    }

     fun getPaste(id: String): Paste? {
        return collection.findOneById(id)
    }

    fun getAllPaste(): List<Paste> {
        return collection.find().toList()
    }



    suspend fun updateTitle(id: String, newTitle: String): Boolean{
        val paste = collection.findOneById(id) // Find the document
        return if (paste != null) {
            val updates=  Updates.combine(
                Updates.set("title", newTitle),
                Updates.set("updatedAt", System.currentTimeMillis()),
                Updates.inc("version", 1),
            )
            collection.updateOneById(id, updates) // Update the document
            return true
        } else {
            false
        }
    }
    suspend fun updatePaste(id: String, newTitle: String, newContent:String): Boolean{
        val paste = collection.findOneById(id) // Find the document
        return if (paste != null) {
            val updates=  Updates.combine(
                Updates.set("title", newTitle),
                Updates.set("title", newContent),
                Updates.set("updatedAt", System.currentTimeMillis()),
                Updates.inc("version", 1),
            )
            collection.updateOneById(id, updates) // Update the document
            return true
        } else {
            false
        }
    }


    suspend fun updatePasteCContent(id: String, newContent:String): Boolean{
        val paste = collection.findOneById(id) // Find the document
        return if (paste != null) {
            val updates=  Updates.combine(
                Updates.set("title", newContent),
                Updates.set("updatedAt", System.currentTimeMillis()),
                Updates.inc("version", 1),
            )
            collection.updateOneById(id, updates) // Update the document
            return true
        } else {
            false
        }
    }
    suspend fun deletePaste(id: String): Boolean {
        val result = collection.deleteOneById(id)
        return result.deletedCount > 0
    }
}
