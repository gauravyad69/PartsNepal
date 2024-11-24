package np.com.parts.system.Models

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorResponse? = null
)

@Serializable
data class ErrorResponse(
    val message: String,
    val code: String? = null,
    val details: Map<String, String> = emptyMap()
)

@Serializable
data class ValidationError(
    val field: String,
    val message: String
) 