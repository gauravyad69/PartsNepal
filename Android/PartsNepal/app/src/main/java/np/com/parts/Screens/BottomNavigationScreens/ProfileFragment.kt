package np.com.parts.Screens.BottomNavigationScreens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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


        //handle current_tier click
        binding.currentTierCard.setOnClickListener{
//            startActivity(Intent(requireContext(), UpgradeActivity::class.java))
        }

//        val email = FirebaseAuth.getInstance().currentUser?.email!!
        val email = "gauravyad2077@gmail.com"
        //load user data such as name email, tier from API




    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}