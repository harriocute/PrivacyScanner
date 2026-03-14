package com.privacyscanner.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.privacyscanner.data.repository.AppScannerRepository
import com.privacyscanner.service.PrivacyMonitorService
import com.privacyscanner.data.model.RiskLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PackageReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val packageName = intent.data?.schemeSpecificPart ?: return
        if (intent.action != Intent.ACTION_PACKAGE_ADDED &&
            intent.action != Intent.ACTION_PACKAGE_REPLACED) return

        // Skip system packages
        if (intent.getBooleanExtra(Intent.EXTRA_REPLACING, false) &&
            intent.action == Intent.ACTION_PACKAGE_ADDED) return

        val prefs = context.getSharedPreferences("privacy_scanner_prefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("background_monitoring", false)) return

        CoroutineScope(Dispatchers.IO).launch {
            val repo = AppScannerRepository(context)
            val appInfo = repo.getAppDetail(packageName) ?: return@launch

            if (appInfo.riskLevel == RiskLevel.HIGH || appInfo.riskLevel == RiskLevel.CRITICAL) {
                val service = PrivacyMonitorService()
                // Note: In production, use a bound service or WorkManager to send notifications
            }
        }
    }
}
