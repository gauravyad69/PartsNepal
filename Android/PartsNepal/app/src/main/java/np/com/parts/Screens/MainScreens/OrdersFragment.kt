package np.com.parts.Screens.MainScreens

import OrderViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import np.com.parts.API.Models.Order
import np.com.parts.databinding.FragmentOrdersBinding

/**
 * An example full-screen fragment that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class OrdersFragment : Fragment() {

    private var _binding: FragmentOrdersBinding? = null
    private val viewModel: OrderViewModel by viewModels()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        // Observe orders
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.orders.collect { orders ->
                updateOrdersList(orders)
            }
        }

        // Observe loading state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loading.collect { isLoading ->
                binding.progressBar.isVisible = isLoading
            }
        }

        // Observe errors
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.error.collect { error ->
                error?.let { showError(it) }
            }
        }
// Load orders
        viewModel.loadUserOrders()
    }
    private fun updateOrdersList(orders: List<Order>) {
// Update your RecyclerView or other UI components
    }
    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }






    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}