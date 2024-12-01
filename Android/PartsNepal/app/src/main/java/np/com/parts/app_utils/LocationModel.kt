package np.com.parts.app_utils

import kotlinx.serialization.Serializable

data class Province(
    val id: Int,
    val name: String,
    val districtList: List<District>
)

data class District(
    val id: Int,
    val name: String,
    val municipalityList: List<Municipality>
)

data class Municipality(
    val id: Int,
    val name: String
)