package np.com.parts.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import np.com.parts.API.Models.LineItem
import np.com.parts.API.Models.formatted
import np.com.parts.R
import np.com.parts.databinding.ItemOrderLineBinding

class OrderItemsAdapter : ListAdapter<LineItem, OrderItemsAdapter.OrderItemViewHolder>(OrderItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderItemViewHolder {
        return OrderItemViewHolder(
            ItemOrderLineBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: OrderItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class OrderItemViewHolder(
        private val binding: ItemOrderLineBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: LineItem) {
            binding.apply {
                productName.text = item.name
                quantityText.text = "×${item.quantity}"
                unitPriceText.text = item.unitPrice.formatted()
                totalPriceText.text = item.totalPrice.formatted()
                
                item.imageUrl?.let {
                    productImage.load(it) {
                        crossfade(true)
                        placeholder(R.drawable.bgmi)
                        error(R.drawable.bgmi)
                    }
                } ?: productImage.setImageResource(R.drawable.bgmi)

                // Show discount if available
                item.discount?.let { discount ->
                    discountText.apply {
                        text = "-${discount.amount.formatted()}"
                        visibility = View.VISIBLE
                    }
                } ?: run {
                    discountText.visibility = View.GONE
                }
            }
        }
    }

    private class OrderItemDiffCallback : DiffUtil.ItemCallback<LineItem>() {
        override fun areItemsTheSame(oldItem: LineItem, newItem: LineItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: LineItem, newItem: LineItem): Boolean {
            return oldItem == newItem
        }
    }
} 