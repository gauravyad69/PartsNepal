package np.com.parts.Screens.StartingScreens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import np.com.parts.API.Repository.AuthRepository
import np.com.parts.API.NetworkModule
import np.com.parts.API.Models.AccountType
import np.com.parts.R
import np.com.parts.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var authRepository: AuthRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        authRepository= AuthRepository(NetworkModule.provideHttpClient(), requireContext())
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
                val result = authRepository.register(
                    email = null, // Since this is phone-based registration
                    phoneNumber = phoneNumber,
                    password = password,
                    firstName = "", // These will be set in the next step
                    lastName = "",
                    username = phoneNumber, // Using phone number as initial username
                    accountType = AccountType.PERSONAL
                )

                result.fold(
                    onSuccess = { response ->
                        // Navigate to additional info fragment
                        findNavController().navigate(R.id.action_registerFragment_to_otherFragment)
                    },
                    onFailure = { exception ->
                        showError(exception.message ?: "Registration failed $exception")
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
