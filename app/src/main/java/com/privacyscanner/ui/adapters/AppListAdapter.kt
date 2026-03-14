package com.privacyscanner.ui.adapters

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.privacyscanner.R
import com.privacyscanner.data.model.AppInfo
import com.privacyscanner.data.model.RiskLevel
import com.privacyscanner.databinding.ItemAppBinding

class AppListAdapter(
    private val onAppClick: (AppInfo) -> Unit
) : ListAdapter<AppInfo, AppListAdapter.AppViewHolder>(AppDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemAppBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AppViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AppViewHolder(
        private val binding: ItemAppBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(app: AppInfo) {
            binding.apply {
                // App icon
                if (app.icon != null) {
                    ivAppIcon.setImageDrawable(app.icon)
                } else {
                    ivAppIcon.setImageResource(R.drawable.ic_default_app)
                }

                // App name & package
                tvAppName.text = app.appName
                tvPackageName.text = app.packageName

                // Permission count
                tvPermCount.text = "${app.dangerousPermissionCount} dangerous / ${app.totalPermissionCount} total"

                // Risk score badge
                tvRiskScore.text = app.privacyScore.toString()
                val riskColor = getRiskColor(app.riskLevel)
                tvRiskScore.backgroundTintList = ColorStateList.valueOf(riskColor)
                tvRiskLabel.text = app.riskLevel.displayName
                tvRiskLabel.setTextColor(riskColor)

                // Risk level indicator bar
                viewRiskBar.backgroundTintList = ColorStateList.valueOf(riskColor)

                // Flags indicator
                if (app.spywareFlags.isNotEmpty()) {
                    ivFlagIcon.visibility = View.VISIBLE
                    tvFlagCount.visibility = View.VISIBLE
                    tvFlagCount.text = "${app.spywareFlags.size} flag${if (app.spywareFlags.size > 1) "s" else ""}"
                } else {
                    ivFlagIcon.visibility = View.GONE
                    tvFlagCount.visibility = View.GONE
                }

                // Background service indicator
                if (app.hasRunningServices) {
                    ivServiceIcon.visibility = View.VISIBLE
                } else {
                    ivServiceIcon.visibility = View.GONE
                }

                // Click handler
                root.setOnClickListener { onAppClick(app) }
            }
        }

        private fun getRiskColor(riskLevel: RiskLevel): Int {
            return when (riskLevel) {
                RiskLevel.SAFE     -> Color.parseColor("#4CAF50")
                RiskLevel.LOW      -> Color.parseColor("#8BC34A")
                RiskLevel.MODERATE -> Color.parseColor("#FF9800")
                RiskLevel.HIGH     -> Color.parseColor("#F44336")
                RiskLevel.CRITICAL -> Color.parseColor("#B71C1C")
            }
        }
    }
}

class AppDiffCallback : DiffUtil.ItemCallback<AppInfo>() {
    override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean =
        oldItem.packageName == newItem.packageName

    override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean =
        oldItem.privacyScore == newItem.privacyScore &&
                oldItem.spywareFlags == newItem.spywareFlags
}
