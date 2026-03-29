package com.privacyscanner.ui.appdetail

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.privacyscanner.R
import com.privacyscanner.data.model.AppInfo
import com.privacyscanner.data.model.RiskLevel
import com.privacyscanner.data.model.SpywareFlag
import com.privacyscanner.databinding.FragmentAppDetailBinding
import com.privacyscanner.ui.adapters.PermissionAdapter
import com.privacyscanner.viewmodel.ScannerViewModel
import java.text.SimpleDateFormat
import java.util.*

class AppDetailFragment : Fragment() {

    private var _binding: FragmentAppDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ScannerViewModel by activityViewModels()
    private lateinit var permissionAdapter: PermissionAdapter
    private var currentApp: AppInfo? = null
    private var currentPackageName: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentPackageName = arguments?.getString("packageName") ?: run {
            findNavController().popBackStack()
            return
        }

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        viewModel.loadAppDetail(currentPackageName)
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupRecyclerView() {
        permissionAdapter = PermissionAdapter()
        binding.recyclerPermissions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = permissionAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupClickListeners() {
        binding.btnOpenSettings.setOnClickListener { openAppSettings(currentPackageName) }
        binding.btnUninstall.setOnClickListener { showUninstallDialog() }
        binding.btnRevokePermissions.setOnClickListener { openAppSettings(currentPackageName) }
    }

    private fun observeViewModel() {
        viewModel.selectedApp.observe(viewLifecycleOwner) { app ->
            app ?: return@observe
            currentApp = app
            populateUI(app)
        }
    }

    private fun populateUI(app: AppInfo) {
        binding.toolbar.title = app.appName
        if (app.icon != null) binding.ivAppIcon.setImageDrawable(app.icon)
        binding.tvAppName.text = app.appName
        binding.tvPackageName.text = app.packageName
        binding.tvVersion.text = "v${app.versionName}"

        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        binding.tvInstallDate.text = "Installed: ${sdf.format(Date(app.installDate))}"

        val riskColor = getRiskColor(app.riskLevel)
        binding.progressRiskScore.progress = app.privacyScore
        binding.progressRiskScore.setIndicatorColor(riskColor)
        binding.tvRiskScore.text = app.privacyScore.toString()
        binding.tvRiskLabel.text = app.riskLevel.displayName
        binding.tvRiskLabel.setTextColor(riskColor)

        binding.tvDangerousCount.text = app.dangerousPermissionCount.toString()
        binding.tvTotalPerms.text = app.totalPermissionCount.toString()

        binding.tvSystemAppNote.visibility = if (app.isSystemApp) View.VISIBLE else View.GONE

        if (app.hasRunningServices) {
            binding.tvServiceStatus.text = "⚡ Background service running"
            binding.tvServiceStatus.setTextColor(Color.parseColor("#FF9800"))
        } else {
            binding.tvServiceStatus.text = "✓ No active background services"
            binding.tvServiceStatus.setTextColor(Color.parseColor("#4CAF50"))
        }

        if (app.spywareFlags.isNotEmpty()) {
            binding.cardFlags.visibility = View.VISIBLE
            populateFlags(app.spywareFlags)
        } else {
            binding.cardFlags.visibility = View.GONE
        }

        binding.btnUninstall.visibility = if (app.isSystemApp) View.GONE else View.VISIBLE

        val sorted = app.permissions.sortedWith(
            compareByDescending<com.privacyscanner.data.model.PermissionInfo> { it.isDangerous }
                .thenByDescending { it.riskWeight }
        )
        permissionAdapter.submitList(sorted)
    }

    private fun populateFlags(flags: List<SpywareFlag>) {
        binding.llFlags.removeAllViews()
        flags.forEach { flag ->
            val flagView = layoutInflater.inflate(R.layout.item_flag, binding.llFlags, false)
            flagView.findViewById<TextView>(R.id.tv_flag_title).text = "⚠ ${flag.title}"
            flagView.findViewById<TextView>(R.id.tv_flag_desc).text = flag.description
            binding.llFlags.addView(flagView)
        }
    }

    private fun getRiskColor(riskLevel: RiskLevel): Int = when (riskLevel) {
        RiskLevel.SAFE     -> Color.parseColor("#4CAF50")
        RiskLevel.LOW      -> Color.parseColor("#8BC34A")
        RiskLevel.MODERATE -> Color.parseColor("#FF9800")
        RiskLevel.HIGH     -> Color.parseColor("#F44336")
        RiskLevel.CRITICAL -> Color.parseColor("#B71C1C")
    }

    private fun openAppSettings(packageName: String) {
        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        })
    }

    private fun showUninstallDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Uninstall App")
            .setMessage("Are you sure you want to uninstall ${currentApp?.appName}?")
            .setPositiveButton("Uninstall") { _, _ ->
                startActivity(Intent(Intent.ACTION_UNINSTALL_PACKAGE).apply {
                    data = Uri.fromParts("package", currentPackageName, null)
                    putExtra(Intent.EXTRA_RETURN_RESULT, true)
                })
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
