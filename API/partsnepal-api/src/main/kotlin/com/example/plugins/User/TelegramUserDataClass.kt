package com.example.plugins.User
import kotlinx.serialization.Serializable

@Serializable
data class TelegramUser(
    val userInfo: UserInfo,
    val balanceInfo: BalanceInfo,
    val upgradesInfo: UpgradesInfo,
    val taskInfo: TaskInfo,
)

@Serializable
data class UserInfo(
    val userId: String,
    val username: String,
    val firstName: String,
    val lastName: String? = null,
    val isPremium: Boolean,
    val profilePicture: String?,
    val refereeId: String? = null,
    val refereeUsername: String? = null,
    val referrals: List<Referral> = emptyList()
)

@Serializable
data class BalanceInfo(
    val totalBalance: Int = 0,
    val balance: Int = 0,
    val tapBalance: Int = 0,
    val refBonus: Int = 0,
    val energy: Int = 500
)





@Serializable
data class TaskInfo(
    val tasksCompleted: List<String> = emptyList(),
    val manualTasks: List<String> = emptyList()
)

@Serializable
data class UpgradesInfo(
    val freeGuru: Int = 3,
    val fullTank: Int = 3,
    val timeSta: Long? = null,
    val timeStaTank: Long? = null,
    val tapValue: TapValue = TapValue(),
    val timeRefill: TimeRefill = TimeRefill(),
    val level: Level = Level(),
    val battery: Battery = Battery()
)
@Serializable
data class TapValue(
    val level: Int = 1,
    val value: Int = 1
)

@Serializable
data class TimeRefill(
    val level: Int = 1,
    val duration: Int = 10,
    val step: Int = 600
)

@Serializable
data class Level(
    val id: Int = 1,
    val name: String = "Bronze",
    val imgUrl: String = "/bronze.webp"
)

@Serializable
data class Battery(
    val level: Int = 1,
    val energy: Int = 500
)

@Serializable
data class Referral(
    val userId: String,
    val username: String,
    val balance: Int,
    val referrals: Int
)