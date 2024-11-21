package np.com.parts.Screens.MainScreens

import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
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
import kotlinx.coroutines.launch
import me.ibrahimsn.lib.SmoothBottomBar
import np.com.parts.API.ViewModels.ProductViewModel
import np.com.parts.API.TokenManager
import np.com.parts.Adapter.DeliveryAdapter
import np.com.parts.Adapter.FeatureAdapter
import np.com.parts.Adapter.ReviewAdapter
import np.com.parts.Adapter.WarrantyAdapter
import np.com.parts.R
import np.com.parts.databinding.FragmentProductBinding
import np.com.parts.system.models.Reviews
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem
import java.text.NumberFormat
import kotlin.math.roundToInt


class ProductFragment : Fragment() {
    private val hideHandler = Handler(Looper.myLooper()!!
    )
    private lateinit var deliveryAdapter: DeliveryAdapter
    private lateinit var warrantyAdapter: WarrantyAdapter
    private var featureAdapter = FeatureAdapter()
    private lateinit var reviewAdapter: ReviewAdapter


    private val list = mutableListOf<CarouselItem>()


    private var _binding: FragmentProductBinding? = null
    private val viewModel: ProductViewModel by viewModels()
    private val args: ProductFragmentArgs by navArgs()


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentProductBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bottomNavigationView = requireActivity().findViewById<SmoothBottomBar>(R.id.bottomBar)

        val productId = args.productId
        val productTitle = args.productName
        binding.carousel.registerLifecycle(lifecycle)
        setupRecyclerView()
        setupObservers()



        bottomNavigationView.visibility = View.GONE // Hide the bottom navigation


        viewModel.loadProductsById(productId)

        binding.addToCartButton.setOnClickListener{
        // Check if user is logged in
        if (TokenManager.getInstance(requireContext()).hasToken()) {
            // User is logged in
            Log.i("auth", "user logged in")
            //add to cart function here,
        } else {
            // User is not logged in
            Log.i("auth", "user is not logged in :((((((")
            // Set up Navigation
            val action = ProductFragmentDirections.actionProductFragmentToLoginFragment()
            findNavController().navigate(action)
        }
        }

    }

    override fun onResume() {
        super.onResume()

    }

    override fun onPause() {
        super.onPause()
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)


    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun toggle() {

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun formatPrice(amount: Long, currency: String): String {
        return "$currency ${NumberFormat.getNumberInstance().format(amount)}"
    }

    private fun setupRecyclerView() {
        // Setup RecyclerView
        deliveryAdapter = DeliveryAdapter()
        binding.deliveryOptionsRecyclerView.apply {
            adapter = deliveryAdapter
            layoutManager = LinearLayoutManager(context)
        }

        // Setup warranty adapter
        warrantyAdapter = WarrantyAdapter()
        binding.warrantyTermsRecyclerView.apply {
            adapter = warrantyAdapter
            layoutManager = LinearLayoutManager(context)
        }

        // Setup review adapter
        reviewAdapter = ReviewAdapter()
        binding.reviewsSection.reviewsRecyclerView.apply {
            adapter = reviewAdapter
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
        }

        binding.reviewsSection.writeReviewButton.setOnClickListener {
            // Handle write review click
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.productById.collect { product ->


                    // Update UI with products
                    with(binding) {
                        if (product != null) {
//                            reviewSectionAdapter.submitData(product.details.features.reviews)
                            featureAdapter.submitList(product.details.features.highlights)
                            deliveryAdapter.submitDeliveryInfo(product.details.delivery)
                            warrantyAdapter.submitWarrantyInfo(product.details.warranty)
                            updateReviewSection(product.details.features.reviews)

                            val listOfImages = product.details.features.images ?: emptyList()
                            listOfImages.forEach { picture ->
                                picture.url.let {
                                    list.add(CarouselItem(it))
                                }
                            }

                            carousel.setData(list)

                            productName.text = product.basic.productName

                            // Format and display regular price
                            regularPrice.text = formatPrice(
                                product.basic.pricing.regularPrice.amount,
                                product.basic.pricing.regularPrice.currency
                            )

                            // Handle sale price if available
                            if (product.basic.pricing.isOnSale) {
                                salePrice.apply {
                                    visibility = View.VISIBLE
                                    text = formatPrice(
                                        product.basic.pricing.salePrice?.amount ?: 0,
                                        product.basic.pricing.salePrice?.currency ?: "NPR"
                                    )
                                }
//                            saleChip.visibility = View.VISIBLE
                                regularPrice.apply {
                                    paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                                    setTextColor(
                                        ContextCompat.getColor(
                                            context,
                                            android.R.color.darker_gray
                                        )
                                    )
                                }
                            } else {
                                salePrice.visibility = View.GONE
//                            saleChip.visibility = View.GONE
                                regularPrice.apply {
                                    paintFlags = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                                    setTextColor(
                                        ContextCompat.getColor(
                                            context,
                                            android.R.color.black
                                        )
                                    )
                                }
                            }




                        }

                    }


                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loading.collect { isLoading ->
                    binding.progressBar.isVisible = isLoading
                    binding.scrollView3.isVisible = !isLoading
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

    private fun updateReviewSection(reviews: Reviews) {
        with(binding.reviewsSection) {
            // Update average rating
            averageRatingText.text = String.format("%.1f", reviews.summary.averageRating)
            averageRatingBar.rating = reviews.summary.averageRating.toFloat()
            totalReviewsText.text = "${reviews.summary.totalCount} reviews"

            // Update rating distribution
            val maxCount = reviews.summary.distribution.values.maxOrNull() ?: 0
            if (maxCount > 0) {
                reviews.summary.distribution[5]?.let { count ->
                    fiveStarProgress.progress = (count * 100 / maxCount).toDouble().roundToInt()
                    fiveStarCount.text = count.toString()
                }
                reviews.summary.distribution[4]?.let { count ->
                    fourStarProgress.progress = (count * 100 / maxCount).toDouble().roundToInt()
                    fourStarCount.text = count.toString()
                }
                // Add similar blocks for 3,2,1 stars
            }

            // Update review items
            reviewAdapter.submitList(reviews.items)
        }
    }
}

