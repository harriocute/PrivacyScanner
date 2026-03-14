package com.privacyscanner.ui.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.privacyscanner.R
import com.privacyscanner.databinding.FragmentScannerBinding
import com.privacyscanner.ui.adapters.AppListAdapter
import com.privacyscanner.viewmodel.FilterMode
import com.privacyscanner.viewmodel.ScanState
import com.privacyscanner.viewmodel.ScannerViewModel
import com.privacyscanner.viewmodel.SortMode

class ScannerFragment : Fragment() {

    private var _binding: FragmentScannerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ScannerViewModel by activityViewModels()
    private lateinit var adapter: AppListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFilterChips()
        setupSearch()
        setupSortButton()
        setupScanButton()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = AppListAdapter(
            onAppClick = { appInfo ->
                val bundle = Bundle().apply {
                    putString("packageName", appInfo.packageName)
                }
                findNavController().navigate(R.id.action_scanner_to_appDetail, bundle)
            }
        )
        binding.recyclerApps.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerApps.adapter = adapter
        binding.recyclerApps.setHasFixedSize(true)
        binding.recyclerApps.itemAnimator = null  // Disable for performance during scan
    }

    private fun setupFilterChips() {
        // Build filter chips programmatically
        val filters = listOf(
            FilterMode.ALL to "All",
            FilterMode.HIGH_RISK to "High Risk",
            FilterMode.MODERATE to "Moderate",
            FilterMode.SAFE to "Safe",
            FilterMode.FLAGGED to "Flagged",
        )

        filters.forEach { (mode, label) ->
            val chip = Chip(requireContext()).apply {
                text = label
                isCheckable = true
                isChecked = mode == FilterMode.ALL
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) viewModel.setFilter(mode)
                }
            }
            binding.chipGroupFilter.addView(chip)
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.setSearchQuery(s?.toString() ?: "")
            }
        })
    }

    private fun setupSortButton() {
        binding.btnSort.setOnClickListener { view ->
            val popup = PopupMenu(requireContext(), view)
            SortMode.values().forEachIndexed { index, mode ->
                popup.menu.add(0, index, index, mode.displayName)
            }
            popup.setOnMenuItemClickListener { item ->
                viewModel.setSortMode(SortMode.values()[item.itemId])
                true
            }
            popup.show()
        }
    }

    private fun setupScanButton() {
        binding.fabScan.setOnClickListener {
            viewModel.startScan()
        }

        binding.btnStartScan.setOnClickListener {
            viewModel.startScan()
        }
    }

    private fun observeViewModel() {
        viewModel.scanState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ScanState.Idle -> {
                    showIdleState()
                }
                is ScanState.Scanning -> {
                    showScanningState()
                }
                is ScanState.Completed -> {
                    showResultsState()
                }
                is ScanState.Error -> {
                    showErrorState(state.message)
                }
            }
        }

        viewModel.scanProgress.observe(viewLifecycleOwner) { progress ->
            binding.progressBar.progress = (progress * 100).toInt()
        }

        viewModel.currentScanningApp.observe(viewLifecycleOwner) { appName ->
            binding.tvScanningApp.text = "Scanning: $appName"
        }

        viewModel.scanCountText.observe(viewLifecycleOwner) { text ->
            binding.tvScanCount.text = text
        }

        viewModel.filteredApps.observe(viewLifecycleOwner) { apps ->
            adapter.submitList(apps)
            binding.tvAppCount.text = "${apps.size} apps"
            binding.tvEmptyState.visibility = if (apps.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.scanResult.observe(viewLifecycleOwner) { result ->
            if (result != null) {
                binding.scanSummaryCard.visibility = View.VISIBLE
                binding.tvSummaryTotal.text = result.totalApps.toString()
                binding.tvSummaryHighRisk.text = (result.highRiskApps + result.criticalRiskApps).toString()
                binding.tvSummaryModerate.text = result.moderateRiskApps.toString()
                binding.tvSummarySafe.text = result.safeApps.toString()
            }
        }
    }

    private fun showIdleState() {
        binding.groupScanProgress.visibility = View.GONE
        binding.groupResults.visibility = View.GONE
        binding.groupIdlePrompt.visibility = View.VISIBLE
        binding.fabScan.show()
    }

    private fun showScanningState() {
        binding.groupIdlePrompt.visibility = View.GONE
        binding.groupScanProgress.visibility = View.VISIBLE
        binding.groupResults.visibility = View.GONE
        binding.fabScan.hide()
        binding.progressBar.progress = 0
    }

    private fun showResultsState() {
        binding.groupScanProgress.visibility = View.GONE
        binding.groupIdlePrompt.visibility = View.GONE
        binding.groupResults.visibility = View.VISIBLE
        binding.fabScan.show()
    }

    private fun showErrorState(message: String) {
        binding.groupScanProgress.visibility = View.GONE
        binding.groupResults.visibility = View.GONE
        binding.groupIdlePrompt.visibility = View.VISIBLE
        binding.tvError.visibility = View.VISIBLE
        binding.tvError.text = "Error: $message"
        binding.fabScan.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
