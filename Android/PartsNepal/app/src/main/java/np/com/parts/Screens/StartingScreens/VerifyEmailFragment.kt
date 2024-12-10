package np.com.parts.Screens.StartingScreens

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
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


        lifecycleScope.launch{
            val getemail = authRepository.getEmail().getOrNull()!!
            if (authRepository.getEmail().isSuccess){
            binding.btnSend.setOnClickListener{
                registerUser(getemail.email.value!!, getemail.cred)
                }
            }
        }




    }



    // Login with email and password
    fun registerUser(email: String, password: String) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    Log.d("Auth", "User registered successfully: ${user?.email}")
                    Toast.makeText(requireContext(), "Sending verification email, please check your inbox", Toast.LENGTH_SHORT).show()
                    // Optionally, send a verification email
                    user?.sendEmailVerification()
                } else {
                    Log.d("Auth", "Registration failed: ${task.exception?.message}")
                }
            }
    }

    fun sendEmailVerification() {
        val user: FirebaseUser? = mAuth.currentUser
        if (user != null) {
            user.sendEmailVerification()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("Auth", "Verification email sent.")
                        // Notify the user to check their email
                    } else {
                        Log.d("Auth", "Failed to send verification email: ${task.exception?.message}")
                    }
                }
        } else {
            Log.d("Auth", "No user is logged in to send verification email.")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}