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
import np.com.parts.system.models.WarrantyInfo

class WarrantyAdapter : RecyclerView.Adapter<WarrantyAdapter.WarrantyViewHolder>() {
    private var warrantyTerms: List<String> = emptyList()

    class WarrantyViewHolder(private val binding: ItemWarrantyBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(term: String) {
            binding.termText.text = term
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WarrantyViewHolder {
        val binding = ItemWarrantyBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WarrantyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WarrantyViewHolder, position: Int) {
        holder.bind(warrantyTerms[position])
    }

    override fun getItemCount(): Int = warrantyTerms.size

    fun submitWarrantyInfo(warrantyInfo: WarrantyInfo) {
        warrantyTerms = warrantyInfo.terms
        notifyDataSetChanged()
    }
}


