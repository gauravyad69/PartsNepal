package np.com.parts.system.Models
import kotlinx.serialization.Serializable

@Serializable
sealed class RegistrationError {
    @Serializable
    object DuplicatePhone : RegistrationError()
    
    @Serializable
    object DuplicateEmail : RegistrationError()
    
    @Serializable
    object DuplicateUsername : RegistrationError()
    
    @Serializable
    object InvalidInput : RegistrationError()
    
    @Serializable
    data class UnexpectedError(val message: String) : RegistrationError()
}

@Serializable
data class ErrorResponse(
    val message: String,
    val code: String,
    val details: Map<String, String> = emptyMap()
) 