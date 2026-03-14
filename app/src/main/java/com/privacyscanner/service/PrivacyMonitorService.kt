package com.privacyscanner.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.privacyscanner.R
import com.privacyscanner.ui.MainActivity

/**
 * Background service for optional privacy monitoring.
 * Monitors for newly installed apps and alerts users of high-risk apps.
 *
 * NOTE: This service only runs when explicitly enabled by the user.
 * No data is transmitted externally.
 */
class PrivacyMonitorService : Service() {

    companion object {
        const val CHANNEL_ID = "privacy_monitor_channel"
        const val NOTIFICATION_ID = 1001
        const val RISK_ALERT_ID = 1002
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildForegroundNotification())
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    fun sendRiskAlert(appName: String, riskLevel: String) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_shield_alert)
            .setContentTitle("⚠ Risky App Installed")
            .setContentText("$appName — $riskLevel risk detected")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(RISK_ALERT_ID, notification)
    }

    private fun buildForegroundNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_shield)
        .setContentTitle("Privacy Scanner")
        .setContentText("Monitoring your device privacy…")
        .setPriority(NotificationCompat.PRIORITY_MIN)
        .setOngoing(true)
        .build()

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Privacy Monitor",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Privacy Scanner background monitoring"
        }
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}
