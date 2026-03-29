package com.privacyscanner.ui.appdetail

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.privacyscanner.R
import com.privacyscanner.data.model.AppInfo
import com.privacyscanner.data.model.RiskLevel
import com.privacyscanner.data.model.SpywareFlag
import com.privacyscanner.databinding.ActivityAppDetailBinding
import com.privacyscanner.ui.adapters.PermissionAdapter
import com.privacyscanner.viewmodel.ScannerViewModel
import java.text.SimpleDateFormat
import java.util.*

class AppDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppDetailBinding
    private val viewModel: ScannerViewModel by viewModels()
    private lateinit var permissionAdapter: PermissionAdapter
    private var currentApp: AppInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val packageName = intent.getStringExtra("packageName") ?: run {
            finish()
            return
        }

        permissionAdapter = PermissionAdapter()
        binding.recyclerPermissions.apply {
            layoutManager = LinearLayoutManager(this@AppDetailActivity)
            adapter = permissionAdapter
            isNestedScrollingEnabled = false
        }

        binding.btnOpenSettings.setOnClickListener { openAppSettings(packageName) }
        binding.btnUninstall.setOnClickListener { showUninstallDialog(packageName) }
        binding.btnRevokePermissions.setOnClickListener { openAppSettings(packageName) }

        viewModel.loadAppDetail(packageName)
        viewModel.selectedApp.observe(this) { app ->
            app ?: return@observe
            currentApp = app
            populateUI(app)
        }
    }

    private fun populateUI(app: AppInfo) {
        supportActionBar?.title = app.appName
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
            binding.llFlags.removeAllViews()
            app.spywareFlags.forEach { flag ->
                val flagView = layoutInflater.inflate(R.layout.item_flag, binding.llFlags, false)
                flagView.findViewById<TextView>(R.id.tv_flag_title).text = "⚠ ${flag.title}"
                flagView.findViewById<TextView>(R.id.tv_flag_desc).text = flag.description
                binding.llFlags.addView(flagView)
            }
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

    private fun showUninstallDialog(packageName: String) {
        AlertDialog.Builder(this)
            .setTitle("Uninstall App")
            .setMessage("Are you sure you want to uninstall ${currentApp?.appName}?")
            .setPositiveButton("Uninstall") { _, _ ->
                startActivity(Intent(Intent.ACTION_UNINSTALL_PACKAGE).apply {
                    data = Uri.fromParts("package", packageName, null)
                    putExtra(Intent.EXTRA_RETURN_RESULT, true)
                })
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
