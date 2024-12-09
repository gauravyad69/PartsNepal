package np.com.parts.Screens.MainScreens

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.khalti.checkout.Khalti
import com.khalti.checkout.data.Environment
import com.khalti.checkout.data.KhaltiPayConfig
import kotlinx.coroutines.launch
import np.com.parts.API.Repository.KhaltiPaymentRepository
import np.com.parts.Screens.OrderScreens.OrderDetailsFragmentArgs
import np.com.parts.ViewModels.CheckoutViewModel
import np.com.parts.ViewModels.PaymentViewModel
import np.com.parts.databinding.FragmentPaymentBinding
import np.com.parts.workers.PaymentViewModelFactory
import kotlin.getValue

/**
 * An example full-screen fragment that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class PaymentFragment : Fragment() {

    private val paymentViewModel: PaymentViewModel by viewModels {
        val repository = KhaltiPaymentRepository(requireActivity())
        PaymentViewModelFactory(repository)
    }
    private val args: PaymentFragmentArgs by navArgs()


    private var _binding: FragmentPaymentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentPaymentBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.payKhalti.setOnClickListener{
            args.orderNumber?.let { orderNumber ->
                lifecycleScope.launch{
                    paymentViewModel.processPayment(orderNumber, requireContext())
                }
            }

        }
        lifecycleScope.launch{
            paymentViewModel.paymentState.collect { paymentResult ->
                paymentResult?.let {
                    when (it.status) {
                        "Completed" -> show("Payment Completed, Your Order Will Be Arriving Soon")
                        "Failed" -> show("Payment Failed: ${it.message}")
                        "Canceled" -> show("Payment Canceled")
                    }
                }
            }
        }
    }



    private fun show(message: String){
        Snackbar.make(binding.root,message, Snackbar.LENGTH_LONG).show()
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}