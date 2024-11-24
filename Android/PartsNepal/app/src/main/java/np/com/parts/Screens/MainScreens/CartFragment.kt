package np.com.parts.Screens.MainScreens

import CartEvent
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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import np.com.parts.API.Models.formatted
import np.com.parts.Adapter.CartAdapter
import np.com.parts.R
import np.com.parts.ViewModels.CartAction
import np.com.parts.ViewModels.CartState
import np.com.parts.ViewModels.CartViewModel
import np.com.parts.databinding.FragmentCartBinding
import np.com.parts.utils.SyncStatus

/**
 * An example full-screen fragment that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class CartFragment : Fragment() {


    private var _binding: FragmentCartBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: CartViewModel by viewModels()

    private val cartAdapter = CartAdapter(
        onQuantityChanged = { item, newQuantity ->
            viewModel.dispatch(CartAction.UpdateQuantity(item.id, newQuantity))
        },
        onRemoveClicked = { item ->
            viewModel.dispatch(CartAction.RemoveItem(item.id))
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        observeState()
        observeEvents()
    }

    private fun setupRecyclerView() {
        binding.cartItemsRecyclerView.apply {
            adapter = cartAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(
                DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
            )
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.cartState.collect { state ->
                    updateUI(state)
                }
            }
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.cartEvents.collect { event ->
                    handleEvent(event)
                }
            }
        }
    }

    private fun updateUI(state: CartState) {
        binding.apply {
            progressBar.isVisible = state is CartState.Loading
            emptyStateLayout.isVisible = state is CartState.Empty
            cartItemsRecyclerView.isVisible = state is CartState.Success
            orderSummaryCard.isVisible = state is CartState.Success

            when (state) {
                is CartState.Success -> {
                    cartAdapter.submitList(state.items)
                    
                    // Update summary
                    subtotalText.text = state.summary.subtotal.formatted()
                    deliveryFeeText.text = state.summary.shippingCost.formatted()
                    totalText.text = state.summary.total.formatted()
                    
                    // Update sync status
                    syncStatusIndicator.apply {
                        isVisible = true
                        setImageResource(
                            when (state.syncStatus) {
                                SyncStatus.SYNCING -> R.drawable.ic_sync_pending
                                SyncStatus.SUCCESS -> R.drawable.ic_sync_success
                                SyncStatus.FAILED -> R.drawable.ic_sync_failed
                                else -> R.drawable.ic_sync_idle
                            }
                        )
                    }
                }
                is CartState.Error -> {
                    showError(state.message)
                }
                else -> Unit
            }
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            checkoutButton.setOnClickListener {
                // Ensure cart is synced before proceeding to checkout
                viewModel.syncCart()
                findNavController().navigate(R.id.action_cartFragment_to_checkoutFragment)
            }

            startShoppingButton.setOnClickListener {
                findNavController().navigateUp()
            }

            // Manual sync button
            syncButton.setOnClickListener {
                viewModel.syncCart()
            }
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun handleEvent(event: CartEvent) {
        when (event) {
            is CartEvent.ShowMessage -> {
                showSnackbar(event.message)
            }
            is CartEvent.ItemAdded -> {
                showSnackbar("${event.name} added to cart")
            }
            is CartEvent.ItemRemoved -> {
                showSnackbar("Item removed from cart")
            }
            is CartEvent.QuantityUpdated -> {
                // Optional: Show quantity update confirmation
                showSnackbar("Quantity updated")
            }
            is CartEvent.SyncStarted -> {
                binding.syncStatusIndicator.setImageResource(R.drawable.ic_sync_pending)
                binding.syncStatusIndicator.isVisible = true
            }
            is CartEvent.SyncCompleted -> {
                binding.syncStatusIndicator.setImageResource(R.drawable.ic_sync_success)
                // Hide the indicator after a delay
                binding.syncStatusIndicator.postDelayed({
                    binding.syncStatusIndicator.isVisible = false
                }, 2000)
            }
            is CartEvent.SyncFailed -> {
                binding.syncStatusIndicator.setImageResource(R.drawable.ic_sync_failed)
                showSnackbar("Failed to sync cart: ${event.error}")
            }
            else -> Unit // Handle any other events
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(
            binding.root,
            message,
            Snackbar.LENGTH_SHORT
        ).apply {
//            // Optional: Add action for certain messages
//            setAction("UNDO") {
//                // Handle undo action if needed
//                viewModel.dispatch(CartAction.RemoveItem())
//            }
            show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
