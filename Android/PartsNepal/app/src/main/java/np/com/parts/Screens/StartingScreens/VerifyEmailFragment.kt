package np.com.parts.Screens.StartingScreens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import np.com.parts.databinding.FragmentVerifyEmailBinding

class VerifyEmailFragment : Fragment() {

    private var _binding: FragmentVerifyEmailBinding? = null


    private val binding get() = _binding!!

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

    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}