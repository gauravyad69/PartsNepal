package np.com.parts.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import np.com.parts.R

class ProductShimmerAdapter : RecyclerView.Adapter<ProductShimmerAdapter.ShimmerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShimmerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_product_shimmer, parent, false)
        return ShimmerViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShimmerViewHolder, position: Int) {
        // Nothing to bind for shimmer
    }

    override fun getItemCount(): Int = 1 // Only need one shimmer for the product view

    class ShimmerViewHolder(view: View) : RecyclerView.ViewHolder(view)
} 