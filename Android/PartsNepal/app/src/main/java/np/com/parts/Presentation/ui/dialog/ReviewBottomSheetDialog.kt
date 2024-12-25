package np.com.parts.Presentation.ui.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import np.com.parts.Domain.ViewModels.ProductViewModel
import np.com.parts.databinding.BottomDialogReviewBinding

class ReviewBottomSheetDialog(private val productViewModel: ProductViewModel) : BottomSheetDialogFragment() {

    private var _binding: BottomDialogReviewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        _binding = BottomDialogReviewBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)
        var newRating = 5
        binding.ratingBar.setOnRatingBarChangeListener { _, rating, fromUser ->
            if (fromUser) {
                newRating = rating.toInt()
            }
        }
        binding.submitButton.setOnClickListener {
            val review = binding.reviewEditText.text.toString()
            lifecycleScope.launch {

                productViewModel.sendReview(newRating,review) // Emit the value
                dismiss()
            }
        }

        binding.cancelButton.setOnClickListener { dismiss() }

        return dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
