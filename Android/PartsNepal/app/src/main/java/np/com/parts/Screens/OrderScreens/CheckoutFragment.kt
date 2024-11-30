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
import kotlinx.coroutines.launch
import np.com.parts.databinding.FragmentCheckoutBinding
import np.com.parts.ViewModels.CheckoutViewModel
import np.com.parts.API.Models.*
import np.com.parts.R
import np.com.parts.ViewModels.CheckoutState
import np.com.parts.utils.DialogManager
import timber.log.Timber

/**
 * An example full-screen fragment that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class CheckoutFragment : Fragment() {

    private var _binding: FragmentCheckoutBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: CheckoutViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentCheckoutBinding.inflate(inflater, container, false)
        return binding.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupObservers()
        setupOrderButton()
//        viewModel.loadCartSummary()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupOrderButton() {
        binding.placeOrderButton.setOnClickListener {
            placeOrder()
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.checkoutState.collect { state ->
                    when (state) {
                        is CheckoutState.Initial -> {
                            binding.placeOrderButton.isEnabled = true
                            binding.progressBar.isVisible = false
                        }
                        is CheckoutState.Loading -> {
                            binding.placeOrderButton.isEnabled = false
                            binding.progressBar.isVisible = true
                            Timber.i("Processing order...")
                        }
                        is CheckoutState.Success -> {
                            binding.progressBar.isVisible = false
                            binding.placeOrderButton.isEnabled = true
                            handleOrderSuccess(state.order)
                            Timber.i("Order placed successfully")
                        }
                        is CheckoutState.Error -> {
                            binding.progressBar.isVisible = false
                            binding.placeOrderButton.isEnabled = true
                            DialogManager(requireContext()).showError(
                                title = "Order Failed",
                                message = state.message
                            )
                            Timber.e("Order failed: ${state.message}")
                        }
                    }
                }
            }
        }
    }

    private fun placeOrder() {
        if (!validateInputs()) return

        // Show loading state
        binding.placeOrderButton.isEnabled = false

        val shippingDetails = ShippingDetails(
            address = ShippingAddress(
                recipient = RecipientInfo(
                    name = binding.fullNameInput.text.toString().trim(),
                    phone = binding.phoneInput.text.toString().trim()
                ),
                street = binding.streetInput.text.toString().trim(),
                ward = binding.wardInput.text.toString().toIntOrNull() ?: 0,
                city = binding.cityInput.text.toString().trim(),
                district = binding.districtInput.text.toString().trim(),
                province = binding.provinceInput.text.toString().trim(),
                landmark = binding.landmarkInput.text?.toString()?.trim()
            ),
            method = ShippingMethod.STANDARD,
            cost = Money(0)
        )

        val paymentMethod = when (binding.paymentMethodGroup.checkedRadioButtonId) {
            R.id.cashOnDeliveryRadio -> PaymentMethod.CASH_ON_DELIVERY
            R.id.khaltiRadio -> PaymentMethod.KHALTI
            R.id.esewaRadio -> PaymentMethod.ESEWA
            else -> PaymentMethod.CASH_ON_DELIVERY
        }

        viewModel.placeOrder(
            shippingDetails = shippingDetails,
            paymentMethod = paymentMethod,
            notes = binding.notesInput.text?.toString()?.trim()
        )
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        
        binding.apply {
            // Validate name
            if (fullNameInput.text.isNullOrBlank()) {
                fullNameInput.error = "Name is required"
                isValid = false
            }
            
            // Validate phone
            if (phoneInput.text.isNullOrBlank()) {
                phoneInput.error = "Phone number is required"
                isValid = false
            }
            
            // Validate address
            if (streetInput.text.isNullOrBlank()) {
                streetInput.error = "Street address is required"
                isValid = false
            }
            
            if (wardInput.text.isNullOrBlank()) {
                wardInput.error = "Ward number is required"
                isValid = false
            }
            
            if (cityInput.text.isNullOrBlank()) {
                cityInput.error = "City is required"
                isValid = false
            }
            
            if (districtInput.text.isNullOrBlank()) {
                districtInput.error = "District is required"
                isValid = false
            }
            
            if (provinceInput.text.isNullOrBlank()) {
                provinceInput.error = "Province is required"
                isValid = false
            }
        }
        
        return isValid
    }


    ///todo this
    private fun updateOrderSummaryUI(items: List<LineItem>, summary: OrderSummary) {
        binding.apply {
            // Update summary
            subtotalText.text = summary.subtotal.formatted()
            shippingText.text = summary.shippingCost.formatted()
            totalText.text = summary.total.formatted()
            
            // Show discount if available
            if (summary.discount != null) {
                discountText.isVisible = true
                discountText.text = "-${summary.discount.formatted()}"
            } else {
                discountText.isVisible = false
            }
        }
    }

    private fun handleOrderSuccess(order: OrderModel) {
        findNavController().navigate(
            CheckoutFragmentDirections.Companion
                .actionCheckoutFragmentToOrderConfirmationFragment(
                    orderId = order.id,
                    orderNumber = order.orderNumber
                )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}