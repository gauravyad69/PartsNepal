package np.com.parts.Screens.ProductScreen

import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import np.com.parts.ViewModels.ProductViewModel
import np.com.parts.Adapter.DeliveryAdapter
import np.com.parts.Adapter.FeatureAdapter
import np.com.parts.Adapter.ReviewAdapter
import np.com.parts.Adapter.WarrantyAdapter
import np.com.parts.R
import np.com.parts.databinding.FragmentProductBinding
import np.com.parts.API.Models.Reviews
import np.com.parts.ViewModels.CartAction
import np.com.parts.ViewModels.CartViewModel
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem
import java.text.NumberFormat
import kotlin.math.roundToInt
import dagger.hilt.android.AndroidEntryPoint
import np.com.parts.API.Models.formatPrice

@AndroidEntryPoint
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
    private val cartViewModel: CartViewModel by viewModels()
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
        val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.bottomBar)

        val productId = args.productId
        val productTitle = args.productName
        binding.carousel.registerLifecycle(lifecycle)
        setupRecyclerView()
        setupObservers()



        bottomNavigationView.visibility = View.GONE // Hide the bottom navigation


        viewModel.loadProductsById(productId)

        setupUI()
        observeCartEvents()
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



    private fun observeCartEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            cartViewModel.cartEvents.collect { event ->
                when (event) {
                    is CartEvent.ItemAdded -> {
                        // Reset button state
                        binding.addToCartButton.isEnabled = true
                        binding.addToCartProgress.isVisible = false
                        binding.addToCartButton.text = getString(R.string.add_to_cart)

                        // Show success animation
                        showAddToCartSuccess()

                        // Show snackbar
                        Snackbar.make(
                            binding.root,
                            "${event.name} added to cart",
                            Snackbar.LENGTH_LONG
                        ).apply {
                            setAction("VIEW CART") {
                                findNavController().navigate(R.id.action_productFragment_to_cartFragment)
                            }
                            // Optional: Custom styling
                            setActionTextColor(resources.getColor(R.color.primary, null))
                            show()
                        }
                    }
                    is CartEvent.ShowMessage -> {
                        // Reset button state
                        binding.addToCartButton.isEnabled = true
                        binding.addToCartProgress.isVisible = false
                        binding.addToCartButton.text = getString(R.string.add_to_cart)

                        // Show error
                        Snackbar.make(binding.root, event.message, Snackbar.LENGTH_LONG).show()
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun showAddToCartSuccess() {
        binding.addToCartSuccess.apply {
            alpha = 0f
            scaleX = 0f
            scaleY = 0f
            isVisible = true
            
            animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .withEndAction {
                    postDelayed({
                        animate()
                            .alpha(0f)
                            .scaleX(0f)
                            .scaleY(0f)
                            .setDuration(200)
                            .withEndAction {
                                isVisible = false
                            }
                    }, 1000)
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

    private fun setupUI() {
        binding.apply {
            // Assuming you have a quantity input in your layout
            // This could be a NumberPicker, EditText, or custom view
            addToCartButton.setOnClickListener {
                val product = viewModel.productById.value ?: return@setOnClickListener
                val quantity = 1 // Or get from your quantity input

                // Show loading state
                addToCartButton.isEnabled = false
                addToCartProgress.isVisible = true

                cartViewModel.dispatch(
                    CartAction.AddItem(
                        productId = product.productId,
                        quantity = quantity,
                        product = product
                    )
                )
            }
        }
    }


}

