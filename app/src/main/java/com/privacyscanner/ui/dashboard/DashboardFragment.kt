package com.privacyscanner.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.privacyscanner.R
import com.privacyscanner.databinding.FragmentDashboardBinding
import com.privacyscanner.ui.tips.PrivacyTipsActivity
import com.privacyscanner.viewmodel.ScanState
import com.privacyscanner.viewmodel.ScannerViewModel
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ScannerViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeViewModel()
        animateCards()
    }

    private fun setupClickListeners() {
        binding.btnScanNow.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_scanner)
        }

        binding.cardHighRisk.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_scanner)
        }

        binding.cardModerate.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_scanner)
        }

        binding.cardSafe.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_scanner)
        }

        binding.btnViewTips.setOnClickListener {
            startActivity(Intent(requireContext(), PrivacyTipsActivity::class.java))
        }

        binding.privacyScoreCard.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_scanner)
        }
    }

    private fun observeViewModel() {
        viewModel.scanResult.observe(viewLifecycleOwner) { result ->
            if (result != null) {
                binding.groupNoScanYet.visibility = View.GONE
                binding.groupScanResults.visibility = View.VISIBLE

                // Privacy score
                animatePrivacyScore(result.devicePrivacyScore)
                binding.tvPrivacyScoreLabel.text = getScoreLabel(result.devicePrivacyScore)

                // Stats cards
                binding.tvHighRiskCount.text = (result.highRiskApps + result.criticalRiskApps).toString()
                binding.tvModerateCount.text = result.moderateRiskApps.toString()
                binding.tvSafeCount.text = result.safeApps.toString()
                binding.tvTotalScanned.text = "${result.totalApps} apps scanned"

                // Last scan time
                val sdf = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
                binding.tvLastScan.text = "Last scan: ${sdf.format(Date(result.scanTimestamp))}"

                // Risky apps badge
                val riskyCount = result.riskyAppsCount
                if (riskyCount > 0) {
                    binding.tvRiskyAlert.visibility = View.VISIBLE
                    binding.tvRiskyAlert.text = "⚠ $riskyCount high-risk ${if (riskyCount == 1) "app" else "apps"} detected"
                } else {
                    binding.tvRiskyAlert.visibility = View.GONE
                }

                // Score ring color
                val color = getScoreColor(result.devicePrivacyScore)
                binding.progressPrivacyScore.setIndicatorColor(
                    requireContext().getColor(color)
                )
            } else {
                binding.groupNoScanYet.visibility = View.VISIBLE
                binding.groupScanResults.visibility = View.GONE
            }
        }

        viewModel.scanState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ScanState.Scanning -> {
                    binding.btnScanNow.text = "Scanning…"
                    binding.btnScanNow.isEnabled = false
                }
                else -> {
                    binding.btnScanNow.text = "Scan My Phone"
                    binding.btnScanNow.isEnabled = true
                }
            }
        }
    }

    private fun animatePrivacyScore(score: Int) {
        binding.progressPrivacyScore.progress = 0
        binding.tvPrivacyScore.text = "0"

        // Animate counter
        val animator = android.animation.ValueAnimator.ofInt(0, score)
        animator.duration = 1200
        animator.interpolator = android.view.animation.DecelerateInterpolator()
        animator.addUpdateListener { anim ->
            val current = anim.animatedValue as Int
            binding.progressPrivacyScore.progress = current
            binding.tvPrivacyScore.text = current.toString()
        }
        animator.start()
    }

    private fun getScoreLabel(score: Int): String = when {
        score >= 80 -> "Excellent Privacy"
        score >= 60 -> "Good Privacy"
        score >= 40 -> "Fair Privacy"
        score >= 20 -> "Poor Privacy"
        else        -> "Critical Risk"
    }

    private fun getScoreColor(score: Int): Int = when {
        score >= 80 -> R.color.risk_safe
        score >= 60 -> R.color.risk_low
        score >= 40 -> R.color.risk_moderate
        else        -> R.color.risk_high
    }

    private fun animateCards() {
        val slideIn = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up_fade_in)
        binding.root.startAnimation(slideIn)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
