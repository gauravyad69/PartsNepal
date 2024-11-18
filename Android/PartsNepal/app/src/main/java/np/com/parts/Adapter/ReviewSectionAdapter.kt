package np.com.parts.Adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import np.com.parts.R
import np.com.parts.system.models.Review
import np.com.parts.system.models.Reviews
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class ReviewSectionAdapter(
    private val onWriteReviewClick: () -> Unit
) : RecyclerView.Adapter<ReviewSectionAdapter.ReviewSectionViewHolder>() {

    private var reviews: Reviews? = null

    fun submitData(reviews: Reviews) {
        this.reviews = reviews
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewSectionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.reviews_section, parent, false)
        return ReviewSectionViewHolder(view, onWriteReviewClick)
    }

    override fun onBindViewHolder(holder: ReviewSectionViewHolder, position: Int) {
        reviews?.let { holder.bind(it) }
    }

    override fun getItemCount(): Int = if (reviews != null) 1 else 0

    class ReviewSectionViewHolder(
        itemView: View,
        private val onWriteReviewClick: () -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val averageRatingText: TextView = itemView.findViewById(R.id.averageRatingText)
        private val averageRatingBar: RatingBar = itemView.findViewById(R.id.averageRatingBar)
        private val totalReviewsText: TextView = itemView.findViewById(R.id.totalReviewsText)
        private val reviewsRecyclerView: RecyclerView = itemView.findViewById(R.id.reviewsRecyclerView)
        private val writeReviewButton: MaterialButton = itemView.findViewById(R.id.writeReviewButton)

        // Rating distribution views
        private val fiveStarProgress: ProgressBar = itemView.findViewById(R.id.fiveStarProgress)
        private val fourStarProgress: ProgressBar = itemView.findViewById(R.id.fourStarProgress)
        private val fiveStarCount: TextView = itemView.findViewById(R.id.fiveStarCount)
        private val fourStarCount: TextView = itemView.findViewById(R.id.fourStarCount)

        private val reviewAdapter = ReviewAdapter()

        init {
            writeReviewButton.setOnClickListener { onWriteReviewClick() }
            reviewsRecyclerView.adapter = reviewAdapter
        }

        fun bind(reviews: Reviews) {
            val summary = reviews.summary

            // Set average rating
            averageRatingText.text = String.format("%.1f", summary.averageRating)
            averageRatingBar.rating = summary.averageRating.toFloat()
            totalReviewsText.text = "${summary.totalCount} reviews"

            // Set rating distribution
            val maxCount = summary.distribution.values.maxOrNull() ?: 0
            summary.distribution[5]?.let { count ->
                fiveStarProgress.progress = (count * 100 / maxCount).toDouble().roundToInt()
                fiveStarCount.text = count.toString()
            }
            summary.distribution[4]?.let { count ->
                fourStarProgress.progress = (count * 100 / maxCount).toDouble().roundToInt()
                fourStarCount.text = count.toString()
            }

            // Submit reviews to the nested adapter
            reviewAdapter.submitList(reviews.items)
        }
    }

}

