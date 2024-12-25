package np.com.parts.Presentation.Screens.Fragments.MainScreens

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
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import np.com.parts.API.BASE_URL
import np.com.parts.API.Models.formatted
import np.com.parts.Presentation.Adapter.CartAdapter
import np.com.parts.R
import np.com.parts.Domain.ViewModels.CartAction
import np.com.parts.Domain.ViewModels.CartState
import np.com.parts.Domain.ViewModels.CartViewModel
import np.com.parts.databinding.FragmentCartBinding
import np.com.parts.app_utils.SyncStatus
import dagger.hilt.android.AndroidEntryPoint
import io.ktor.client.HttpClient
import javax.inject.Inject

/**
 * An example full-screen fragment that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
@AndroidEntryPoint
class CartFragment : Fragment() {

    @Inject
    lateinit var client: HttpClient

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

        val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.bottomBar)
        bottomNavigationView.visibility=View.VISIBLE

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
                    when (event) {
                        is CartEvent.ShowMessage -> showSnackbar(event.message)
                        is CartEvent.ItemAdded -> showSnackbar("${event.name} added to cart")
                        is CartEvent.ItemRemoved -> showSnackbar("Item removed from cart")
                        is CartEvent.QuantityUpdated -> showSnackbar("Quantity updated")
                        is CartEvent.SyncEvent -> handleSyncEvent(event)
                    }

                }
            }
        }
    }

    private fun handleSyncEvent(event: CartEvent.SyncEvent) {
        binding.syncStatusIndicator.apply {
            when (event) {
                CartEvent.SyncEvent.Started -> {
                    setImageResource(R.drawable.ic_sync_pending)
                    isVisible = true
                }
                CartEvent.SyncEvent.Completed -> {
                    setImageResource(R.drawable.ic_sync_success)
                    // Hide after delay using viewLifecycleOwner
                    viewLifecycleOwner.lifecycleScope.launch {
                        delay(2000)
                        isVisible = false
                    }
                }
                is CartEvent.SyncEvent.Failed -> {
                    setImageResource(R.drawable.ic_sync_failed)
                    showSnackbar("Sync failed: ${event.error}")
                }
            }
        }
    }

    private fun updateUI(state: CartState) {
        binding.apply {
            progressBar.isVisible = state is CartState.Loading


            when (state){
                is CartState.Success -> {
                    holderOfCart.isVisible = true
                    emptyStateLayout.isVisible=false

                }
                is CartState.Error -> {
                    emptyStateLayout.isVisible = true
                    holderOfCart.isVisible=false
                    textViewState.text="An Error Occurred"
                }
                is CartState.Empty -> {
                    emptyStateLayout.isVisible = true
                    textViewState.text="Your Cart Is Empty"

                }
                else -> Unit

            }





            if (state is CartState.Error) {
                textViewState.text="An Error Occurred"
                emptyStateLayout.isVisible=true
            }
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
                proceedToCheckout()
            }

            startShoppingButton.setOnClickListener {
                findNavController().navigateUp()
            }

            syncButton.setOnClickListener {
                syncCart()
            }
        }
    }

    private fun proceedToCheckout() {
        viewLifecycleOwner.lifecycleScope.launch {
            binding.checkoutButton.isEnabled = false
            
            try {
                val response = client.get("$BASE_URL/protected")
                
                when (response.status) {
                    HttpStatusCode.Unauthorized -> {
                        findNavController().navigate(R.id.action_cartFragment_to_loginFragment)
                        return@launch
                    }
                    HttpStatusCode.OK -> {
                        viewModel.syncCart()
                            .collect { result ->
                                result.fold(
                                    onSuccess = {
                                        findNavController().navigate(R.id.action_cartFragment_to_checkoutFragment)
                                    },
                                    onFailure = { error ->
                                        showError("Failed to sync cart: ${error.message}")
                                    }
                                )
                            }
                    }
                    else -> showError("Unexpected error")
                }
            } catch (e: Exception) {
                showError("Network error: ${e.message}")
            } finally {
                binding.checkoutButton.isEnabled = true
            }
        }
    }

    private fun syncCart() {
        lifecycleScope.launch {
            viewModel.syncCart()
                .collect { result ->
                    result.fold(
                        onSuccess = {
                            // Success is handled via CartEvents
                        },
                        onFailure = { error ->
                            showError("Failed to sync: ${error.message}")
                        }
                    )
                }
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
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
