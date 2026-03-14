package com.privacyscanner.ui.appdetail

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.view.View
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

        // Set up toolbar with back button
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val packageName = intent.getStringExtra("packageName") ?: run {
            finish()
            return
        }

        setupRecyclerView()
        setupClickListeners(packageName)
        viewModel.loadAppDetail(packageName)
        observeViewModel()
    }

    private fun setupRecyclerView() {
        permissionAdapter = PermissionAdapter()
        binding.recyclerPermissions.apply {
            layoutManager = LinearLayoutManager(this@AppDetailActivity)
            adapter = permissionAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupClickListeners(packageName: String) {
        binding.btnOpenSettings.setOnClickListener {
            openAppSettings(packageName)
        }

        binding.btnUninstall.setOnClickListener {
            showUninstallDialog(packageName)
        }

        binding.btnRevokePermissions.setOnClickListener {
            openAppSettings(packageName)
        }
    }

    private fun observeViewModel() {
        viewModel.selectedApp.observe(this) { app ->
            app ?: return@observe
            currentApp = app
            populateUI(app)
        }
    }

    private fun populateUI(app: AppInfo) {
        supportActionBar?.title = app.appName

        // App icon and header
        if (app.icon != null) {
            binding.ivAppIcon.setImageDrawable(app.icon)
        }
        binding.tvAppName.text = app.appName
        binding.tvPackageName.text = app.packageName
        binding.tvVersion.text = "v${app.versionName}"

        // Install date
        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        binding.tvInstallDate.text = "Installed: ${sdf.format(Date(app.installDate))}"

        // Privacy score ring
        val score = app.privacyScore
        val riskColor = getRiskColor(app.riskLevel)
        binding.progressRiskScore.progress = score
        binding.progressRiskScore.setIndicatorColor(riskColor)
        binding.tvRiskScore.text = score.toString()
        binding.tvRiskLabel.text = app.riskLevel.displayName
        binding.tvRiskLabel.setTextColor(riskColor)

        // Permission summary
        binding.tvDangerousCount.text = app.dangerousPermissionCount.toString()
        binding.tvTotalPerms.text = app.totalPermissionCount.toString()

        // System app note
        if (app.isSystemApp) {
            binding.tvSystemAppNote.visibility = View.VISIBLE
        }

        // Spyware flags
        if (app.spywareFlags.isNotEmpty()) {
            binding.cardFlags.visibility = View.VISIBLE
            populateFlags(app.spywareFlags)
        } else {
            binding.cardFlags.visibility = View.GONE
        }

        // Hide uninstall for system apps
        if (app.isSystemApp) {
            binding.btnUninstall.visibility = View.GONE
        }

        // Background service
        if (app.hasRunningServices) {
            binding.tvServiceStatus.text = "⚡ Background service running"
            binding.tvServiceStatus.setTextColor(Color.parseColor("#FF9800"))
        } else {
            binding.tvServiceStatus.text = "✓ No background services"
            binding.tvServiceStatus.setTextColor(Color.parseColor("#4CAF50"))
        }

        // Permissions list
        val sortedPerms = app.permissions
            .sortedWith(compareByDescending<com.privacyscanner.data.model.PermissionInfo>
                { it.isDangerous }.thenByDescending { it.riskWeight })
        permissionAdapter.submitList(sortedPerms)
    }

    private fun populateFlags(flags: List<SpywareFlag>) {
        binding.llFlags.removeAllViews()
        flags.forEach { flag ->
            val flagView = layoutInflater.inflate(
                R.layout.item_flag, binding.llFlags, false
            )
            val tvTitle = flagView.findViewById<android.widget.TextView>(R.id.tvFlagTitle)
            val tvDesc = flagView.findViewById<android.widget.TextView>(R.id.tvFlagDesc)
            tvTitle.text = "⚠ ${flag.title}"
            tvDesc.text = flag.description
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
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    private fun showUninstallDialog(packageName: String) {
        AlertDialog.Builder(this)
            .setTitle("Uninstall App")
            .setMessage("Are you sure you want to uninstall ${currentApp?.appName}?")
            .setPositiveButton("Uninstall") { _, _ ->
                val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE).apply {
                    data = Uri.fromParts("package", packageName, null)
                    putExtra(Intent.EXTRA_RETURN_RESULT, true)
                }
                startActivity(intent)
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
