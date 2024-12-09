package np.com.parts.Items

import android.view.LayoutInflater
import android.view.ViewGroup
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import np.com.parts.databinding.ItemLoadingBinding

class ProgressItem : AbstractBindingItem<ItemLoadingBinding>() {
    override val type: Int = hashCode()

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): ItemLoadingBinding {
        return ItemLoadingBinding.inflate(inflater, parent, false)
    }

    override fun bindView(binding: ItemLoadingBinding, payloads: List<Any>) {
        // Nothing to bind for progress indicator
    }
} 