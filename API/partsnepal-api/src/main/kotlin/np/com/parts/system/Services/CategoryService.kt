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
import kotlin.math.absoluteValue

@Serializable
data class CategoryModel(
    @BsonId val categoryId: String = UUID.randomUUID().toString(), // Use UUID for unique IDs
    val categoryName: String,
    val subCategoryName: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val version: Int = 1
)

@Serializable
data class CategoryModelReq(
    val categoryName: String,
    val subCategoryName: String,
    val createdAt: Long?,
    val updatedAt: Long?,
    val version: Int?
)

class CategoryService(private val database: MongoDatabase) {
    private var collection: MongoCollection<CategoryModel>

    init {
        try {
            database.createCollection("categories")
        } catch (e: Exception) {
            // Collection already exists
        }
        collection = database.getCollection<CategoryModel>("categories")
    }

     fun createCategory(categoryName: String, subCategoryName: String): CategoryModel {
        val category = CategoryModel(categoryName=categoryName, subCategoryName=subCategoryName)
        collection.insertOne(category)
        return category
    }

     fun getCategory(id: String): CategoryModel? {
        return collection.findOneById(id)
    }

    fun getAllCategory(): List<CategoryModel> {
        return collection.find().toList()
    }



     fun updateCategoryName(id: String, newCategoryName: String): Boolean{
        val paste = collection.findOneById(id) // Find the document
        return if (paste != null) {
            val updates=  Updates.combine(
                Updates.set("categoryName", newCategoryName),
                Updates.set("updatedAt", System.currentTimeMillis()),
                Updates.inc("version", 1),
            )
            collection.updateOneById(id, updates) // Update the document
            return true
        } else {
            false
        }
    }

    fun updateSubCategoryName(id: String, newSubCategoryName: String): Boolean{
        val paste = collection.findOneById(id) // Find the document
        return if (paste != null) {
            val updates=  Updates.combine(
                Updates.set("subCategoryName", newSubCategoryName),
                Updates.set("updatedAt", System.currentTimeMillis()),
                Updates.inc("version", 1),
            )
            collection.updateOneById(id, updates) // Update the document
            return true
        } else {
            false
        }
    }
     fun updateCategoryModel(id: String, categoryName: String, subCategoryName: String): Boolean{
        val paste = collection.findOneById(id) // Find the document
        return if (paste != null) {
            val updates=  Updates.combine(
                Updates.set("categoryName", categoryName),
                Updates.set("subCategoryName", subCategoryName),
                Updates.set("updatedAt", System.currentTimeMillis()),
                Updates.inc("version", 1),
            )
            collection.updateOneById(id, updates) // Update the document
            return true
        } else {
            false
        }
    }

     fun deleteCategory(id: String): Boolean {
        val result = collection.deleteOneById(id)
        return result.deletedCount > 0
    }
}
