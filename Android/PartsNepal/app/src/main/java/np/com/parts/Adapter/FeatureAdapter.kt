package np.com.parts.Adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import np.com.parts.R

class FeatureAdapter : ListAdapter<String, FeatureAdapter.FeatureViewHolder>(FeatureDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeatureViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_feature, parent, false)
        return FeatureViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeatureViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class FeatureViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val featureText: TextView = itemView.findViewById(R.id.featureText)

        fun bind(feature: String) {
            featureText.text = feature
        }
    }

    class FeatureDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}