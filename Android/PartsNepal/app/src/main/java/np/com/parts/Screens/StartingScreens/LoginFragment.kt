package np.com.parts.Screens.StartingScreens


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import np.com.parts.API.Auth.AuthError
import np.com.parts.API.Repository.AuthRepository
import np.com.parts.API.TokenManager
import np.com.parts.R
import np.com.parts.databinding.FragmentLoginBinding
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment() {


    @Inject
    lateinit var tokenManager: TokenManager

    @Inject
    lateinit var authRepository: AuthRepository

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.loginButton.setOnClickListener {
            loginUser()
        }

        binding.forgotPasswordText.setOnClickListener {
            // Navigate to forgot password
        }

        binding.registerText.setOnClickListener{
            val action = LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
            findNavController().navigate(action)
        }
    }

    private fun loginUser() {
        val identifier = binding.emailPhoneInput.text.toString()
        val password = binding.passwordInput.text.toString()

        if (identifier.isEmpty() || password.isEmpty()) {
            showError("Please fill all fields")
            return
        }

        // Determine if it's a phone login based on input format
        val isEmailLogin = identifier.all { it.isLetter() }
        val isPhoneLogin=!isEmailLogin

        lifecycleScope.launch {
            try {
                when (val result = authRepository.login(identifier, password, isPhoneLogin)) {
                    is AuthRepository.AuthResult.Success -> {
                        // Login successful
                        Toast.makeText(requireContext(), "Login Successful!", Toast.LENGTH_SHORT).show()
                        requireActivity().finish() // Close the current activity
                        requireActivity().startActivity(requireActivity().intent)
//                        NavHostFragment.findNavController(this@LoginFragment)
//                            .navigate(R.id.action_loginFragment_to_homeFragment)
                    }
                    is AuthRepository.AuthResult.Error -> {
                        // Handle specific error cases
                        when (result.error) {
                            AuthError.INVALID_CREDENTIALS -> {
                                showError("Invalid email/phone or password")
                            }
                            AuthError.ACCOUNT_INACTIVE -> {
                                showError("Your account is inactive. Please contact support.")
                            }
                            AuthError.NETWORK_ERROR -> {
                                showError("Network error. Please check your connection.")
                            }
                            else -> {
                                showError(result.message)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                showError("An unexpected error occurred")
                Timber.e(e, "Login error $e")
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onStart() {
        super.onStart()
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}