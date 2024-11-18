package np.com.parts.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import np.com.parts.R

class WarrantyTermsAdapter : ListAdapter<String, WarrantyTermsAdapter.TermViewHolder>(TermDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TermViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_warranty, parent, false)
        return TermViewHolder(view)
    }

    override fun onBindViewHolder(holder: TermViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TermViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val termText: TextView = itemView.findViewById(R.id.termText)

        fun bind(term: String) {
            termText.text = term
        }
    }

    class TermDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}