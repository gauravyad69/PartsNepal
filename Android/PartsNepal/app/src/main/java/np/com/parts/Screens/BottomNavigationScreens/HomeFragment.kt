package np.com.parts.Screens.BottomNavigationScreens

import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
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
import np.com.parts.API.Product.ProductViewModel
import np.com.parts.Adapter.BasicProductAdapter
import np.com.parts.Adapter.BasicProductAdapter.Companion.LOADING_ITEM_VIEW_TYPE
import np.com.parts.Adapter.ShimmerAdapter
import np.com.parts.R
import np.com.parts.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {
    private val hideHandler = Handler(Looper.myLooper()!!)


    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    private val viewModel: ProductViewModel by viewModels()
    private lateinit var basicProductAdapter: BasicProductAdapter
    private var gridLayoutManager: GridLayoutManager? = null
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
        val bottomNavigationView = requireActivity().findViewById<SmoothBottomBar>(R.id.bottomBar)
        retainInstance = true

        bottomNavigationView.visibility=View.VISIBLE
        setupRecyclerView()
        setupObservers()
        setupListeners()

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

        basicProductAdapter = BasicProductAdapter()
        val gridLayoutManager = GridLayoutManager(context, 2)

        // Handle the loading item span
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (basicProductAdapter.getItemViewType(position) == LOADING_ITEM_VIEW_TYPE) 2 else 1
            }
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.resetPagination()
            viewModel.loadBasicProducts()
            binding.swipeRefreshLayout.isRefreshing = false
        }

        binding.recyclerView.apply {
            layoutManager = gridLayoutManager
            adapter = basicProductAdapter
            addItemDecoration(GridSpacingItemDecoration(2, 16, true))
            setHasFixedSize(true)

            // Add scroll listener for pagination
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val lastVisibleItemPosition = gridLayoutManager.findLastVisibleItemPosition()
                    val totalItemCount = gridLayoutManager.itemCount

                    if (lastVisibleItemPosition >= totalItemCount - 5) {
                        viewModel.loadBasicProducts()
                    }
                }
            })
        }
    }


    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect products

                viewModel.basicProducts.collect { products ->
                    basicProductAdapter.submitList(products)
                    if (products.isNotEmpty()) {
                        binding.shimmerLayout.root.visibility = View.GONE
                        binding.recyclerView.visibility = View.VISIBLE
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect loading state
                viewModel.isLoadingMore.collect { isLoadingMore ->
                    basicProductAdapter.setLoadingMore(isLoadingMore)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect loading state
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
        basicProductAdapter.setOnItemClickListener { product ->
            // Handle product click
            val action = HomeFragmentDirections
                .actionHomeFragmentToProductFragment(
                    productId = product.basic.productId,
                    productName = product.basic.productName
                )
            findNavController().navigate(action)

        }
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