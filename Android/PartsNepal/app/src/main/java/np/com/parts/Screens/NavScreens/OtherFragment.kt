package np.com.parts.Screens.NavScreens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import np.com.parts.API.NetworkModule
import np.com.parts.API.Models.AccountType
import np.com.parts.API.Repository.UserRepository
import np.com.parts.R
import np.com.parts.databinding.FragmentOtherBinding

class OtherFragment : Fragment() {
    private var _binding: FragmentOtherBinding? = null
    private val binding get() = _binding!!
    
    private val userRepository by lazy {
        UserRepository(NetworkModule.provideHttpClient())
    }

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
        val updates = mapOf(
            "firstName" to binding.firstNameInput.text.toString(),
            "lastName" to binding.lastNameInput.text.toString(),
            "email" to binding.emailInput.text.toString(),
            "accountType" to AccountType.valueOf(
                binding.accountTypeDropdown.text.toString().uppercase()
            )
        )

//        binding.progressBar.visibility = View.VISIBLE
        binding.loginButton.isEnabled = false

        lifecycleScope.launch {
            try {
                userRepository.updateProfile(updates)
                    .onSuccess {
                        // Update successful
                        findNavController().navigate(R.id.action_otherFragment_to_homeFragment2)
                    }
                    .onFailure { exception ->
                        showError(exception.message ?: "Failed to update profile")
                    }
            } catch (e: Exception) {
                showError("An error occurred: ${e.message}")
            } finally {
//                binding.progressBar.visibility = View.GONE
                binding.loginButton.isEnabled = true
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