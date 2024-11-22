package np.com.parts.Screens.NavScreens

import np.com.parts.ViewModels.UserProfileViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import np.com.parts.API.Models.AccountType
import np.com.parts.API.Models.UpdateProfileRequest
import np.com.parts.API.Models.UserModel
import np.com.parts.API.TokenManager
import np.com.parts.databinding.FragmentProfileBinding


/**
 * An example full-screen fragment that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    private val viewModel: UserProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

binding.logoutButton.setOnClickListener{
    TokenManager.getInstance(requireContext()).clearToken()
}
        binding.editProfileButton.setOnClickListener {
            binding.editProfileCard.visibility = View.VISIBLE
            // Optionally animate the card appearance
            binding.editProfileCard.alpha = 0f
            binding.editProfileCard.animate()
                .alpha(1f)
                .setDuration(300)
                .start()
        }

        binding.cancelButton.setOnClickListener {
            binding.editProfileCard.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction {
                    binding.editProfileCard.visibility = View.GONE
                }
                .start()
        }
        viewModel.loadUserProfile()
        setupUpdateButton()
        setupObserversForUpdate()




    }

    private fun setupObserversForUpdate() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.profileUpdateState.collect { state ->
                when (state) {
                    is UserProfileViewModel.ProfileUpdateState.Loading -> {
                        binding.progressBar.isVisible = true
                        binding.updateButton.isEnabled = false
                    }
                    is UserProfileViewModel.ProfileUpdateState.Success -> {
                        binding.progressBar.isVisible = false
                        binding.updateButton.isEnabled = true
//                        showSuccess(state.message)
                        // Optionally navigate or show success UI
                    }
                    is UserProfileViewModel.ProfileUpdateState.Error -> {
                        binding.progressBar.isVisible = false
                        binding.updateButton.isEnabled = true
                        showError(state.message)
                    }
                    is UserProfileViewModel.ProfileUpdateState.Idle -> {
                        binding.progressBar.isVisible = false
                        binding.updateButton.isEnabled = true
                    }

                    else -> {}
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.userProfile.collect { profile ->
                profile?.let { updateUI(it) }
            }
        }

    }

    private fun setupUpdateButton() {
        binding.updateButton.setOnClickListener {
            val request = UpdateProfileRequest(
                firstName = binding.firstNameInput.text?.toString()?.takeIf { it.isNotBlank() },
                lastName = binding.lastNameInput.text?.toString()?.takeIf { it.isNotBlank() },
                email = binding.emailInput.text?.toString()?.takeIf { it.isNotBlank() },
                accountType = try {
                    binding.accountTypeDropdown.text?.toString()?.uppercase()?.let {
                        AccountType.valueOf(it.toString())
                    }
                } catch (e: IllegalArgumentException) {
                    null
                }
            )
            viewModel.updateProfile(request)
        }
    }



    private fun updateUI(profile: UserModel) {
        binding.apply {
            firstNameInput.setText(profile.firstName)
            lastNameInput.setText(profile.lastName)
//            emailInput.setText(profile.email)
            accountTypeDropdown.setText(profile.accountType.toString())
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