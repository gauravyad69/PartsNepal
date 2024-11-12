package xyz.illuminate.tradersarena.Screens.PaymentScreens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import xyz.illuminate.tradersarena.R
import xyz.illuminate.tradersarena.databinding.FragmentPackageSelectionBinding

class PackageSelectionFragment : Fragment() {

    private var _binding: FragmentPackageSelectionBinding? = null
    private val binding get() = _binding!!

    private var containerPackageSelected = false
    private var packageSelected = 0
    private var containerTierSelected = false
    private var tierSelected = 0
    private var containerDurationSelected = false
    private var durationSelected = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPackageSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val colorWhite = ContextCompat.getColor(requireContext(), R.color.white)
        val defaultStrokeColor = binding.containerPackageOne.strokeColorStateList?.defaultColor ?: 0

        val slideInFromBottom = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_fade_from_bottom)
        val slideOutFromBottom = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_fade_to_bottom)
        val fadein = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        val fadeout = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_out)

        setupPackageClickListeners(colorWhite, defaultStrokeColor, slideInFromBottom, slideOutFromBottom)
        setupTierClickListeners(colorWhite, defaultStrokeColor, slideInFromBottom, slideOutFromBottom)
        setupDurationClickListeners(colorWhite, defaultStrokeColor, slideInFromBottom, fadein, fadeout)
    }

    private fun setupPackageClickListeners(colorWhite: Int, defaultStrokeColor: Int, slideInFromBottom: android.view.animation.Animation, slideOutFromBottom: android.view.animation.Animation) {
        fun handlePackageClick(clickedContainer: MaterialCardView, selectedPackage: Int) {
            if (!containerPackageSelected) {
                containerPackageSelected = true
                packageSelected = selectedPackage
                val packageText = when (selectedPackage) {
                    1 -> "Intraday"
                    2 -> "Quotex"
                    3 -> "Binary"
                    else -> ""
                }
                binding.tvTierText.append(packageText)
                binding.tvDurationText.append(packageText)

                clickedContainer.strokeColor = colorWhite
                binding.containerTier.apply {
                    visibility = View.VISIBLE
                    startAnimation(slideInFromBottom)
                }
                setVisibilityForOtherContainers(clickedContainer, View.GONE)
            } else {
                packageSelected = 0
                containerPackageSelected = false
                clickedContainer.strokeColor = defaultStrokeColor

                binding.tvTierText.text="Select a tier for "
                binding.tvDurationText.text="Select duration for "

                binding.containerTier.apply {
                    startAnimation(slideOutFromBottom)
                    visibility = View.GONE
                }
                binding.containerDuration.apply {
                    startAnimation(slideOutFromBottom)
                    visibility = View.GONE
                }
                setVisibilityForOtherContainers(clickedContainer, View.VISIBLE)
            }
        }

        binding.containerPackageOne.setOnClickListener { handlePackageClick(binding.containerPackageOne, 1) }
        binding.containerPackageTwo.setOnClickListener { handlePackageClick(binding.containerPackageTwo, 2) }
        binding.containerPackageThree.setOnClickListener { handlePackageClick(binding.containerPackageThree, 3) }
    }

    private fun setupTierClickListeners(colorWhite: Int, defaultStrokeColor: Int, slideInFromBottom: android.view.animation.Animation, slideOutFromBottom: android.view.animation.Animation) {
        fun handleTierClick(clickedContainer: MaterialCardView, selectedTier: Int) {
            if (!containerTierSelected) {
                containerTierSelected = true
                tierSelected = selectedTier
                clickedContainer.strokeColor = colorWhite
                binding.containerDuration.apply {
                    visibility = View.VISIBLE
                    startAnimation(slideInFromBottom)
                }
                setVisibilityForOtherTiers(clickedContainer, View.GONE)
            } else {
                tierSelected = 0
                containerTierSelected = false
                clickedContainer.strokeColor = defaultStrokeColor
                binding.containerDuration.apply {
                    startAnimation(slideOutFromBottom)
                    visibility = View.GONE
                }
                setVisibilityForOtherTiers(clickedContainer, View.VISIBLE)
            }
        }

        binding.containerGold.setOnClickListener { handleTierClick(binding.containerGold, 1) }
        binding.containerSilver.setOnClickListener { handleTierClick(binding.containerSilver, 2) }
        binding.containerBronze.setOnClickListener { handleTierClick(binding.containerBronze, 3) }
    }

    private fun setupDurationClickListeners(colorWhite: Int, defaultStrokeColor: Int, slideInFromBottom: android.view.animation.Animation, fadein: android.view.animation.Animation, fadeout: android.view.animation.Animation) {
        fun handleDurationClick(clickedContainer: MaterialCardView, selectedDuration: Int) {
            if (!containerDurationSelected) {
                containerDurationSelected = true
                durationSelected = selectedDuration
                clickedContainer.strokeColor = colorWhite

                binding.btnContinue.apply {
                    visibility=View.VISIBLE
                    startAnimation(fadein)
                }

                binding.containerDuration.apply {
                    visibility = View.VISIBLE
                    startAnimation(slideInFromBottom)
                }
                setVisibilityForOtherDuration(clickedContainer, View.GONE)
            } else {
                durationSelected = 0
                containerDurationSelected = false
                clickedContainer.strokeColor = defaultStrokeColor

                setVisibilityForOtherDuration(clickedContainer, View.VISIBLE)
                binding.btnContinue.apply {
                    visibility=View.GONE
                    startAnimation(fadeout)
                }
            }
        }

        binding.container1month.setOnClickListener { handleDurationClick(binding.container1month, 1) }
        binding.container1week.setOnClickListener { handleDurationClick(binding.container1week, 2) }
        binding.container1day.setOnClickListener { handleDurationClick(binding.container1day, 3) }
    }

    private fun setVisibilityForOtherDuration(clickedContainer: View, visibility: Int) {
        setVisibilityWithAnimation(clickedContainer, binding.container1month, visibility)
        setVisibilityWithAnimation(clickedContainer, binding.container1week, visibility)
        setVisibilityWithAnimation(clickedContainer, binding.container1day, visibility)
    }

    private fun setVisibilityForOtherContainers(clickedContainer: View, visibility: Int) {
        setVisibilityWithAnimation(clickedContainer, binding.containerPackageOne, visibility)
        setVisibilityWithAnimation(clickedContainer, binding.containerPackageTwo, visibility)
        setVisibilityWithAnimation(clickedContainer, binding.containerPackageThree, visibility)
    }

    private fun setVisibilityForOtherTiers(clickedContainer: View, visibility: Int) {
        setVisibilityWithAnimation(clickedContainer, binding.containerGold, visibility)
        setVisibilityWithAnimation(clickedContainer, binding.containerSilver, visibility)
        setVisibilityWithAnimation(clickedContainer, binding.containerBronze, visibility)
    }

    private fun setVisibilityWithAnimation(clickedContainer: View, container: View, visibility: Int) {
        if (clickedContainer != container) {
            val animation = if (visibility == View.GONE) {
                AnimationUtils.loadAnimation(requireContext(), R.anim.fade_out)
            } else {
                AnimationUtils.loadAnimation(requireContext(), R.anim.slide_from_bottom)
            }
            container.startAnimation(animation)
            container.visibility = visibility
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}