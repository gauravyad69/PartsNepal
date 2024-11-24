package np.com.parts.Screens.MainScreens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import kotlinx.coroutines.launch
import np.com.parts.ViewModels.ProductViewModel
import np.com.parts.Items.BasicProductItem
import np.com.parts.Items.ProgressItem
import np.com.parts.R
import np.com.parts.Screens.NavScreens.GridSpacingItemDecoration
import np.com.parts.databinding.FragmentSearchedProductsBinding
import np.com.parts.API.Models.ProductModel
import np.com.parts.API.Models.toBasicView

class SearchedProductsFragment : Fragment() {
    private var _binding: FragmentSearchedProductsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProductViewModel by viewModels()
    
    private lateinit var fastAdapter: FastAdapter<AbstractBindingItem<*>>
    private lateinit var itemAdapter: ItemAdapter<BasicProductItem>
    private lateinit var footerAdapter: ItemAdapter<ProgressItem>

    private var currentPage = 1
    private var isLastPage = false
    private var isLoading = false
    private var searchQuery: String = ""
    private var onSale: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchedProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Get search parameters from arguments
        arguments?.let {
            searchQuery = it.getString("query", "")
            onSale = it.getBoolean("onSale", false)
            binding.searchQueryText.text = "Search results for '$searchQuery'"
        }
        
        setupRecyclerView()
        setupObservers()
        performInitialSearch()
    }

    private fun setupRecyclerView() {
        itemAdapter = ItemAdapter()
        footerAdapter = ItemAdapter()
        fastAdapter = FastAdapter.with(listOf(itemAdapter, footerAdapter))

        val gridLayoutManager = GridLayoutManager(context, 2).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when (fastAdapter.getItemViewType(position)) {
                        R.id.fastadapter_progress_item_id -> 2
                        else -> 1
                    }
                }
            }
        }

        binding.recyclerView.apply {
            layoutManager = gridLayoutManager
            adapter = fastAdapter
            addItemDecoration(GridSpacingItemDecoration(2, 16, true))

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy > 0) {
                        val totalItemCount = gridLayoutManager.itemCount
                        val lastVisible = gridLayoutManager.findLastVisibleItemPosition()

                        if (!isLoading && !isLastPage && lastVisible >= totalItemCount - 5) {
                            loadMoreItems()
                        }
                    }
                }
            })
        }

        // Handle item clicks
        fastAdapter.onClickListener = { _, _, item, _ ->
            when (item) {
                is BasicProductItem -> {
                    findNavController().navigate(
                        R.id.action_searchedProductsFragment_to_productFragment,
                        Bundle().apply {
                            putInt("productId", item.product.basic.productId)
                            putString("productName", item.product.basic.productName)
                        }
                    )
                }
            }
            false
        }

        // Setup refresh
        binding.swipeRefreshLayout.setOnRefreshListener {
            resetSearch()
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.searchResults.collect { products ->
                        handleSearchResults(products)
                    }
                }

                launch {
                    viewModel.isSearching.collect { isSearching ->
                        handleLoadingState(isSearching)
                    }
                }

                launch {
                    viewModel.error.collect { error ->
                        error?.let {
                            // Show error message
                            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun handleSearchResults(products: List<ProductModel>) {
        if (products.isEmpty() && currentPage == 1) {
            binding.emptyStateText.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        } else {
            binding.emptyStateText.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
            
            val items = products.map { product ->
                BasicProductItem(product.toBasicView())
            }
            
            if (currentPage == 1) {
                itemAdapter.set(items)
            } else {
                itemAdapter.add(items)
            }
        }
        binding.swipeRefreshLayout.isRefreshing = false
    }

    private fun handleLoadingState(isLoading: Boolean) {
        this.isLoading = isLoading
        if (currentPage == 1) {
            binding.shimmerLayout.root.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.recyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE
        } else {
            if (isLoading) footerAdapter.add(ProgressItem())
            else footerAdapter.clear()
        }
    }

    private fun performInitialSearch() {
        currentPage = 1
        isLastPage = false
        viewModel.searchProducts(
            query = searchQuery,
            page = currentPage,
            onSale = if (onSale) true else null
        )
    }

    private fun loadMoreItems() {
        if (!isLoading && !isLastPage) {
            currentPage++
            viewModel.searchProducts(
                query = searchQuery,
                page = currentPage,
                onSale = if (onSale) true else null
            )
        }
    }

    private fun resetSearch() {
        currentPage = 1
        isLastPage = false
        performInitialSearch()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}