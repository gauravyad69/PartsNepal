package np.com.parts.Presentation.Screens.Fragments.ProductScreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import np.com.parts.Domain.ViewModels.ProductViewModel
import np.com.parts.R
import np.com.parts.databinding.FragmentSearchBinding


@AndroidEntryPoint
class SearchFragment : Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProductViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.bottomBar)
        bottomNavigationView.visibility=View.VISIBLE
        
        setupSearch()
        setupFilters()
    }

    private fun setupSearch() {
        binding.searchView.setupWithSearchBar(binding.searchBar)

        binding.searchView.editText.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val searchText = textView.text.toString()
                if (searchText.isNotBlank()) {
                    performSearch(searchText)
                    binding.searchView.hide()
                    return@setOnEditorActionListener true
                }
            }
            false
        }
    }

    private fun setupFilters() {
        binding.onSaleChip.setOnCheckedChangeListener { _, _ ->
            // Handle filter changes if needed
        }

        binding.priceFilterChip.setOnClickListener {
            showPriceRangeDialog()
        }

        binding.typeFilterChip.setOnClickListener {
            showProductTypeDialog()
        }
    }

    private fun showPriceRangeDialog() {
        val prices = arrayOf("Any", "Under 1000", "1000-5000", "5000-10000", "Above 10000")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Price Range")
            .setItems(prices) { _, which ->
                val minPrice = when (which) {
                    1 -> 0L
                    2 -> 1000L
                    3 -> 5000L
                    4 -> 10000L
                    else -> null
                }
                binding.priceFilterChip.isChecked = which != 0
            }
            .show()
    }

    private fun showProductTypeDialog() {
        val types = arrayOf("Any", "Engine", "Brake", "Transmission", "Suspension")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Product Type")
            .setItems(types) { _, which ->
                val selectedType = if (which == 0) null else types[which]
                binding.typeFilterChip.isChecked = which != 0
            }
            .show()
    }

    private fun performSearch(query: String) {
        val onSale = if (binding.onSaleChip.isChecked) true else null
        findNavController().navigate(
            R.id.action_searchFragment_to_searchedProductsFragment,
            Bundle().apply {
                putString("query", query)
                putBoolean("onSale", onSale ?: false)
                // Add other filters as needed
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}