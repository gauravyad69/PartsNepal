package np.com.parts.Presentation.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import np.com.parts.databinding.ItemHomeCarouselBinding

class CarouselAdapter : RecyclerView.Adapter<CarouselAdapter.CarouselViewHolder>() {
    private var images = listOf<String>()

    fun setImages(newImages: List<String>) {
        images = newImages
        notifyDataSetChanged()
    }

    inner class CarouselViewHolder(private val binding: ItemHomeCarouselBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(imageUrl: String) {
            Glide.with(binding.root.context)
                .load(imageUrl)
                .centerCrop()
                .into(binding.carouselImage)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
        val binding = ItemHomeCarouselBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CarouselViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
        holder.bind(images[position])
    }

    override fun getItemCount() = images.size
}

data class CarouselImage(
    val id: String,
    val imageUrl: String
)