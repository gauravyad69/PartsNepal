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
import np.com.parts.Adapter.OrdersAdapter
import np.com.parts.API.Models.OrderModel
import np.com.parts.ViewModels.OrderViewModel
import np.com.parts.databinding.FragmentOrdersBinding

@AndroidEntryPoint
class OrdersFragment : Fragment() {
    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: OrderViewModel by viewModels()
    private val args: OrdersFragmentArgs by navArgs()
    
    private val ordersAdapter = OrdersAdapter(
        onOrderClicked = { order ->
            navigateToOrderDetails(order)
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupRecyclerView()
        setupObservers()
        
        // If we have an orderId from navigation, scroll to that order
        args.orderId?.let { orderId ->
            viewModel.getOrderDetails(orderId)
        } ?: run {
            viewModel.loadUserOrders()
        }
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    private fun setupRecyclerView() {
        binding.ordersRecyclerView.apply {
            adapter = ordersAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.orderState.collect { state ->
                    when (state) {
                        is OrderViewModel.OrderState.Loading -> {
                            showLoading(true)
                        }
                        is OrderViewModel.OrderState.Success -> {
                            showLoading(false)
                            updateOrdersList(state.orders)
                        }
                        is OrderViewModel.OrderState.Error -> {
                            showLoading(false)
                            showError(state.message)
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            progressBar.isVisible = isLoading
            ordersRecyclerView.isVisible = !isLoading
            emptyStateLayout.isVisible = false
        }
    }

    private fun updateOrdersList(orders: List<OrderModel>) {
        if (orders.isEmpty()) {
            showEmptyState()
        } else {
            binding.emptyStateLayout.isVisible = false
            ordersAdapter.submitList(orders)
            
            // If we have an orderId from navigation, scroll to that order
            args.orderId?.let { orderId ->
                val position = orders.indexOfFirst { it.id == orderId }
                if (position != -1) {
                    binding.ordersRecyclerView.scrollToPosition(position)
                }
            }
        }
    }

    private fun showEmptyState() {
        binding.apply {
            emptyStateLayout.isVisible = true
            ordersRecyclerView.isVisible = false
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun navigateToOrderDetails(order: OrderModel) {
        findNavController().navigate(
            OrdersFragmentDirections.Companion.actionOrdersFragmentToOrderDetailsFragment(
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