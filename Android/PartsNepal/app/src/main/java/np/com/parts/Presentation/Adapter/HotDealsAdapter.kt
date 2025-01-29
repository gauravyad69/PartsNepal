package np.com.parts.Presentation.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import np.com.parts.API.Models.BasicProductView
import np.com.parts.API.Models.formatPrice
import np.com.parts.R
import np.com.parts.databinding.ItemHomeDealsBinding

class HotDealsAdapter(
    private var products: List<BasicProductView>,
    private val onProductClick: (BasicProductView) -> Unit
) : RecyclerView.Adapter<HotDealsAdapter.DealViewHolder>() {

    inner class DealViewHolder(private val binding: ItemHomeDealsBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(product: BasicProductView) {
            binding.apply {
                dealTitle.text = product.basic.productName
                dealPrice.text = formatPrice(
                    product.basic.pricing.regularPrice.amount,
                    product.basic.pricing.regularPrice.currency
                )
                
              /*  // Calculate and show discount if on sale
                if (product.basic.pricing.isOnSale) {
                    val discount = ((1 - (product.basic.pricing.salePrice?.amount ?: 0.0) /
                        product.basic.pricing.regularPrice.amount) * 100).toInt()
                    dealDiscount.text = "-${discount}%"
                }*/

                // Load image
                dealImage.load(product.basic.inventory.mainImage) {
                    crossfade(true)
                    placeholder(R.drawable.plan_premium)
                    error(R.drawable.ic_close)
                }

                root.setOnClickListener { onProductClick(product) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DealViewHolder {
        return DealViewHolder(
            ItemHomeDealsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: DealViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    fun updateProducts(newProducts: List<BasicProductView>) {
        products = newProducts
        notifyDataSetChanged()
    }
} 