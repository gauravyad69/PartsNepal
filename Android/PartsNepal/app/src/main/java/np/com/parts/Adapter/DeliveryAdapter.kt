package np.com.parts.Adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import np.com.parts.R
import np.com.parts.databinding.ItemWarrantyBinding
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

