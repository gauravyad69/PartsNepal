package np.com.parts.Presentation.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import np.com.parts.R

class ShimmerAdapter : RecyclerView.Adapter<ShimmerAdapter.ShimmerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShimmerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_basic_product_shimmer, parent, false)
        return ShimmerViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShimmerViewHolder, position: Int) {
        // Nothing to bind as it's just a shimmer effect
    }

    override fun getItemCount(): Int = 6 // Show 6 shimmer items

    class ShimmerViewHolder(view: View) : RecyclerView.ViewHolder(view)
}