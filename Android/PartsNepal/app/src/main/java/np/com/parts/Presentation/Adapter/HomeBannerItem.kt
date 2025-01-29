package np.com.parts.Presentation.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import np.com.parts.R
import np.com.parts.databinding.ItemHomeBannerBinding
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem

class HomeBannerItem(private val bannerImages: List<CarouselItem>) : AbstractBindingItem<ItemHomeBannerBinding>() {
    override val type: Int = R.id.fastadapter_banner_item_id

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): ItemHomeBannerBinding {
        return ItemHomeBannerBinding.inflate(inflater, parent, false)
    }

    override fun bindView(binding: ItemHomeBannerBinding, payloads: List<Any>) {
        binding.carousel.setData(bannerImages)
    }
} 