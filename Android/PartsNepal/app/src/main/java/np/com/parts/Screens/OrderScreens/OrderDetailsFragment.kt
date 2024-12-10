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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import np.com.parts.API.Models.OrderModel
import np.com.parts.API.Models.PaymentStatus
import np.com.parts.API.Models.formatted
import np.com.parts.API.Models.formattedDate
import np.com.parts.Adapter.OrderItemsAdapter
import np.com.parts.ViewModels.OrderViewModel
import np.com.parts.databinding.FragmentOrderDetailsBinding
import timber.log.Timber
import java.util.Locale

/**
 * An example full-screen fragment that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
@AndroidEntryPoint
class OrderDetailsFragment : Fragment() {

    private var _binding: FragmentOrderDetailsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: OrderViewModel by viewModels()
    private val args: OrderDetailsFragmentArgs by navArgs()

    private val orderItemsAdapter = OrderItemsAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentOrderDetailsBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupObservers()
        binding.pay.visibility= View.GONE

        binding.pay.setOnClickListener{
            args.orderNumber?.let { orderNumber ->
                findNavController().navigate(
                    OrderDetailsFragmentDirections.Companion
                        .actionOrderDetailsFragmentToPaymentFragment(
                            orderNumber = orderNumber
                        )
                )
            }
        }

        args.orderNumber?.let { orderNumber ->
            viewModel.getOrderDetails(orderNumber)
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        binding.orderItemsRecyclerView.apply {
            adapter = orderItemsAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(false)
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.orderState.collect { state ->
                    when (state) {
                        is OrderViewModel.OrderState.Loading -> {
                            binding.progressBar.isVisible = true
                        }
                        is OrderViewModel.OrderState.Success -> {
                            binding.progressBar.isVisible = false
                            state.orders.firstOrNull()?.let { order ->
                                updateOrderDetails(order)
                            }
                        }
                        is OrderViewModel.OrderState.Error -> {
                            binding.progressBar.isVisible = false
                            showError(state.message)
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    private fun updateOrderDetails(order: OrderModel) {
        binding.apply {
            orderNumberText.text = "Order #${order.orderNumber}"
            orderDateText.text = "Placed on ${order.formattedDate()}"
            orderStatusText.text = order.status.toString()
                .replace("_", " ")
                .lowercase()
                .capitalize(Locale.ROOT)

            // Update shipping details
            with(order.shippingDetails) {
                recipientNameText.text = address.recipient.name
                recipientPhoneText.text = address.recipient.phone
                addressText.text = buildString {
                    append("${address.street}\n")
                    append("Ward ${address.ward}, ${address.city}\n")
                    append("${address.district}, ${address.province}")
                }
            }

            // Update order summary
            with(order.summary) {
                subtotalText.text = subtotal.formatted()
                shippingText.text = shippingCost.formatted()
                totalText.text = total.formatted()
                discountText.text = discount?.formatted()
            }

            // Update order items
            orderItemsAdapter.submitList(order.items)



            if(order.payment.status.equals(PaymentStatus.PENDING)){
                pay.visibility= View.VISIBLE
            }
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}