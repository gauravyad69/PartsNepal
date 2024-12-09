package np.com.parts.API.Models
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
interface BaseModel {
    val id: String
    val lastUpdated: Long
    val version: Int
}


