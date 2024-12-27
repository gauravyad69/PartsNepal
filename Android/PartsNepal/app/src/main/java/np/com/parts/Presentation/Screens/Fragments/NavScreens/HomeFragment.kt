package np.com.parts.Presentation.Screens.Fragments.NavScreens

import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import np.com.parts.Domain.ViewModels.ProductViewModel
import np.com.parts.Presentation.Adapter.ShimmerAdapter
import np.com.parts.R
import np.com.parts.databinding.FragmentHomeBinding
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import np.com.parts.Domain.Items.BasicProductItem
import np.com.parts.Domain.Items.ProgressItem
import np.com.parts.Domain.ViewModels.MiscViewModel
import np.com.parts.Presentation.Adapter.CarouselAdapter
import np.com.parts.Presentation.Adapter.Deal
import np.com.parts.Presentation.Adapter.DealsAdapter
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem

@AndroidEntryPoint
class HomeFragment : Fragment() {


    private val carouselHandler = Handler(Looper.getMainLooper())
    private val carouselRunnable = object : Runnable {
        override fun run() {
            binding.viewPager.currentItem =
                (binding.viewPager.currentItem + 1) % (carouselAdapter.itemCount)
            carouselHandler.postDelayed(this, 3000)
        }
    }

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    private val viewModel: ProductViewModel by viewModels()
    private lateinit var fastAdapter: FastAdapter<AbstractBindingItem<*>>
    private lateinit var itemAdapter: ItemAdapter<BasicProductItem>
    private lateinit var footerAdapter: ItemAdapter<ProgressItem>
    private lateinit var dealsAdapter: DealsAdapter
    private lateinit var carouselAdapter: CarouselAdapter
    private lateinit var shimmerAdapter: ShimmerAdapter
    private var carouselJob: Job? = null



    private val miscViewModel: MiscViewModel by viewModels()

    private val list = mutableListOf<CarouselItem>()


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
        val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.bottomBar)
        retainInstance = true
        initializeDealsAndCarousel()



        Snackbar.make(binding.root, "Please Wait...", Snackbar.LENGTH_LONG)
        Snackbar.make(binding.root, "The Products Are Being Loaded...", Snackbar.LENGTH_LONG)



        // Test data to verify adapter
        val testDeals = listOf(
            Deal("1", "Test Item 1", 999.0, 15, "https://example.com/1.jpg"),
            Deal("2", "Test Item 2", 1999.0, 20, "https://example.com/2.jpg")
        )
        dealsAdapter.setDeals(testDeals)


        bottomNavigationView.visibility=View.VISIBLE
        setupRecyclerView()
        setupObservers()
        setupListeners()
        setupShimmer()
        setupObserversForMisc()


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
                    val action =
                        HomeFragmentDirections.Companion.actionHomeFragmentToProductFragment(
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
                        binding.swipeRefreshLayout.visibility = View.VISIBLE
                    }
                }
            }
        }

     viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    // Observe carousel images
                    viewModel.carouselImages.collect { images ->
                        carouselAdapter.setImages(images.map { it.imageUrl })
                    }

                    // Observe deals
                    viewModel.deals.collect { deals ->
                        dealsAdapter.setDeals(deals)
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
                        binding.swipeRefreshLayout.visibility = View.GONE
                    } else {
                        binding.shimmerLayout.root.visibility = View.GONE
                        binding.swipeRefreshLayout.visibility = View.VISIBLE
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

    private fun setupObserversForMisc() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                miscViewModel.homePageCarrousel.collect { items ->

                    val listOfImages = items ?: emptyList()
                    listOfImages.forEach { items ->
                            list.add(CarouselItem(items.imageUrl, caption = items.caption))
                    }

//                    binding.carousel.setData(list)

                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                miscViewModel.loading.collect { isLoading ->
                    if (isLoading) {
                        binding.progressBar2.visibility=View.VISIBLE
//                        binding.carousel.visibility=View.GONE
                    } else {
                        binding.progressBar2.visibility=View.GONE
//                        binding.carousel.visibility=View.VISIBLE
                    }
                }
            }
        }


        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                miscViewModel.error.collect { error ->
                    error?.let {
                        Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }

    }

    private fun initializeDealsAndCarousel() {
        startCarousel()
        // Initialize Carousel
        carouselAdapter = CarouselAdapter()
        dealsAdapter = DealsAdapter()
        binding.viewPager.apply {
            adapter = carouselAdapter
            orientation = ViewPager2.ORIENTATION_HORIZONTAL

            // Reduce sensitivity for smoother scrolling
            val recyclerViewField = ViewPager2::class.java.getDeclaredField("mRecyclerView")
            recyclerViewField.isAccessible = true
            val recyclerView = recyclerViewField.get(this) as RecyclerView
            recyclerView.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }

        // Connect dots indicator
        binding.dotsIndicator.attachTo(binding.viewPager)

        // Initialize Deals
//        dealsAdapter = DealsAdapter().apply {
//            onDealClick = { deal ->
//                navigateToDealDetails(deal)
//            }
//        }

        binding.dealsRecyclerView.apply {
            adapter = dealsAdapter
            layoutManager = LinearLayoutManager(
                context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            // Add padding for better visibility of next items
            setPadding(16, 0, 16, 0)
            clipToPadding = false
        }
    }

    private fun startCarouselAutoScroll() {
        carouselJob = lifecycleScope.launch {
            while(isActive) {
                delay(3000)
                binding.viewPager.let { viewPager ->
                    val nextItem = (viewPager.currentItem + 1) % carouselAdapter.itemCount
                    viewPager.setCurrentItem(nextItem, true)
                }
            }
        }
    }
   /* private fun navigateToDealDetails(deal: Deal) {
        findNavController().navigate(
            HomeFragmentDirections.actionHomeFragmentToDealDetails(deal.id)
        )
    }
*/

    private fun setupListeners() {
        // Remove the old listener setup and use the one in setupRecyclerView()
    }
    private fun startCarousel() {
        carouselHandler.postDelayed(carouselRunnable, 3000)
    }
    override fun onPause() {
        super.onPause()
        carouselHandler.removeCallbacks(carouselRunnable)
    }

    override fun onResume() {
        super.onResume()
        startCarousel()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        carouselHandler.removeCallbacks(carouselRunnable)
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