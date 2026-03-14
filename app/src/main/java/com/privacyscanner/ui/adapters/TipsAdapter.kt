package com.privacyscanner.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.privacyscanner.databinding.ItemTipBinding
import com.privacyscanner.ui.tips.PrivacyTip

class TipsAdapter(
    private val tips: List<PrivacyTip>
) : RecyclerView.Adapter<TipsAdapter.TipViewHolder>() {

    private val expandedPositions = mutableSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TipViewHolder {
        val binding = ItemTipBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TipViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TipViewHolder, position: Int) {
        holder.bind(tips[position], position in expandedPositions)
    }

    override fun getItemCount(): Int = tips.size

    inner class TipViewHolder(
        private val binding: ItemTipBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(tip: PrivacyTip, isExpanded: Boolean) {
            binding.tvTipIcon.text = tip.icon
            binding.tvTipTitle.text = tip.title
            binding.tvTipDescription.text = tip.description

            // Steps
            binding.llSteps.removeAllViews()
            tip.steps.forEachIndexed { index, step ->
                val stepView = android.widget.TextView(binding.root.context).apply {
                    text = "${index + 1}. $step"
                    setPadding(0, 4, 0, 4)
                    setTextAppearance(com.google.android.material.R.style.TextAppearance_MaterialComponents_Body2)
                }
                binding.llSteps.addView(stepView)
            }

            // Expand/collapse
            binding.expandableContent.visibility = if (isExpanded) View.VISIBLE else View.GONE
            binding.ivExpandArrow.rotation = if (isExpanded) 180f else 0f

            binding.root.setOnClickListener {
                val pos = adapterPosition
                if (pos == RecyclerView.NO_ID.toInt()) return@setOnClickListener
                if (expandedPositions.contains(pos)) {
                    expandedPositions.remove(pos)
                } else {
                    expandedPositions.add(pos)
                }
                notifyItemChanged(pos)
            }
        }
    }
}
