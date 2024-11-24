package np.com.parts.database

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import np.com.parts.API.Models.Money

class Converters {
    @TypeConverter
    fun fromMoney(money: Money): String {
        return Json.encodeToString(money)
    }

    @TypeConverter
    fun toMoney(value: String): Money {
        return Json.decodeFromString(value)
    }
} 