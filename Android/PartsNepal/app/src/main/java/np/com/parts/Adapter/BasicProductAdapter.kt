package np.com.parts.Adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import np.com.parts.R
import np.com.parts.databinding.ItemProductBinding
import np.com.parts.system.models.BasicProductView
import java.text.NumberFormat

class BasicProductAdapter : ListAdapter<BasicProduct, RecyclerView.ViewHolder>(ProductDiffCallback()) {
    init {
        setHasStableIds(true)
    }


    companion object {
        const val LOADING_ITEM_VIEW_TYPE = 1
        const val PRODUCT_ITEM_VIEW_TYPE = 2
    }

    private var isLoadingMore = false

    fun setLoadingMore(loading: Boolean) {
        if (isLoadingMore != loading) {
            isLoadingMore = loading
            if (loading) {
                notifyItemInserted(itemCount)
            } else {
                notifyItemRemoved(itemCount)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == itemCount - 1 && isLoadingMore) {
            LOADING_ITEM_VIEW_TYPE
        } else {
            PRODUCT_ITEM_VIEW_TYPE
        }
    }

    private var products = listOf<BasicProductView>()
    private var onItemClickListener: ((BasicProductView) -> Unit)? = null

    fun setOnItemClickListener(listener: (BasicProductView) -> Unit) {
        onItemClickListener = listener
    }

    fun submitList(newProducts: List<BasicProductView>) {
        products = newProducts
        notifyDataSetChanged() // Consider using DiffUtil for better performance
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        return when (viewType) {
            LOADING_ITEM_VIEW_TYPE -> LoadingViewHolder.create(parent)
            else -> ProductViewHolder.create(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ProductViewHolder -> {
                val product = getItem(position)
                holder.bind(product)
            }

            is LoadingViewHolder -> {
                // Loading view doesn't need binding
            }
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + if (isLoadingMore) 1 else 0
    }


    inner class ProductViewHolder(
        private val binding: ItemProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.invoke(products[position])
                }
            }



        }

        fun bind(product: BasicProductView) {
            with(binding) {
                productName.text = product.basic.productName
                productType.text = product.basic.productType

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
                    saleChip.visibility = View.VISIBLE
                    regularPrice.apply {
                        paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                        setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                    }
                } else {
                    salePrice.visibility = View.GONE
                    saleChip.visibility = View.GONE
                    regularPrice.apply {
                        paintFlags = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                        setTextColor(ContextCompat.getColor(context, android.R.color.black))
                    }
                }

                // Load image using Coil
                productImage.load(product.basic.inventory.mainImage) {
                    crossfade(true)
                    placeholder(R.drawable.bgmi)
                    error(R.drawable.plan_pro)
                }
            }
        }

        private fun formatPrice(amount: Long, currency: String): String {
            return "$currency ${NumberFormat.getNumberInstance().format(amount)}"
        }
    }
}

class LoadingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    companion object {
        fun create(parent: ViewGroup): LoadingViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_loading, parent, false)
            return LoadingViewHolder(view)
        }
    }
}