package np.com.parts.Screens.StartingScreens

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import np.com.parts.API.Repository.AuthRepository
import np.com.parts.R
import np.com.parts.databinding.FragmentVerifyEmailBinding
import javax.inject.Inject


@AndroidEntryPoint
class VerifyEmailFragment : Fragment() {

    private var _binding: FragmentVerifyEmailBinding? = null
    private lateinit var mAuth: FirebaseAuth;

    private val binding get() = _binding!!

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentVerifyEmailBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAuth = Firebase.auth

        val bottomNavigationView = requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomBar)
        bottomNavigationView.visibility=View.GONE

        binding.btnSend.setOnClickListener{
        lifecycleScope.launch{
            val getemail = authRepository.getEmail().getOrNull()!!
            if (authRepository.getEmail().isSuccess){
                registerUser(getemail.email.value!!, getemail.cred)
                it.isEnabled=false
                }
            }
        }




    }



    // Login with email and password
    suspend fun registerUser(email: String, password: String) {
        if (mAuth.currentUser?.isEmailVerified == false){
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    Log.d("Auth", "User registered successfully: ${user?.email}")
                    // Optionally, send a verification email
                    user?.sendEmailVerification()
                } else {
                    Log.d("Auth", "Registration failed: ${task.exception?.message}")
                    Toast.makeText(requireContext(), "Somthing went wrong, please contact support", Toast.LENGTH_SHORT).show()
                }
            }
        }else{
            Toast.makeText(requireContext(), "The Email Is Already Verified", Toast.LENGTH_SHORT).show()
            authRepository.updateAccountStatus()
        }
    }

    fun sendEmailVerification() {
        val user: FirebaseUser? = mAuth.currentUser
        if (user != null) {
            user.sendEmailVerification()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(requireContext(), "Verification email sent, please check your inbox", Toast.LENGTH_SHORT).show()

                        // Notify the user to check their email
                    } else {
                        Log.d("Auth", "Failed to send verification email: ${task.exception?.message}")
                        Toast.makeText(requireContext(), "Failed to send verification email.", Toast.LENGTH_SHORT).show()
                        requireActivity().finishAffinity()
                    }
                }
        } else {
            Log.d("Auth", "No user is logged in to send verification email.")
        }
    }

    private fun updateUI(success: Boolean){
        if (success){
            Toast.makeText(requireContext(), "Your email was verified successfully", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
        if (!success){
            Toast.makeText(requireContext(), "Your email wasn't verified, Please verify it before proceeding", Toast.LENGTH_SHORT).show()
            binding.btnSend.isEnabled=true
        }
    }

    override fun onResume() {
        super.onResume()
        if(mAuth.currentUser?.isEmailVerified == true){
            updateUI(success = true)
        }
        if (mAuth.currentUser?.isEmailVerified == false){
            updateUI(success = false)

        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}