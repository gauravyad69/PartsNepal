package np.com.parts.Adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import np.com.parts.R
import np.com.parts.system.models.DeliveryInfo
import np.com.parts.system.models.DeliveryOption
import np.com.parts.system.models.WarrantyInfo

class DeliveryAdapter : ListAdapter<DeliveryOption, DeliveryAdapter.DeliveryViewHolder>(DeliveryDiffCallback()) {
    private var deliveryInfo: DeliveryInfo? = null

    fun submitDeliveryInfo(info: DeliveryInfo) {
        deliveryInfo = info
        submitList(info.options.toList())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeliveryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_delivery, parent, false)
        return DeliveryViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeliveryViewHolder, position: Int) {
        holder.bind(getItem(position), deliveryInfo)
    }

    class DeliveryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val deliveryOptionTitle: TextView = itemView.findViewById(R.id.deliveryOptionTitle)
        private val estimatedDays: TextView = itemView.findViewById(R.id.estimatedDays)
        private val shippingCost: TextView = itemView.findViewById(R.id.shippingCost)

        fun bind(option: DeliveryOption, deliveryInfo: DeliveryInfo?) {
            deliveryOptionTitle.text = getFormattedTitle(option)

            deliveryInfo?.let {
                estimatedDays.text = "Estimated delivery: ${it.estimatedDays} days"
                shippingCost.text = "Shipping Cost: ${it.shippingCost.currency} ${it.shippingCost.amount}"
            }
        }

        private fun getFormattedTitle(option: DeliveryOption): String {
            return when (option) {
                DeliveryOption.STORE_PICKUP -> "Store Pickup"
                DeliveryOption.STANDARD_DELIVERY -> "Standard Delivery"
                DeliveryOption.EXPRESS_DELIVERY -> "Express Delivery"
                DeliveryOption.INTERNATIONAL_SHIPPING -> "International Shipping"
            }
        }
    }

    class DeliveryDiffCallback : DiffUtil.ItemCallback<DeliveryOption>() {
        override fun areItemsTheSame(oldItem: DeliveryOption, newItem: DeliveryOption): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: DeliveryOption, newItem: DeliveryOption): Boolean {
            return oldItem == newItem
        }
    }
}

class WarrantyAdapter : RecyclerView.Adapter<WarrantyAdapter.WarrantyViewHolder>() {
    private var warrantyInfo: WarrantyInfo? = null
    private val termsAdapter = WarrantyTermsAdapter()

    fun submitWarrantyInfo(info: WarrantyInfo) {
        warrantyInfo = info
        termsAdapter.submitList(info.terms)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WarrantyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_warranty, parent, false)
        return WarrantyViewHolder(view)
    }

    override fun onBindViewHolder(holder: WarrantyViewHolder, position: Int) {
        warrantyInfo?.let { holder.bind(it) }
    }

    override fun getItemCount(): Int = if (warrantyInfo != null) 1 else 0

    class WarrantyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val warrantyPeriod: TextView = itemView.findViewById(R.id.warrantyPeriod)
        private val returnPeriod: TextView = itemView.findViewById(R.id.returnPolicy)
        private val warrantyTermsRecyclerView: RecyclerView = itemView.findViewById(R.id.warrantyTermsRecyclerView)
        private val termsAdapter = WarrantyTermsAdapter()

        init {
            warrantyTermsRecyclerView.adapter = termsAdapter
        }

        fun bind(info: WarrantyInfo) {
            warrantyPeriod.text = "${info.warrantyMonths} Months Warranty"
            returnPeriod.text = if (info.isReturnable) {
                "${info.returnPeriodDays} Days Return"
            } else {
                "No Returns"
            }
            termsAdapter.submitList(info.terms)
        }
    }
}

