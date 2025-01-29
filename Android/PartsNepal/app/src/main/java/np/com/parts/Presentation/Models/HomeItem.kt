package np.com.parts.Presentation.Models

import np.com.parts.API.Models.BasicProductView
import np.com.parts.API.Models.ProductModel
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem

sealed class HomeItem {
    data class BannerItem(val bannerImages: List<CarouselItem>) : HomeItem()
    data class HotDealsSection(val products: List<ProductModel>) : HomeItem()
    data class RegularProduct(val product: BasicProductView) : HomeItem()
} 