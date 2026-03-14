package com.privacyscanner.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.privacyscanner.service.PrivacyMonitorService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs: SharedPreferences = context.getSharedPreferences("privacy_scanner_prefs", Context.MODE_PRIVATE)
            val monitoringEnabled = prefs.getBoolean("background_monitoring", false)
            if (monitoringEnabled) {
                val serviceIntent = Intent(context, PrivacyMonitorService::class.java)
                context.startForegroundService(serviceIntent)
            }
        }
    }
}
