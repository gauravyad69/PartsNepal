package np.com.parts.Domain.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import np.com.parts.API.Models.UpdateProfileRequest
import np.com.parts.API.Models.UserModel
import np.com.parts.API.Models.UserPreferences
import np.com.parts.API.Repository.UserRepository
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(private val userRepository: UserRepository) : ViewModel() {



    private val _userProfile = MutableStateFlow<UserModel?>(null)
    val userProfile: StateFlow<UserModel?> = _userProfile.asStateFlow()

    private val _profileUpdateState = MutableStateFlow<ProfileUpdateState>(ProfileUpdateState.Idle)
    val profileUpdateState = _profileUpdateState.asStateFlow()

    private val _preferencesUpdateState = MutableStateFlow<PreferencesUpdateState>(
        PreferencesUpdateState.Idle
    )
    val preferencesUpdateState = _preferencesUpdateState.asStateFlow()

    sealed class ProfileUpdateState {
        object Idle : ProfileUpdateState()
        object Loading : ProfileUpdateState()
        data class Success(val message: String = "Profile updated successfully") : ProfileUpdateState()
        data class Error(val message: String) : ProfileUpdateState()
    }

    sealed class PreferencesUpdateState {
        object Idle : PreferencesUpdateState()
        object Loading : PreferencesUpdateState()
        data class Success(val message: String = "Preferences updated successfully") : PreferencesUpdateState()
        data class Error(val message: String) : PreferencesUpdateState()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            try {
                userRepository.getUserProfile()
                    .onSuccess { user ->
                        _userProfile.value = user
                    }
                    .onFailure { exception ->
                        _profileUpdateState.value = ProfileUpdateState.Error(
                            exception.message ?: "Failed to load profile"
                        )
                    }
            } catch (e: Exception) {
                Timber.e(e, "Error loading user profile")
                _profileUpdateState.value = ProfileUpdateState.Error(
                    e.message ?: "Failed to load profile"
                )
            }
        }
    }

    fun updateProfile(request: UpdateProfileRequest) {
        viewModelScope.launch {
            _profileUpdateState.value = ProfileUpdateState.Loading

            try {
                userRepository.updateProfile(request)
                    .onSuccess { success ->
                        if (success) {
                            loadUserProfile() // Reload profile after update
                            _profileUpdateState.value = ProfileUpdateState.Success()
                        } else {
                            _profileUpdateState.value = ProfileUpdateState.Error(
                                "Failed to update profile"
                            )
                        }
                    }
                    .onFailure { exception ->
                        Timber.i(exception, "Profile update error")
                        _profileUpdateState.value = ProfileUpdateState.Error(
                            exception.message ?: "Failed to update profile"
                        )
                    }
            } catch (e: Exception) {
                Timber.e(e, "Profile update error")
                _profileUpdateState.value = ProfileUpdateState.Error(
                    e.message ?: "An unexpected error occurred"
                )
            }
        }
    }

    fun updatePreferences(preferences: UserPreferences) {
        viewModelScope.launch {
            _preferencesUpdateState.value = PreferencesUpdateState.Loading

            try {
                userRepository.updatePreferences(preferences)
                    .onSuccess { success ->
                        if (success) {
                            loadUserProfile() // Reload profile after update
                            _preferencesUpdateState.value = PreferencesUpdateState.Success()
                        } else {
                            _preferencesUpdateState.value = PreferencesUpdateState.Error(
                                "Failed to update preferences"
                            )
                        }
                    }
                    .onFailure { exception ->
                        Timber.e(exception, "Preferences update error")
                        _preferencesUpdateState.value = PreferencesUpdateState.Error(
                            exception.message ?: "Failed to update preferences"
                        )
                    }
            } catch (e: Exception) {
                Timber.e(e, "Preferences update error")
                _preferencesUpdateState.value = PreferencesUpdateState.Error(
                    e.message ?: "An unexpected error occurred"
                )
            }
        }
    }

    fun resetUpdateStates() {
        _profileUpdateState.value = ProfileUpdateState.Idle
        _preferencesUpdateState.value = PreferencesUpdateState.Idle
    }
}