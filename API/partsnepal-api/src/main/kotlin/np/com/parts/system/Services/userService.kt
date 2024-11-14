package np.com.parts.system.Services

import com.mongodb.ErrorCategory
import com.mongodb.MongoCommandException
import com.mongodb.MongoWriteException
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import np.com.parts.system.Models.FullUserDetails
import np.com.parts.system.Models.OrderModel
import org.litote.kmongo.getCollection

class UserService(private val database: MongoDatabase) {
    private lateinit var collection: MongoCollection<FullUserDetails>

    init {
        try {
            database.createCollection("users")
        } catch (e: MongoCommandException) {
            // Collection already exists, ignore the error
        }
        collection = database.getCollection<FullUserDetails>("users")

        // Create indexes for better query performance
        collection.createIndex(Indexes.ascending("user.userId"))
        collection.createIndex(Indexes.ascending("user.email"))
        collection.createIndex(Indexes.text("user.username"))
        collection.createIndex(Indexes.ascending("accountStatus"))
    }

    // Create a new user
    suspend fun createUser(user: FullUserDetails): Boolean = withContext(Dispatchers.IO) {
        try {
            collection.insertOne(user)
            true
        } catch (e: MongoWriteException) {
            if (e.error.category == ErrorCategory.DUPLICATE_KEY) {
                false // system already exists
            } else {
                throw e // Rethrow other errors
            }
        }
    }

    // Read a user by userId
    suspend fun getUserById(userId: Int): FullUserDetails? = withContext(Dispatchers.IO) {
        collection.find(Filters.eq("user.userId", userId)).firstOrNull()
    }

    // Read a user by email
    suspend fun getUserByEmail(email: String): FullUserDetails? = withContext(Dispatchers.IO) {
        collection.find(Filters.eq("user.email", email)).firstOrNull()
    }

    // Read all users with optional pagination
    suspend fun getAllUsers(skip: Int = 0, limit: Int = 50): List<FullUserDetails> = withContext(Dispatchers.IO) {
        collection.find()
            .skip(skip)
            .limit(limit)
            .toList()
    }

    // Search users by username
    suspend fun searchUsers(query: String): List<FullUserDetails> = withContext(Dispatchers.IO) {
        collection.find(Filters.text(query)).toList()
    }

    // Update specific user fields
    suspend fun updateUserFields(userId: Int, updates: Map<String, Any>): Boolean = withContext(Dispatchers.IO) {
        val updateOperations = updates.map { (field, value) ->
            Updates.set(field, value)
        }
        val combinedUpdate = Updates.combine(updateOperations)
        val result = collection.updateOne(
            Filters.eq("user.userId", userId),
            combinedUpdate
        )
        result.modifiedCount > 0
    }

    // Add order to user's history
    suspend fun addOrder(userId: Int, order: OrderModel): Boolean = withContext(Dispatchers.IO) {
        val result = collection.updateOne(
            Filters.eq("user.userId", userId),
            Updates.combine(
                Updates.push("orders", order),
                Updates.inc("totalOrders", 1)
            )
        )
        result.modifiedCount > 0
    }

    // Add review to user's history
    suspend fun addReview(userId: Int, review: String): Boolean = withContext(Dispatchers.IO) {
        val result = collection.updateOne(
            Filters.eq("user.userId", userId),
            Updates.combine(
                Updates.push("reviewHistory", review),
                Updates.inc("totalReviews", 1)
            )
        )
        result.modifiedCount > 0
    }

    // Update user engagement score
    suspend fun updateEngagementScore(userId: Int, score: Int): Boolean = withContext(Dispatchers.IO) {
        val result = collection.updateOne(
            Filters.eq("user.userId", userId),
            Updates.set("engagementScore", score)
        )
        result.modifiedCount > 0
    }

    // Update account status
    suspend fun updateAccountStatus(userId: Int, status: String): Boolean = withContext(Dispatchers.IO) {
        val result = collection.updateOne(
            Filters.eq("user.userId", userId),
            Updates.set("accountStatus", status)
        )
        result.modifiedCount > 0
    }

    // Delete a user
    suspend fun deleteUser(userId: Int): Boolean = withContext(Dispatchers.IO) {
        val result = collection.deleteOne(Filters.eq("user.userId", userId))
        result.deletedCount > 0
    }

    // Get users by account status
    suspend fun getUsersByStatus(status: String): List<FullUserDetails> = withContext(Dispatchers.IO) {
        collection.find(Filters.eq("accountStatus", status)).toList()
    }

    // Get business accounts
    suspend fun getBusinessAccounts(): List<FullUserDetails> = withContext(Dispatchers.IO) {
        collection.find(Filters.eq("user.isBusinessAccount", true)).toList()
    }

    // Update user preferences
    suspend fun updatePreferences(userId: Int, preferences: Map<String, String>): Boolean = withContext(Dispatchers.IO) {
        val result = collection.updateOne(
            Filters.eq("user.userId", userId),
            Updates.set("preferences", preferences)
        )
        result.modifiedCount > 0
    }

    // Get users by engagement score range
    suspend fun getUsersByEngagementScore(minScore: Int, maxScore: Int): List<FullUserDetails> = withContext(Dispatchers.IO) {
        collection.find(
            Filters.and(
                Filters.gte("engagementScore", minScore),
                Filters.lte("engagementScore", maxScore)
            )
        ).toList()
    }
}