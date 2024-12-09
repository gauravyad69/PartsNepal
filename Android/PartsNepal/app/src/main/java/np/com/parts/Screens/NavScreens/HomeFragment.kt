package np.com.parts.Screens.NavScreens

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import me.ibrahimsn.lib.SmoothBottomBar
import np.com.parts.ViewModels.ProductViewModel
import np.com.parts.Adapter.ShimmerAdapter
import np.com.parts.R
import np.com.parts.databinding.FragmentHomeBinding
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import dagger.hilt.android.AndroidEntryPoint
import io.ktor.client.request.get
import np.com.parts.API.BASE_URL
import np.com.parts.Items.BasicProductItem
import np.com.parts.Items.ProgressItem
import timber.log.Timber

@AndroidEntryPoint
class HomeFragment : Fragment() {



    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    private val viewModel: ProductViewModel by viewModels()
    private lateinit var fastAdapter: FastAdapter<AbstractBindingItem<*>>
    private lateinit var itemAdapter: ItemAdapter<BasicProductItem>
    private lateinit var footerAdapter: ItemAdapter<ProgressItem>
    private lateinit var shimmerAdapter: ShimmerAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root



    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bottomNavigationView = requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomBar)
        retainInstance = true





        bottomNavigationView.visibility=View.VISIBLE
        setupRecyclerView()
        setupObservers()
        setupListeners()
        setupShimmer()

        viewModel.loadBasicProducts()


    }

    private fun setupShimmer() {
        shimmerAdapter = ShimmerAdapter()
        binding.shimmerLayout.shimmerRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = shimmerAdapter
            addItemDecoration(GridSpacingItemDecoration(2, 16, true))
        }
    }

    private fun setupRecyclerView() {
        // Initialize adapters
        itemAdapter = ItemAdapter()
        footerAdapter = ItemAdapter()
        fastAdapter = FastAdapter.with(listOf(itemAdapter, footerAdapter))

        val gridLayoutManager = GridLayoutManager(context, 2).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when (fastAdapter.getItemViewType(position)) {
                        R.id.fastadapter_progress_item_id -> 2 // Full width for progress
                        else -> 1 // Normal items take 1 span
                    }
                }
            }
        }

        binding.recyclerView.apply {
            layoutManager = gridLayoutManager
            adapter = fastAdapter
            addItemDecoration(GridSpacingItemDecoration(2, 16, true))

            // Endless scrolling
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy > 0) {
                        val totalItemCount = gridLayoutManager.itemCount
                        val lastVisible = gridLayoutManager.findLastVisibleItemPosition()

                        if (lastVisible >= totalItemCount - 5) {
                            viewModel.loadBasicProducts()
                        }
                    }
                }
            })
        }

        // Handle item clicks
        fastAdapter.onClickListener = { _, _, item, _ ->
            when (item) {
                is BasicProductItem -> {
                    val action = HomeFragmentDirections.actionHomeFragmentToProductFragment(
                        productId = item.product.basic.productId,
                        productName = item.product.basic.productName
                    )
                    findNavController().navigate(action)
                }
            }
            false
        }

        // Setup refresh
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.resetPagination()
            viewModel.loadBasicProducts()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }


    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.basicProducts.collect { products ->
                    val items = products.map { BasicProductItem(it) }
                    itemAdapter.set(items)
                    
                    if (products.isNotEmpty()) {
                        binding.shimmerLayout.root.visibility = View.GONE
                        binding.recyclerView.visibility = View.VISIBLE
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoadingMore.collect { isLoading ->
                    if (isLoading) {
                        footerAdapter.add(ProgressItem())
                    } else {
                        footerAdapter.clear()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loading.collect { isLoading ->
                    if (isLoading && viewModel.basicProducts.value.isEmpty()) {
                        // Show shimmer only for initial load when no items are present
                        binding.shimmerLayout.root.visibility = View.VISIBLE
                        binding.recyclerView.visibility = View.GONE
                    } else {
                        binding.shimmerLayout.root.visibility = View.GONE
                        binding.recyclerView.visibility = View.VISIBLE
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.error.collect { error ->
                    error?.let {
                        Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        // Remove the old listener setup and use the one in setupRecyclerView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}




// Grid spacing decoration
class GridSpacingItemDecoration(
    private val spanCount: Int,
    private val spacing: Int,
    private val includeEdge: Boolean
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val column = position % spanCount

        if (includeEdge) {
            outRect.left = spacing - column * spacing / spanCount
            outRect.right = (column + 1) * spacing / spanCount

            if (position < spanCount) {
                outRect.top = spacing
            }
            outRect.bottom = spacing
        } else {
            outRect.left = column * spacing / spanCount
            outRect.right = spacing - (column + 1) * spacing / spanCount
            if (position >= spanCount) {
                outRect.top = spacing
            }
        }
    }
}