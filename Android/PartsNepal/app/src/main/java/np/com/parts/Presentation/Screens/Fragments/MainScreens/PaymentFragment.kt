package np.com.parts.Presentation.Screens.Fragments.MainScreens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import np.com.parts.Domain.ViewModels.PaymentViewModel
import np.com.parts.Presentation.Screens.Fragments.MainScreens.PaymentFragmentArgs
import np.com.parts.databinding.FragmentPaymentBinding
import timber.log.Timber
import kotlin.getValue

/**
 * An example full-screen fragment that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
@AndroidEntryPoint
class PaymentFragment : Fragment() {
    private val paymentViewModel: PaymentViewModel by viewModels()


    private val args: PaymentFragmentArgs by navArgs()

    ///checker is used to check if the khalti screen has been destroyed without the payment being made.
    //0 signifies that nothing has been initiated, 1 signifies payment initiated, 2 signifies response (complete, fail)
    private var checker=0
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

        setupToolbar()

        binding.payKhalti.setOnClickListener{
            binding.loadingView.visibility=View.VISIBLE
            binding.payKhalti.visibility=View.GONE
            binding.loadingView.startLoading()

            args.orderNumber?.let { orderNumber ->
                Timber.i("PaymentFragment got the order number $orderNumber")
                lifecycleScope.launch{
                    paymentViewModel.processPayment(orderNumber, requireActivity())
                    checker=1
                }
            }

        }

        lifecycleScope.launch{
            paymentViewModel.paymentState.collect { paymentResult ->
                paymentResult?.let {
                    when (it.status) {
                        "Completed" -> success("Payment Completed, Your Order Will Be Arriving Soon")
                        "Failed" -> show("Payment Failed: ${it.message}")
                        "Canceled" -> show("Payment Canceled")
                    }
                }
            }
        }
    }
    private fun setupToolbar() {
        binding.toolbar.apply {
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    private fun success(message: String){
        checker=2
        Snackbar.make(binding.root,message, Snackbar.LENGTH_LONG).show()
        findNavController().popBackStack()
        findNavController().navigateUp()
    }


    private fun show(message: String){
        checker=2
        Snackbar.make(binding.root,message, Snackbar.LENGTH_LONG).show()
        findNavController().popBackStack()
        findNavController().navigateUp()
    }



    override fun onResume() {
        super.onResume()
        if (checker==2||checker==0){
            binding.loadingView.visibility=View.GONE
            binding.payKhalti.visibility=View.VISIBLE
        }
        if (checker==2){
            findNavController().navigateUp()
        }
        if(checker==1){
            checker=0
            binding.loadingView.visibility=View.GONE
            binding.payKhalti.visibility=View.VISIBLE
            findNavController().navigateUp()
        }

    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}