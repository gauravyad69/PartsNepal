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

    // In-memory cache for frequently accessed products
    private val productCache = ConcurrentHashMap<Int, CachedItem<ProductModel>>()

    private data class CachedItem<T>(
        val data: T,
        val timestamp: Long = System.currentTimeMillis(),
        val version: Int
    )

    init {
        try {
            database.createCollection("products")
        } catch (e: MongoCommandException) {
            // Collection already exists, ignore the error
        }

        productCollection = database.getCollection<ProductModel>("products")
        createIndexes()
    }

    private fun createIndexes() {
        productCollection.createIndexes(
            listOf(
                // Basic info indexes
                IndexModel(Indexes.ascending("basic.productId"), IndexOptions().unique(true)),
                IndexModel(Indexes.ascending("basic.productSKU"), IndexOptions().unique(true)),
                IndexModel(Indexes.text("basic.productName")),
                IndexModel(Indexes.ascending("basic.categoryId")),

                // Inventory indexes
                IndexModel(Indexes.ascending("basic.inventory.stock")),
                IndexModel(Indexes.ascending("basic.inventory.isAvailable")),

                // Pricing indexes
                IndexModel(Indexes.ascending("basic.pricing.regularPrice.amount")),
                IndexModel(Indexes.ascending("basic.pricing.salePrice.amount")),

                // Details indexes
//                IndexModel(Indexes.text("details.description")),
                IndexModel(Indexes.ascending("details.addDate")),

                // Review indexes
                IndexModel(Indexes.ascending("details.features.reviews.summary.averageRating")),
                IndexModel(Indexes.ascending("details.features.reviews.summary.totalCount"))
            )
        )
    }

    // Cache management
    private suspend fun getProductWithCache(productId: Int): ProductModel? {
        val cached = productCache[productId]
        if (cached != null && System.currentTimeMillis() - cached.timestamp < 5.minutes.inWholeMilliseconds) {
            return cached.data
        }

        return withContext(Dispatchers.IO) {
            productCollection.find(Filters.eq("basic.productId", productId))
                .firstOrNull()
                ?.also { product ->
                    productCache[productId] = CachedItem(product, version = product.version)
                }
        }
    }

    // Create operations
    suspend fun createProduct(product: ProductModel): Boolean = withContext(Dispatchers.IO) {
        try {
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
    suspend fun getProductById(productId: Int): ProductModel? =
        getProductWithCache(productId)

    suspend fun getBasicProductById(productId: Int): BasicProductView? =
        getProductWithCache(productId)?.toBasicView()

    // Efficient batch operations using Kotlin Flow
    fun getAllProductsFlow(batchSize: Int = 100): Flow<ProductModel> = flow {
        var skip = 0
        while (true) {
            val batch = withContext(Dispatchers.IO) {
                productCollection.find()
                    .skip(skip)
                    .limit(batchSize)
                    .toList()
            }
            if (batch.isEmpty()) break

            batch.forEach { product ->
                emit(product)
            }
            skip += batchSize
        }
    }

    fun getAllBasicProductsFlow(batchSize: Int = 100): Flow<BasicProductView> =
        getAllProductsFlow(batchSize).map { it.toBasicView() }

    // Update operations
    suspend fun updateBasicInfo(productId: Int, updates: Map<String, Any>): Boolean = withContext(Dispatchers.IO) {
        val updateResult = productCollection.updateOne(
            Filters.eq("basic.productId", productId),
            Updates.combine(
                updates.map { (field, value) -> Updates.set("basic.$field", value) } +
                        listOf(
                            Updates.set("lastUpdated", System.currentTimeMillis()),
                            Updates.inc("version", 1)
                        )
            )
        )
        if (updateResult.modifiedCount > 0) {
            productCache.remove(productId)
            true
        } else false
    }

    suspend fun updateDetailedInfo(productId: Int, updates: Map<String, Any>): Boolean = withContext(Dispatchers.IO) {
        val updateResult = productCollection.updateOne(
            Filters.eq("basic.productId", productId),
            Updates.combine(
                updates.map { (field, value) -> Updates.set("details.$field", value) } +
                        listOf(
                            Updates.set("lastUpdated", System.currentTimeMillis()),
                            Updates.inc("version", 1)
                        )
            )
        )
        if (updateResult.modifiedCount > 0) {
            productCache.remove(productId)
            true
        } else false
    }

    // Inventory operations
    suspend fun updateStock(productId: Int, newStock: Int): Boolean = withContext(Dispatchers.IO) {
        val updateResult = productCollection.updateOne(
            Filters.eq("basic.productId", productId),
            Updates.combine(
                Updates.set("basic.inventory.stock", newStock),
                Updates.set("basic.inventory.isAvailable", newStock > 0),
                Updates.set("lastUpdated", System.currentTimeMillis()),
                Updates.inc("version", 1)
            )
        )
        if (updateResult.modifiedCount > 0) {
            productCache.remove(productId)
            true
        } else false
    }

    // Pricing operations
    suspend fun updatePricing(productId: Int, pricing: PricingInfo): Boolean = withContext(Dispatchers.IO) {
        val updateResult = productCollection.updateOne(
            Filters.eq("basic.productId", productId),
            Updates.combine(
                Updates.set("basic.pricing", pricing),
                Updates.set("lastUpdated", System.currentTimeMillis()),
                Updates.inc("version", 1)
            )
        )
        if (updateResult.modifiedCount > 0) {
            productCache.remove(productId)
            true
        } else false
    }

    // Review operations
    suspend fun addReview(productId: Int, review: Review): Boolean = withContext(Dispatchers.IO) {
        val product = getProductWithCache(productId) ?: return@withContext false

        // Calculate new review summary
        val currentReviews = product.details.features.reviews
        val newTotalCount = currentReviews.summary.totalCount + 1
        val newDistribution = currentReviews.summary.distribution.toMutableMap()
        newDistribution[review.rating] = (newDistribution[review.rating] ?: 0) + 1

        val newAverageRating = (currentReviews.summary.averageRating * currentReviews.summary.totalCount + review.rating) / newTotalCount

        val newSummary = ReviewSummary(
            averageRating = newAverageRating,
            totalCount = newTotalCount,
            distribution = newDistribution
        )

        val updateResult = productCollection.updateOne(
            Filters.eq("basic.productId", productId),
            Updates.combine(
                Updates.push("details.features.reviews.items", review),
                Updates.set("details.features.reviews.summary", newSummary),
                Updates.set("lastUpdated", System.currentTimeMillis()),
                Updates.inc("version", 1)
            )
        )

        if (updateResult.modifiedCount > 0) {
            productCache.remove(productId)
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

        val products = withContext(Dispatchers.IO) {
            productCollection.find(combinedFilters)
                .skip(page * pageSize)
                .limit(pageSize)
                .toList()
        }

        products.forEach { emit(it) }
    }

    // Get products by type
    suspend fun getProductsByCategory(
        categoryId: Int,
        page: Int = 0,
        pageSize: Int = 20
    ): List<BasicProductInfo> {
        val products = withContext(Dispatchers.IO) {
            productCollection.find(Filters.eq("basic.categoryId", categoryId))
                .skip(page * pageSize)
                .limit(pageSize)
                .toList()
        }

       return products.map { it.basic }
    }

    // Get products on sale
    suspend fun getProductsOnSale(
        page: Int = 0,
        pageSize: Int = 20
    ): Flow<ProductModel> = flow {
        val products = withContext(Dispatchers.IO) {
            productCollection.find(
                Filters.and(
                    Filters.exists("basic.pricing.salePrice"),
                    Filters.exists("basic.pricing.discount")
                )
            )
                .skip(page * pageSize)
                .limit(pageSize)
                .toList()
        }

        products.forEach { emit(it) }
    }

    // Cleanup expired cache entries
    private suspend fun cleanupCache() = withContext(Dispatchers.IO) {
        val expiryTime = System.currentTimeMillis() - 5.minutes.inWholeMilliseconds
        productCache.entries.removeIf { it.value.timestamp < expiryTime }
    }
}