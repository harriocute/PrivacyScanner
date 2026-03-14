package com.privacyscanner.ui.adapters

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.privacyscanner.data.model.PermissionInfo
import com.privacyscanner.databinding.ItemPermissionBinding

class PermissionAdapter : ListAdapter<PermissionInfo, PermissionAdapter.PermissionViewHolder>(
    PermissionDiffCallback()
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PermissionViewHolder {
        val binding = ItemPermissionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PermissionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PermissionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PermissionViewHolder(
        private val binding: ItemPermissionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(permission: PermissionInfo) {
            binding.apply {
                tvPermissionName.text = permission.simpleName
                tvPermissionDescription.text = permission.description
                tvCategory.text = permission.category.displayName

                // Risk indicator dot
                val color = when {
                    permission.riskWeight >= 9 -> Color.parseColor("#F44336")
                    permission.riskWeight >= 7 -> Color.parseColor("#FF9800")
                    permission.riskWeight >= 5 -> Color.parseColor("#FFC107")
                    else                       -> Color.parseColor("#4CAF50")
                }
                viewRiskDot.backgroundTintList = ColorStateList.valueOf(color)

                // Dangerous badge
                if (permission.isDangerous) {
                    chipDangerous.visibility = android.view.View.VISIBLE
                } else {
                    chipDangerous.visibility = android.view.View.GONE
                }

                // Risk weight label
                tvRiskWeight.text = "Risk: ${permission.riskWeight}/10"
            }
        }
    }
}

class PermissionDiffCallback : DiffUtil.ItemCallback<PermissionInfo>() {
    override fun areItemsTheSame(oldItem: PermissionInfo, newItem: PermissionInfo): Boolean =
        oldItem.name == newItem.name

    override fun areContentsTheSame(oldItem: PermissionInfo, newItem: PermissionInfo): Boolean =
        oldItem == newItem
}
