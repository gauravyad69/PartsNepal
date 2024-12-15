package np.com.parts.Screens.NavScreens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import np.com.parts.API.Models.AccountType
import np.com.parts.API.Models.UpdateProfileRequest
import np.com.parts.API.Repository.UserRepository
import np.com.parts.R
import np.com.parts.databinding.FragmentOtherBinding
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class OtherFragment : Fragment() {
    private var _binding: FragmentOtherBinding? = null
    private val binding get() = _binding!!
    
    @Inject
    lateinit var userRepository: UserRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOtherBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAccountTypeDropdown()
        setupUpdateButton()
    }

    private fun setupAccountTypeDropdown() {
        val accountTypes = AccountType.values().map { it.name.capitalize() }
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, accountTypes)
        binding.accountTypeDropdown.setAdapter(arrayAdapter)
    }

    private fun setupUpdateButton() {
        binding.loginButton.setOnClickListener {
            if (validateInputs()) {
                updateUserProfile()
            }
        }
    }

    private fun validateInputs(): Boolean {
        val firstName = binding.firstNameInput.text.toString()
        val lastName = binding.lastNameInput.text.toString()
        val email = binding.emailInput.text.toString()
        val accountType = binding.accountTypeDropdown.text.toString()

        when {
            firstName.isEmpty() -> {
                showError("First name is required")
                binding.firstNameInput.requestFocus()
                return false
            }
            lastName.isEmpty() -> {
                showError("Last name is required")
                binding.lastNameInput.requestFocus()
                return false
            }
            email.isEmpty() -> {
                showError("Email is required")
                binding.emailInput.requestFocus()
                return false
            }
            !isValidEmail(email) -> {
                showError("Please enter a valid email")
                binding.emailInput.requestFocus()
                return false
            }
            accountType.isEmpty() -> {
                showError("Please select an account type")
                binding.accountTypeDropdown.requestFocus()
                return false
            }
        }
        return true
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }



    private fun updateUserProfile() {
        binding.progressBar.isVisible = true
        binding.loginButton.isEnabled = false

        val request = UpdateProfileRequest(
            firstName = binding.firstNameInput.text?.toString()?.takeIf { it.isNotBlank() },
            lastName = binding.lastNameInput.text?.toString()?.takeIf { it.isNotBlank() },
            email = binding.emailInput.text?.toString()?.takeIf { it.isNotBlank() },
            accountType = try {
                binding.accountTypeDropdown.text?.toString()?.uppercase()?.let {
                    AccountType.valueOf(it)
                }
            } catch (e: IllegalArgumentException) {
                Timber.e(e, "Invalid account type")
                null
            }
        )

        // Validate inputs
        if (request.firstName.isNullOrBlank() || request.lastName.isNullOrBlank()) {
            showError("First name and last name are required")
            binding.loginButton.isEnabled = true
            return
        }

        lifecycleScope.launch {
            try {
                userRepository.updateProfile(request)
                    .onSuccess { success ->
                        if (success) {
                            // Show success message before navigation
                            Snackbar.make(
                                binding.root,
                                "Profile updated successfully",
                                Snackbar.LENGTH_SHORT
                            ).show()

                            // Short delay to show the success message
                            delay(500)


                            // Navigate to home
//                            findNavController().navigate(
//                                R.id.action_otherFragment_to_homeFragment)
                            requireActivity().finish() // Close the current activity
                            requireActivity().startActivity(requireActivity().intent) // Start the activity again
                        } else {
                            showError("Failed to update profile")
                        }
                    }
                    .onFailure { exception ->
                        val errorMessage = when (exception) {
                            is IOException -> "Network error. Please check your connection."
                            else -> exception.message ?: "An unexpected error occurred"
                        }
                        showError(errorMessage)
                    }
            } catch (e: Exception) {
                Timber.e(e, "Profile update error")
                showError("An unexpected error occurred")
            } finally {
                binding.progressBar.isVisible = false
                binding.loginButton.isEnabled = true
            }
        }
    }

    private fun showError(message: String) {
        binding.errorText.apply {
            text = message
            isVisible = true

            // Auto-hide error after delay
            lifecycleScope.launch {
                delay(3000)
                isVisible = false
            }
        }
    }


//    private fun showError(message: String) {
//        binding.errorText.apply {
//            text = message
//            isVisible = true
//            // Optional: Hide error after delay
//            lifecycleScope.launch {
//                delay(3000)
//                isVisible = false
//            }
//        }
//    }

//    private fun showError(message: String) {
//        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}