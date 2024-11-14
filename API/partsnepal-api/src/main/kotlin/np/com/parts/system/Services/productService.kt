package np.com.parts.system.Services

import com.mongodb.ErrorCategory
import com.mongodb.MongoCommandException
import com.mongodb.MongoWriteException
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import np.com.parts.system.Models.*
import org.litote.kmongo.getCollection
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.minutes

class ProductService(private val database: MongoDatabase) {
    private lateinit var productCollection: MongoCollection<ProductModel>
    private lateinit var mainDetailsCollection: MongoCollection<MainProductDetailsModel>
    private lateinit var fullDetailsCollection: MongoCollection<FullProductDetailsModel>

    // In-memory cache for frequently accessed data
    private val mainDetailsCache = ConcurrentHashMap<Int, CachedItem<MainProductDetailsModel>>()
    private val fullDetailsCache = ConcurrentHashMap<Int, CachedItem<FullProductDetailsModel>>()

    private data class CachedItem<T>(
        val data: T,
        val timestamp: Long = System.currentTimeMillis(),
        val version: Int
    )

    init {
        try {
            database.createCollection("products")
            database.createCollection("main_details")
            database.createCollection("full_details")
        } catch (e: MongoCommandException) {
            // Collections already exist, ignore the error
        }

        productCollection = database.getCollection<ProductModel>("products")
        mainDetailsCollection = database.getCollection<MainProductDetailsModel>("main_details")
        fullDetailsCollection = database.getCollection<FullProductDetailsModel>("full_details")

        // Create indexes
        createIndexes()
    }

    private fun createIndexes() {
        // Main details indexes
        mainDetailsCollection.createIndexes(
            listOf(
                IndexModel(Indexes.ascending("productId"), IndexOptions().unique(true)),
                IndexModel(Indexes.ascending("productSKU"), IndexOptions().unique(true)),
                IndexModel(Indexes.text("productName")),
                IndexModel(Indexes.ascending("productType")),
                IndexModel(Indexes.ascending("productStock")),
                IndexModel(Indexes.ascending("isProductOnSale")),
                IndexModel(Indexes.ascending("productSPPrice"))
            )
        )

        // Full details indexes
        fullDetailsCollection.createIndexes(
            listOf(
                IndexModel(Indexes.ascending("productId"), IndexOptions().unique(true)),
                IndexModel(Indexes.text("productDescription")),
                IndexModel(Indexes.ascending("pricing.productManufacturer")),
                IndexModel(Indexes.ascending("pricing.isProductAuthentic"))
            )
        )
    }

    // Cache management
    private suspend fun getMainDetailsWithCache(productId: Int): MainProductDetailsModel? {
        val cached = mainDetailsCache[productId]
        if (cached != null && System.currentTimeMillis() - cached.timestamp < 5.minutes.inWholeMilliseconds) {
            return cached.data
        }

        return withContext(Dispatchers.IO) {
            mainDetailsCollection.find(Filters.eq("productId", productId))
                .firstOrNull()
                ?.also { mainDetails ->
                    mainDetailsCache[productId] = CachedItem(mainDetails, version = mainDetails.version)
                }
        }
    }

    private suspend fun getFullDetailsWithCache(productId: Int): FullProductDetailsModel? {
        val cached = fullDetailsCache[productId]
        if (cached != null && System.currentTimeMillis() - cached.timestamp < 5.minutes.inWholeMilliseconds) {
            return cached.data
        }

        return withContext(Dispatchers.IO) {
            fullDetailsCollection.find(Filters.eq("productId", productId))
                .firstOrNull()
                ?.also { fullDetails ->
                    fullDetailsCache[productId] = CachedItem(fullDetails, version = fullDetails.version)
                }
        }
    }

    // Create operations
    suspend fun createProduct(product: ProductModel): Boolean = withContext(Dispatchers.IO) {
        try {
            // Insert into separate collections
            mainDetailsCollection.insertOne(product.mainDetails)
            fullDetailsCollection.insertOne(product.fullDetails)
            productCollection.insertOne(product)
            true
        } catch (e: MongoWriteException) {
            if (e.error.category == ErrorCategory.DUPLICATE_KEY) {
                false
            } else {
                throw e
            }
        }
    }

    // Read operations
    suspend fun getProductById(productId: Int): ProductModel? = withContext(Dispatchers.IO) {
        val mainDetails = getMainDetailsWithCache(productId) ?: return@withContext null
        val fullDetails = getFullDetailsWithCache(productId) ?: return@withContext null
        ProductModel(
            id = productId.toString(),
            mainDetails = mainDetails,
            fullDetails = fullDetails
        )
    }

    // Efficient batch operations using Kotlin Flow
    fun getAllProductsFlow(batchSize: Int = 100): Flow<ProductModel> = flow {
        var skip = 0
        while (true) {
            val batch = withContext(Dispatchers.IO) {
                mainDetailsCollection.find()
                    .skip(skip)
                    .limit(batchSize)
                    .toList()
            }
            if (batch.isEmpty()) break

            batch.forEach { mainDetails ->
                getFullDetailsWithCache(mainDetails.productId)?.let { fullDetails ->
                    emit(
                        ProductModel(
                            id = mainDetails.productId.toString(),
                            mainDetails = mainDetails,
                            fullDetails = fullDetails
                        )
                    )
                }
            }
            skip += batchSize
        }
    }

    fun getAllMainProductsFlow(batchSize: Int = 100): Flow<Main> = flow {
        var skip = 0
        while (true) {
            val batch = withContext(Dispatchers.IO) {
                mainDetailsCollection.find()
                    .skip(skip)
                    .limit(batchSize)
                    .toList()
            }
            if (batch.isEmpty()) break

            batch.forEach { mainDetails ->
                getFullDetailsWithCache(mainDetails.productId)?.let { fullDetails ->
                    emit(
                        ProductModel(
                            id = mainDetails.productId.toString(),
                            mainDetails = mainDetails,
                            fullDetails = fullDetails
                        )
                    )
                }
            }
            skip += batchSize
        }
    }

    // Update operations
    suspend fun updateMainDetails(productId: Int, updates: Map<String, Any>): Boolean = withContext(Dispatchers.IO) {
        val updateResult = mainDetailsCollection.updateOne(
            Filters.eq("productId", productId),
            Updates.combine(
                updates.map { (field, value) -> Updates.set(field, value) } +
                        listOf(
                            Updates.set("lastUpdated", System.currentTimeMillis()),
                            Updates.inc("version", 1)
                        )
            )
        )
        if (updateResult.modifiedCount > 0) {
            mainDetailsCache.remove(productId)
            true
        } else false
    }

    suspend fun updateFullDetails(productId: Int, updates: Map<String, Any>): Boolean = withContext(Dispatchers.IO) {
        val updateResult = fullDetailsCollection.updateOne(
            Filters.eq("productId", productId),
            Updates.combine(
                updates.map { (field, value) -> Updates.set(field, value) } +
                        listOf(
                            Updates.set("lastUpdated", System.currentTimeMillis()),
                            Updates.inc("version", 1)
                        )
            )
        )
        if (updateResult.modifiedCount > 0) {
            fullDetailsCache.remove(productId)
            true
        } else false
    }

    // Add review with rating update
    suspend fun addProductReview(productId: Int, review: ProductReview): Boolean = withContext(Dispatchers.IO) {
        val fullDetails = getFullDetailsWithCache(productId) ?: return@withContext false

        // Calculate new rating
        val currentRating = fullDetails.features.productRating
        val newTotalRatings = currentRating.totalRatings + 1
        val newDistribution = currentRating.ratingDistribution.toMutableMap()
        newDistribution[review.rating] = (newDistribution[review.rating] ?: 0) + 1

        val newAverageRating = (currentRating.averageRating * currentRating.totalRatings + review.rating) / newTotalRatings

        val updateResult = fullDetailsCollection.updateOne(
            Filters.eq("productId", productId),
            Updates.combine(
                Updates.push("features.productReviews", review),
                Updates.set("features.productRating", ProductRating(
                    averageRating = newAverageRating,
                    totalRatings = newTotalRatings,
                    ratingDistribution = newDistribution
                )),
                Updates.set("lastUpdated", System.currentTimeMillis()),
                Updates.inc("version", 1)
            )
        )

        if (updateResult.modifiedCount > 0) {
            fullDetailsCache.remove(productId)
            true
        } else false
    }

    // Search operations with pagination
    suspend fun searchProducts(
        query: String,
        page: Int = 0,
        pageSize: Int = 20,
        filters: Map<String, Any> = emptyMap()
    ): Flow<ProductModel> = flow {
        val textFilter = Filters.text(query)
        val additionalFilters = filters.map { (field, value) -> Filters.eq(field, value) }
        val combinedFilters = if (additionalFilters.isEmpty()) {
            textFilter
        } else {
            Filters.and(textFilter, *additionalFilters.toTypedArray())
        }

        val mainDetailsList = withContext(Dispatchers.IO) {
            mainDetailsCollection.find(combinedFilters)
                .skip(page * pageSize)
                .limit(pageSize)
                .toList()
        }

        mainDetailsList.forEach { mainDetails ->
            getFullDetailsWithCache(mainDetails.productId)?.let { fullDetails ->
                emit(
                    ProductModel(
                        id = mainDetails.productId.toString(),
                        mainDetails = mainDetails,
                        fullDetails = fullDetails
                    )
                )
            }
        }
    }

    // Cleanup expired cache entries
    private suspend fun cleanupCache() = withContext(Dispatchers.IO) {
        val expiryTime = System.currentTimeMillis() - 5.minutes.inWholeMilliseconds
        mainDetailsCache.entries.removeIf { it.value.timestamp < expiryTime }
        fullDetailsCache.entries.removeIf { it.value.timestamp < expiryTime }
    }
}