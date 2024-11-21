import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import np.com.parts.API.Models.User
import np.com.parts.API.Models.UserPreferences
import np.com.parts.API.Repository.UserRepository

class UserProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadUserProfile() {
        viewModelScope.launch {
            userRepository.getUserProfile()
                .onSuccess { user ->
                    _userProfile.value = user
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
        }
    }

    fun updateProfile(updates: Map<String, Any>) {
        viewModelScope.launch {
            userRepository.updateProfile(updates)
                .onSuccess { success ->
                    if (success) {
                        loadUserProfile() // Reload profile after update
                    } else {
                        _error.value = "Failed to update profile"
                    }
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
        }
    }

    fun updatePreferences(preferences: UserPreferences) {
        viewModelScope.launch {
            userRepository.updatePreferences(preferences)
                .onSuccess { success ->
                    if (success) {
                        loadUserProfile() // Reload profile after update
                    } else {
                        _error.value = "Failed to update preferences"
                    }
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
        }
    }
}