package np.com.parts.Screens.StartingScreens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import io.ktor.client.HttpClient
import kotlinx.coroutines.launch
import np.com.parts.API.Auth.AuthError
import np.com.parts.API.Repository.AuthRepository
import np.com.parts.API.Models.AccountType
import np.com.parts.R
import np.com.parts.app_utils.RandomTextGenerator
import np.com.parts.databinding.FragmentRegisterBinding
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    @Inject
    lateinit var client: HttpClient

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.registerButton.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val phoneNumber = binding.phoneInput.text.toString()
        val password = binding.passwordInput.text.toString()
        val confirmPassword = binding.confirmPasswordInput.text.toString()

        // Validation
        if (phoneNumber.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Please fill all fields")
            return
        }

        if (password != confirmPassword) {
            showError("Passwords do not match")
            return
        }



        lifecycleScope.launch {
            try {
                // Show loading
//                showLoading(true)

                when (val result = authRepository.register(
                    email = null, // Since this is phone-based registration
                    phoneNumber = phoneNumber,
                    password = password,
                    firstName = "PleaseChange ${RandomTextGenerator.generate(3)}",
                    lastName = "PleaseChange",
                    username = "PleaseChange${RandomTextGenerator.generate(5)}",
                    accountType = AccountType.PERSONAL
                )) {
                    is AuthRepository.AuthResult.Success -> {
//                        showLoading(false)
                        // Navigate to additional info fragment
                        findNavController().navigate(
                            R.id.action_registerFragment_to_otherFragment
                        )
                    }

                    is AuthRepository.AuthResult.Error -> {
//                        showLoading(false)
                        when (result.error) {
                            AuthError.DUPLICATE_USER -> {
                                showError("Phone number is already registered")
                            }
                            AuthError.NETWORK_ERROR -> {
                                showError("Network error. Please check your connection")
                            }
                            AuthError.INVALID_REQUEST -> {
                                showError("Invalid registration details")
                            }
                            else -> {
                                showError(result.message)
                                Timber.e("Registration error: ${result.message}")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
//                showLoading(false)
                showError("An unexpected error occurred")
                Timber.e(e, "Registration error $e")
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }



//    // Helper functions
//    private fun showLoading(isLoading: Boolean) {
//        binding.apply {
//            progressBar.isVisible = isLoading
//            registerButton.isEnabled = !isLoading
//            // Optionally disable input fields during loading
//            phoneNumberInput.isEnabled = !isLoading
//            passwordInput.isEnabled = !isLoading
//        }
//    }

//    private fun showError(message: String) {
//        binding.errorTextView.apply {
//            text = message
//            isVisible = true
//            // Optional: Add animation
//            alpha = 0f
//            animate()
//                .alpha(1f)
//                .setDuration(300)
//                .start()
//        }

        // Hide error after delay
//        lifecycleScope.launch {
//            delay(3000)
//            binding.errorTextView.animate()
//                .alpha(0f)
//                .setDuration(300)
//                .withEndAction {
//                    binding.errorTextView.isVisible = false
//                }
//                .start()
//        }
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
