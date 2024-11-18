package np.com.parts.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import np.com.parts.R
import np.com.parts.system.models.Review
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReviewAdapter : ListAdapter<Review, ReviewAdapter.ReviewViewHolder>(ReviewDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userName: TextView = itemView.findViewById(R.id.userName)
        private val userAvatar: ImageView = itemView.findViewById(R.id.userAvatar)
        private val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        private val reviewDate: TextView = itemView.findViewById(R.id.reviewDate)
        private val reviewComment: TextView = itemView.findViewById(R.id.reviewComment)

        fun bind(review: Review) {
            userName.text = "User ${review.userId.takeLast(4)}" // Simplified user display
            ratingBar.rating = review.rating.toFloat()
            reviewComment.text = review.comment
            reviewDate.text = formatDate(review.lastUpdated)

            // You might want to load actual user avatar here
            // userAvatar.loadImage(...)
        }

        private fun formatDate(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diffInMillis = now - timestamp

            return when {
                diffInMillis < 24 * 60 * 60 * 1000 -> "Today"
                diffInMillis < 48 * 60 * 60 * 1000 -> "Yesterday"
                else -> {
                    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    sdf.format(Date(timestamp))
                }
            }
        }
    }

    class ReviewDiffCallback : DiffUtil.ItemCallback<Review>() {
        override fun areItemsTheSame(oldItem: Review, newItem: Review): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Review, newItem: Review): Boolean {
            return oldItem == newItem
        }
    }
}