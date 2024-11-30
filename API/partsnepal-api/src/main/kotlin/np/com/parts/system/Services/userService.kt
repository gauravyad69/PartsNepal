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
import org.bson.conversions.Bson
import org.litote.kmongo.getCollection
import java.security.MessageDigest
import java.util.*

class UserService(private val database: MongoDatabase) {
    private val collection: MongoCollection<FullUserDetails>

    init {
        collection = database.getCollection<FullUserDetails>("users")
        recreateIndexes()
    }

    private fun recreateIndexes() {
        try {
            // First, drop all existing indexes
            collection.dropIndexes()
            // Create fresh indexes with correct paths
            collection.createIndexes(
                listOf(
                    IndexModel(
                        Indexes.ascending("user.userId"),
                        IndexOptions().unique(true).name("userId_unique")
                    ),
                    IndexModel(
                        Indexes.ascending("user.phoneNumber"),
                        IndexOptions().unique(true).name("phone_unique")
                    ),
                    IndexModel(
                        Indexes.ascending("user.username"),
                        IndexOptions().unique(true).name("username_unique")
                    )
                )
            )
            println("Successfully recreated all indexes")
        } catch (e: Exception) {
            println("Error recreating indexes: ${e.message}")
            throw e
        }
    }

    suspend fun getUserByUsername(username: String): FullUserDetails? = withContext(Dispatchers.IO) {
        collection.find(Filters.eq("user.username", username)).firstOrNull()
    }

    suspend fun createUser(user: FullUserDetails): Boolean = withContext(Dispatchers.IO) {
        try {
            // Find the highest userId
            val lastUser = collection.find()
                .sort(Sorts.descending("user.userId"))
                .limit(1)
                .firstOrNull()

            val newId = (lastUser?.user?.userId?.value ?: 0) + 1
            println("Generated new user ID: $newId")

            // Create updated user with new ID BEFORE checking duplicates
            val updatedUser = user.copy(
                user = user.user.copy(
                    userId = UserId(newId)
                )
            )

            // Check for duplicates after setting the ID
            val duplicateChecks = mutableListOf<Bson>(
                Filters.eq("user.phoneNumber", updatedUser.user.phoneNumber),
                Filters.eq("user.username", updatedUser.user.username)
            )
            
            updatedUser.user.email?.let { email ->
                duplicateChecks.add(Filters.eq("user.email", email))
            }

            val existingUser = collection.find(Filters.or(duplicateChecks)).firstOrNull()
            if (existingUser != null) {
                println("Duplicate user found with username: ${existingUser.user.username}")
                return@withContext false
            }

            collection.insertOne(updatedUser)
            println("User inserted successfully with ID: $newId")
            true
        } catch (e: MongoWriteException) {
            println("MongoDB Write Error: ${e.message}")
            false
        } catch (e: Exception) {
            println("Unexpected error during user creation: ${e.message}")
            throw e
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

    // Read operations
    suspend fun getUserById(userId: UserId): UserModel? = withContext(Dispatchers.IO) {
        println("Searching for user with ID: ${userId.value}")  // Debug log
        val result = collection.find(Filters.eq("user.userId", userId.value)).firstOrNull()
        println("Database result: $result")  // Debug log
        result?.user  // Return just the UserModel part
    }

    suspend fun getUserPhoneNumberById(userId: UserId): PhoneNumber? = withContext(Dispatchers.IO) {
        println("Searching for user with ID: ${userId.value}")  // Debug log
        val result = collection.find(Filters.eq("user.userId", userId.value)).firstOrNull()
        println("Database result: $result")  // Debug log
        result?.user?.phoneNumber  // Return just the phone number part
    }


    suspend fun getUserByEmail(email: Email?): FullUserDetails? = withContext(Dispatchers.IO) {
        email?.let {
            collection.find(Filters.eq("user.email", email)).firstOrNull()
        }
    }

    suspend fun getUserByPhone(phoneNumber: PhoneNumber?): FullUserDetails? = withContext(Dispatchers.IO) {
        phoneNumber?.let {
            collection.find(Filters.eq("user.phoneNumber", phoneNumber)).firstOrNull()
        }
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

    suspend fun updateProfile(userId: UserId, update: UpdateProfileRequest): Boolean = withContext(Dispatchers.IO) {
        try {
            val updates = mutableListOf<Bson>()
            
            // Add non-null fields to updates
            update.firstName?.let { updates.add(Updates.set("user.firstName", it)) }
            update.lastName?.let { updates.add(Updates.set("user.lastName", it)) }
            update.username?.let {
                // Check if username is already taken by another user
                val existingUser = getUserByUsername(it)
                if (existingUser != null && existingUser.user.userId != userId) {
                    return@withContext false
                }
                updates.add(Updates.set("user.username", it))
            }
            
            // Handle email update with validation
            update.email?.let { emailStr ->
                val email = Email(emailStr)
                val existingUser = getUserByEmail(email)
                if (existingUser != null && existingUser.user.userId != userId) {
                    return@withContext false
                }
                updates.add(Updates.set("user.email", email))
            }
            
            // Handle phone update with validation
            update.phoneNumber?.let { phoneStr ->
                val phone = PhoneNumber(phoneStr)
                val existingUser = getUserByPhone(phone)
                if (existingUser != null && existingUser.user.userId != userId) {
                    return@withContext false
                }
                updates.add(Updates.set("user.phoneNumber", phone))
            }
            
            // Add last modified timestamp
            updates.add(Updates.set("lastModifiedAt", System.currentTimeMillis()))
            
            if (updates.isEmpty()) {
                return@withContext true
            }

            val result = collection.updateOne(
                Filters.eq("user.userId", userId),
                Updates.combine(updates)
            )
            
            result.modifiedCount > 0
        } catch (e: Exception) {
            println("Error updating profile: ${e.message}")
            false
        }
    }
}