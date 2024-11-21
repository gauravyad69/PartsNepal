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
import kotlinx.coroutines.launch
import np.com.parts.API.Repository.AuthRepository
import np.com.parts.API.NetworkModule
import np.com.parts.R
import np.com.parts.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var authRepository: AuthRepository

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

        authRepository= AuthRepository(
            NetworkModule.provideHttpClient(),
            requireContext()
        )

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
        val isPhoneLogin = identifier.all { it.isDigit() }

        lifecycleScope.launch {
            try {
                val result = authRepository.login(identifier, password, isPhoneLogin)
                result.fold(
                    onSuccess = { response ->
                        // Save auth token
                        // Navigate to main screen
                        NavHostFragment.findNavController(this@LoginFragment)
                            .navigate(R.id.action_loginFragment_to_homeFragment);
                    },
                    onFailure = { exception ->
                        showError(exception.message ?: "Login failed")
                    }
                )
            } catch (e: Exception) {
                showError(e.message ?: "An error occurred")
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