package np.com.parts.Presentation.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import np.com.parts.API.Models.BasicProductView
import np.com.parts.API.Models.ProductModel
import np.com.parts.R
import np.com.parts.databinding.ItemHotDealsSectionBinding

class HotDealsItem(
    private val products: List<ProductModel>,
    private val onProductClick: (ProductModel) -> Unit
) : AbstractBindingItem<ItemHotDealsSectionBinding>() {
    override val type: Int = R.id.fastadapter_hot_deals_item_id

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): ItemHotDealsSectionBinding {
        return ItemHotDealsSectionBinding.inflate(inflater, parent, false)
    }

    override fun bindView(binding: ItemHotDealsSectionBinding, payloads: List<Any>) {
        binding.dealsRecyclerView.apply {
            if (layoutManager == null) {
                layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            }
            
            if (adapter == null) {
                adapter = HotDealsAdapter(products, onProductClick)
            } else {
                (adapter as HotDealsAdapter).updateProducts(products)
            }
        }
    }
} 