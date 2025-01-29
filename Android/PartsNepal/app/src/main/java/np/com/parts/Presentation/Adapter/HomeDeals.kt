package np.com.parts.Presentation.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import np.com.parts.API.Models.Money
import np.com.parts.databinding.ItemHomeDealsBinding


// DealsAdapter.kt
class DealsAdapter : RecyclerView.Adapter<DealsAdapter.DealViewHolder>() {
    private var deals = listOf<Deal>()
    var onDealClick: ((Deal) -> Unit)? = null

    fun setDeals(newDeals: List<Deal>) {
        deals = newDeals
        notifyDataSetChanged()
    }

    inner class DealViewHolder(private val binding: ItemHomeDealsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(deal: Deal) {
            binding.apply {
                dealTitle.text = deal.title
                dealPrice.text = "Rs ${deal.price}"
                dealDiscount.text = "${deal.discount}% OFF"

                Glide.with(root.context)
                    .load(deal.imageUrl)
                    .centerCrop()
                    .into(dealImage)

                root.setOnClickListener { onDealClick?.invoke(deal) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DealViewHolder {
        val binding = ItemHomeDealsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DealViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DealViewHolder, position: Int) {
        holder.bind(deals[position])
    }

    override fun getItemCount() = deals.size
}

// Data class for deals
data class Deal(
    val id: String,
    val title: String,
    val price: Money,
    val discount: Money,
    val imageUrl: String
)