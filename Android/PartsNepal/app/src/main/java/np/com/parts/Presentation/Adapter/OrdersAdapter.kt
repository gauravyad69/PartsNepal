package np.com.parts.Presentation.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import np.com.parts.API.Models.OrderModel
import np.com.parts.API.Models.formattedDate
import np.com.parts.API.Models.formattedTotal
import np.com.parts.databinding.ItemOrderBinding

class OrdersAdapter(
    private val onOrderClicked: (OrderModel) -> Unit
) : ListAdapter<OrderModel, OrdersAdapter.OrderViewHolder>(OrderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        return OrderViewHolder(
            ItemOrderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class OrderViewHolder(
        private val binding: ItemOrderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(order: OrderModel) {
            binding.apply {
                orderNumber.text = "Order #${order.orderNumber}"
                orderDate.text = order.formattedDate()
                itemCount.text = "${order.items.size} items"
                totalAmount.text = order.formattedTotal()
                orderStatus.text = order.status.toString()
                    .replace("_", " ")
                    .lowercase()
                    .capitalize()

                root.setOnClickListener {
                    onOrderClicked(order)
                }
            }
        }
    }

    private class OrderDiffCallback : DiffUtil.ItemCallback<OrderModel>() {
        override fun areItemsTheSame(oldItem: OrderModel, newItem: OrderModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: OrderModel, newItem: OrderModel): Boolean {
            return oldItem == newItem
        }
    }
} 