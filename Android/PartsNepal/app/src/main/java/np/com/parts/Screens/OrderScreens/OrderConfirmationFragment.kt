package np.com.parts.Screens.OrderScreens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import np.com.parts.API.Models.OrderModel
import np.com.parts.API.Models.formattedDate
import np.com.parts.API.Models.formattedTotal
import np.com.parts.ViewModels.OrderConfirmationState
import np.com.parts.ViewModels.OrderConfirmationViewModel
import np.com.parts.databinding.FragmentOrderConfirmationBinding

@AndroidEntryPoint
class OrderConfirmationFragment : Fragment() {
    private var _binding: FragmentOrderConfirmationBinding? = null
    private val binding get() = _binding!!

    private val args: OrderConfirmationFragmentArgs by navArgs()
    
    private val viewModel: OrderConfirmationViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderConfirmationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupObservers()
        setupButtons()
        
        viewModel.loadOrder(args.orderNumber)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            navigateToHome()
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.orderState.collect { state ->
                    when (state) {
                        is OrderConfirmationState.Loading -> showLoading(true)
                        is OrderConfirmationState.Success -> {
                            showLoading(false)
                            updateOrderDetails(state.order)
                        }
                        is OrderConfirmationState.Error -> {
                            showLoading(false)
                            showError(state.message)
                        }
                    }
                }
            }
        }
    }

    private fun setupButtons() {
        binding.trackOrderButton.setOnClickListener {
            navigateToOrders()
        }

        binding.continueShoppingButton.setOnClickListener {
            navigateToHome()
        }
    }

    private fun updateOrderDetails(order: OrderModel) {
        binding.apply {
            orderNumberText.text = order.orderNumber
            orderDateText.text = order.formattedDate()
            totalAmountText.text = order.formattedTotal()
            paymentMethodText.text = order.payment.method.toString()
                .replace("_", " ")
                .lowercase()
                .capitalize()
        }
    }

    private fun showLoading(show: Boolean) {
        binding.apply {
            progressBar.isVisible = show
            contentScrollView.isVisible = !show
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun navigateToOrders() {
        findNavController().navigate(
            OrderConfirmationFragmentDirections.Companion.actionOrderConfirmationFragmentToOrdersFragment(
                orderNumber = args.orderNumber
            )
        )
    }

    private fun navigateToHome() {
        findNavController().navigate(
            OrderConfirmationFragmentDirections.Companion.actionOrderConfirmationFragmentToHomeFragment()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 