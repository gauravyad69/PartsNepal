package np.com.parts.Items

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import coil.load
import np.com.parts.R
import np.com.parts.databinding.ItemProductBinding
import np.com.parts.API.Models.BasicProductView
import java.text.NumberFormat

class BasicProductItem(val product: BasicProductView) : AbstractBindingItem<ItemProductBinding>() {
    override val type: Int = R.id.fastadapter_product_item_id

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): ItemProductBinding {
        return ItemProductBinding.inflate(inflater, parent, false)
    }

    override fun bindView(binding: ItemProductBinding, payloads: List<Any>) {
        binding.apply {
            productName.text = product.basic.productName
            productType.text = product.basic.productType

            // Regular price
            regularPrice.text = formatPrice(
                product.basic.pricing.regularPrice.amount,
                product.basic.pricing.regularPrice.currency
            )

            // Handle sale price
            if (product.basic.pricing.isOnSale) {
                salePrice.visibility = View.VISIBLE
                salePrice.text = formatPrice(
                    product.basic.pricing.salePrice?.amount ?: 0,
                    product.basic.pricing.salePrice?.currency ?: "NPR"
                )
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

            // Load image
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