package com.example.plugins

import com.example.plugins.User.*
import com.mongodb.ErrorCategory
import com.mongodb.MongoCommandException
import com.mongodb.MongoWriteException
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import org.bson.Document
import org.bson.conversions.Bson
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import kotlin.reflect.full.memberProperties
lateinit var collection: MongoCollection<TelegramUser>


class TelegramUserService(private val database: MongoDatabase) {
    init {
        try {
            database.createCollection("telegramUsers")
        } catch (e: MongoCommandException) {
            // Collection already exists, ignore the error
        }
        collection = database.getCollection<TelegramUser>("telegramUsers")
    }

    /// this is useful when we just want to update some of the data and not the whole document,
    // this function takes (parameter) field and updates the data inside the field, (the data is also a parameter)
    suspend fun updateUserField(userId: String,field: String, subField: String, value: Any): Boolean = withContext(Dispatchers.IO) {
        var finalField="$field.$subField"
        val filter = Filters.eq("userInfo.userId", userId)
        val update = Updates.set(finalField, value)
        val result = collection.updateOne(filter, update)
        result.modifiedCount > 0
    }


///its specificly used for updating the value inside of an array instead of resetting with the new one
    suspend fun updateArrayField(userId: String, field: String, subField: String, value: Referral): Boolean = withContext(Dispatchers.IO) {
        // Define the field path to target the specific referral in the array
        val filter = Filters.eq("userInfo.userId", userId)

        // Create update operations using the positional `$` operator within `referrals`
        val update = Updates.combine(
            Updates.set("userInfo.referrals.$[ref].userId", value.userId),
            Updates.set("userInfo.referrals.$[ref].username", value.username),
            Updates.set("userInfo.referrals.$[ref].balance", value.balance),
            Updates.set("userInfo.referrals.$[ref].referrals", value.referrals)
        )

        // Use arrayFilters to apply the update to the correct array element
        val updateOptions = UpdateOptions().arrayFilters(listOf(Filters.eq("ref.userId", value.userId)))

        // Execute the update
        val result = collection.updateOne(filter, update, updateOptions)
        result.modifiedCount > 0
    }


    /// this is exact same as the updateUserField but instead of updating just a particular field it reads it instead
    // this function takes (parameter) field and reads the data inside the field, then returns the data
    suspend fun readUserField(userId: String, field: String): Any? = withContext(Dispatchers.IO) {
        val filter = Filters.eq("userInfo.userId", userId)
        val user = collection.find(filter).first()

        user?.let {
            val property = TelegramUser::class.memberProperties.find { it.name == field }
            property?.get(it)
        }
    }



///this is a function that is used to add referrals, the sole purpose of it is only to add referral
    suspend fun addReferral(userId: String, referral: Referral): Boolean = withContext(Dispatchers.IO) {
        val filter = Filters.eq("userInfo.userId", userId)
        val update: Bson = Updates.push("userInfo.referrals", referral)
        val result = collection.updateOne(filter, update)
        result.modifiedCount > 0
    }

    // Create new user also handles the referee
    suspend fun create(user: TelegramUser): TelegramUser = withContext(Dispatchers.IO) {
        try {
            // Check if a user with this userId already exists
            val existingUser = collection.find(Filters.eq("userInfo.userId", user.userInfo.userId)).first()
            if (existingUser != null) {
                throw IllegalArgumentException("User with userId ${user.userInfo.userId} already exists")
            }

            // If no existing user, proceed with insertion
            collection.insertOne(user)

            // Check if the user has a refereeId
            if (user.userInfo.refereeId != null && user.userInfo.refereeId != "0") {
                // Retrieve the referee's username
                val refereeUsername = collection
                    .find(Filters.eq("userInfo.userId", user.userInfo.refereeId))
                    .first()
                    ?.userInfo
                    ?.username

                // Update the user's refereeUsername field
                if (refereeUsername != null) {
                    updateUserField(user.userInfo.userId, "userInfo", "refereeUsername", refereeUsername)
                }

                val referral = Referral(
                    userId = user.userInfo.userId,
                    username = user.userInfo.username,
                    balance = 0,
                    referrals = +1//fix this/todo
                )

                //since you are being referred by a referee the referral value will go the referee.
                addReferral(user.userInfo.refereeId, referral)
                print("added referral to ${user.userInfo.refereeUsername}")
            }

            user
        } catch (e: MongoWriteException) {
            // This catch block handles the case where a duplicate key error occurs
            // due to a race condition between our check and insert
            if (e.error.category == ErrorCategory.DUPLICATE_KEY) {
                throw IllegalArgumentException("User with userId ${user.userInfo.userId} already exists")
            }
            throw e
        }
    }
    // Read a user by userId
    suspend fun readByUserId(userId: String): TelegramUser? = withContext(Dispatchers.IO) {
        collection.find(Filters.eq("userInfo.userId", userId)).firstOrNull()
    }

    // Read all users
    suspend fun readAllUsers(): List<TelegramUser> = withContext(Dispatchers.IO) {
        collection.find().toList()
    }


    ///
    suspend fun readTotalBalance(): Long = withContext(Dispatchers.IO) {
        collection.find()
            .mapNotNull { it.balanceInfo?.totalBalance } // Safely extract non-null balances
            .sumOf { it.toLong() } // Sum and convert to Long
    }


    // Update a user by userId
    suspend fun updateByUserId(userId: String, user: TelegramUser): TelegramUser? = withContext(Dispatchers.IO) {
        collection.findOneAndReplace(Filters.eq("userInfo.userId", userId), user)
    }
    // Delete a user by userId
    suspend fun deleteByUserId(userId: String): TelegramUser? = withContext(Dispatchers.IO) {
        collection.findOneAndDelete(Filters.eq("userInfo.userId", userId))
    }


    suspend fun userExists(userId: String): Boolean = withContext(Dispatchers.IO) {
        collection.find(Filters.eq("userInfo.userId", userId))
            .limit(1) // Limits the result to just one document
            .iterator()
            .hasNext() // Check if a result exists
    }


    suspend fun updateUserLevel(userId: String, newLevel: Level): Boolean = withContext(Dispatchers.IO) {
        val filter = Filters.eq("userId", userId)
        val update: Bson = Updates.combine(
            Updates.set("level.id", newLevel.id),
            Updates.set("level.name", newLevel.name),
            Updates.set("level.imgUrl", newLevel.imgUrl)
        )
        val result = collection.updateOne(filter, update)
        result.modifiedCount > 0
    }
}
