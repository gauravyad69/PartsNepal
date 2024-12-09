package np.com.parts.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import np.com.parts.databinding.ItemCartBinding
import np.com.parts.API.Models.LineItem
import np.com.parts.API.Models.formatted

class CartAdapter(
    private val onQuantityChanged: (LineItem, Int) -> Unit,
    private val onRemoveClicked: (LineItem) -> Unit
) : ListAdapter<LineItem, CartAdapter.CartViewHolder>(CartDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CartViewHolder(private val binding: ItemCartBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: LineItem) {
            binding.apply {
                productName.text = item.name
                productPrice.text = item.unitPrice.formatted()
                quantityText.text = item.quantity.toString()
                
                item.imageUrl?.let {
                    productImage.load(it) {
                        crossfade(true)
                    }
                }

                // Quantity controls
                decreaseButton.setOnClickListener {
                    if (item.quantity > 1) {
                        onQuantityChanged(item, item.quantity - 1)
                    }
                }

                increaseButton.setOnClickListener {
                    onQuantityChanged(item, item.quantity + 1)
                }

                removeButton.setOnClickListener {
                    onRemoveClicked(item)
                }
            }
        }
    }
}


    private class CartDiffCallback : DiffUtil.ItemCallback<LineItem>() {
        override fun areItemsTheSame(oldItem: LineItem, newItem: LineItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: LineItem, newItem: LineItem): Boolean {
            return oldItem == newItem
        }
    }
