package np.com.parts.system.Services

import com.mongodb.ErrorCategory
import com.mongodb.MongoCommandException
import com.mongodb.MongoWriteException
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import np.com.parts.system.Models.*
import org.litote.kmongo.getCollection
import java.security.MessageDigest
import java.util.*

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
        try {
            collection.createIndexes(
                listOf(
                    IndexModel(Indexes.ascending("user.userId"), IndexOptions().unique(true)),
                    IndexModel(Indexes.ascending("user.email.value"), IndexOptions().unique(true)),
                    IndexModel(Indexes.ascending("user.phoneNumber.value"), IndexOptions().unique(true)),
                    IndexModel(Indexes.text("user.username")),
                    IndexModel(Indexes.ascending("accountStatus")),
                    IndexModel(Indexes.ascending("user.accountType")),
                    IndexModel(Indexes.ascending("lastModifiedAt")),
                    IndexModel(Indexes.ascending("engagement.lastActive"))
                )
            )
        } catch (e: MongoCommandException) {
            // Indexes might already exist
        }
    }

    // Authentication related methods
    suspend fun loginWithPhoneAndPassword(phoneNumber: PhoneNumber, password: String): FullUserDetails? =
        withContext(Dispatchers.IO) {
            val user = getUserByPhone(phoneNumber)
            user?.let {
                if (it.credentials.hashedPassword == hashPassword(password)) {
                    // Update last active and login history
                    updateLoginActivity(it.user.userId)
                    it
                } else null
            }
        }

    suspend fun loginWithGoogle(email: Email): FullUserDetails? = withContext(Dispatchers.IO) {
        getUserByEmail(email)?.also { user ->
            updateLoginActivity(user.user.userId)
        }
    }

    suspend fun updateLoginActivity(userId: UserId) {
        val currentTime = System.currentTimeMillis()
        collection.updateOne(
            Filters.eq("user.userId", userId),
            Updates.combine(
                Updates.set("engagement.lastActive", currentTime),
                Updates.push("engagement.loginHistory", currentTime)
            )
        )
    }

    fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return Base64.getEncoder().encodeToString(digest)
    }

    // Create operations
    suspend fun createUser(user: FullUserDetails): Boolean = withContext(Dispatchers.IO) {
        try {
            collection.insertOne(user)
            true
        } catch (e: MongoWriteException) {
            when (e.error.category) {
                ErrorCategory.DUPLICATE_KEY -> false
                else -> throw e
            }
        }
    }

    // Read operations
    suspend fun getUserById(userId: UserId): FullUserDetails? = withContext(Dispatchers.IO) {
        collection.find(Filters.eq("user.userId", userId)).firstOrNull()
    }

    suspend fun getUserByEmail(email: Email): FullUserDetails? = withContext(Dispatchers.IO) {
        collection.find(Filters.eq("user.email.value", email.value)).firstOrNull()
    }

    suspend fun getUserByPhone(phoneNumber: PhoneNumber): FullUserDetails? = withContext(Dispatchers.IO) {
        collection.find(Filters.eq("user.phoneNumber.value", phoneNumber.value)).firstOrNull()
    }

    suspend fun getAllUsers(
        skip: Int = 0,
        limit: Int = 50,
        sortBy: String = "lastModifiedAt",
        ascending: Boolean = false
    ): List<FullUserDetails> = withContext(Dispatchers.IO) {
        collection.find()
            .sort(if (ascending) Sorts.ascending(sortBy) else Sorts.descending(sortBy))
            .skip(skip)
            .limit(limit)
            .toList()
    }

    // Update operations
    suspend fun updateUser(userId: UserId, updates: Map<String, Any>): Boolean = withContext(Dispatchers.IO) {
        val updateOperations = updates.map { (field, value) ->
            Updates.set(field, value)
        }
        val combinedUpdate = Updates.combine(
            Updates.combine(updateOperations),
            Updates.set("lastModifiedAt", System.currentTimeMillis())
        )
        val result = collection.updateOne(
            Filters.eq("user.userId", userId),
            combinedUpdate
        )
        result.modifiedCount > 0
    }

    suspend fun updateAccountStatus(userId: UserId, status: AccountStatus): Boolean = withContext(Dispatchers.IO) {
        val result = collection.updateOne(
            Filters.eq("user.userId", userId),
            Updates.combine(
                Updates.set("accountStatus", status),
                Updates.set("lastModifiedAt", System.currentTimeMillis())
            )
        )
        result.modifiedCount > 0
    }

    suspend fun updatePreferences(userId: UserId, preferences: UserPreferences): Boolean = withContext(Dispatchers.IO) {
        val result = collection.updateOne(
            Filters.eq("user.userId", userId),
            Updates.combine(
                Updates.set("preferences", preferences),
                Updates.set("lastModifiedAt", System.currentTimeMillis())
            )
        )
        result.modifiedCount > 0
    }

    // Order and Review operations
    suspend fun addOrder(userId: UserId, order: OrderRef): Boolean = withContext(Dispatchers.IO) {
        val result = collection.updateOne(
            Filters.eq("user.userId", userId),
            Updates.combine(
                Updates.push("orders.orderHistory", order),
                Updates.inc("orders.totalOrders", 1),
                Updates.inc("orders.totalSpent", order.amount),
                Updates.set("lastModifiedAt", System.currentTimeMillis())
            )
        )
        result.modifiedCount > 0
    }

    suspend fun addReview(userId: UserId, review: ReviewRef): Boolean = withContext(Dispatchers.IO) {
        val result = collection.updateOne(
            Filters.eq("user.userId", userId),
            Updates.combine(
                Updates.push("reviews.reviewHistory", review),
                Updates.inc("reviews.totalReviews", 1),
                Updates.set("lastModifiedAt", System.currentTimeMillis())
            )
        )
        result.modifiedCount > 0
    }

    // Query operations
    suspend fun getUsersByStatus(status: AccountStatus): List<FullUserDetails> = withContext(Dispatchers.IO) {
        collection.find(Filters.eq("accountStatus", status)).toList()
    }

    suspend fun getUsersByAccountType(accountType: AccountType): List<FullUserDetails> = withContext(Dispatchers.IO) {
        collection.find(Filters.eq("user.accountType", accountType)).toList()
    }

    suspend fun searchUsers(query: String): List<FullUserDetails> = withContext(Dispatchers.IO) {
        collection.find(Filters.text(query)).toList()
    }

    suspend fun getUsersByEngagementScore(minScore: Int, maxScore: Int): List<FullUserDetails> =
        withContext(Dispatchers.IO) {
            collection.find(
                Filters.and(
                    Filters.gte("engagement.engagementScore", minScore),
                    Filters.lte("engagement.engagementScore", maxScore)
                )
            ).toList()
        }

    suspend fun getInactiveUsers(inactiveThresholdMs: Long): List<FullUserDetails> = withContext(Dispatchers.IO) {
        val thresholdTime = System.currentTimeMillis() - inactiveThresholdMs
        collection.find(Filters.lt("engagement.lastActive", thresholdTime)).toList()
    }

    // Delete operation
    suspend fun deleteUser(userId: UserId): Boolean = withContext(Dispatchers.IO) {
        val result = collection.deleteOne(Filters.eq("user.userId", userId))
        result.deletedCount > 0
    }
}