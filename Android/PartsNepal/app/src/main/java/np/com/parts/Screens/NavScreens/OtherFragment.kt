package np.com.parts.Screens.NavScreens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import np.com.parts.databinding.FragmentOtherBinding


class OtherFragment : Fragment() {

    private var _binding: FragmentOtherBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentOtherBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)




        binding.button2.setOnClickListener{



        }

    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}