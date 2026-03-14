package com.privacyscanner.data.model

import android.graphics.drawable.Drawable

/**
 * Core data model representing a scanned application with its privacy analysis results.
 */
data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val permissions: List<PermissionInfo>,
    val privacyScore: Int,          // 0–100 (higher = more risky)
    val riskLevel: RiskLevel,
    val isSystemApp: Boolean,
    val installDate: Long,
    val lastUpdateDate: Long,
    val spywareFlags: List<SpywareFlag>,
    val hasRunningServices: Boolean,
    val versionName: String,
    val targetSdkVersion: Int
) {
    val dangerousPermissionCount: Int
        get() = permissions.count { it.isDangerous }

    val totalPermissionCount: Int
        get() = permissions.size

    val isFlagged: Boolean
        get() = spywareFlags.isNotEmpty()
}

/**
 * Represents a single Android permission with risk metadata.
 */
data class PermissionInfo(
    val name: String,
    val simpleName: String,
    val description: String,
    val isDangerous: Boolean,
    val category: PermissionCategory,
    val riskWeight: Int          // 1–10, used in score calculation
)

/**
 * Categories of permissions for grouping/display.
 */
enum class PermissionCategory(val displayName: String, val iconRes: Int) {
    LOCATION("Location", android.R.drawable.ic_menu_mylocation),
    CAMERA("Camera", android.R.drawable.ic_menu_camera),
    MICROPHONE("Microphone", android.R.drawable.ic_btn_speak_now),
    CONTACTS("Contacts", android.R.drawable.ic_menu_myplaces),
    MESSAGES("Messages & Calls", android.R.drawable.ic_menu_send),
    STORAGE("Storage", android.R.drawable.ic_menu_save),
    SENSORS("Sensors & Body", android.R.drawable.ic_menu_compass),
    NETWORK("Network & Bluetooth", android.R.drawable.ic_menu_share),
    PHONE("Phone", android.R.drawable.ic_menu_call),
    CALENDAR("Calendar", android.R.drawable.ic_menu_today),
    ACCOUNTS("Accounts & Identity", android.R.drawable.ic_menu_login),
    OTHER("Other", android.R.drawable.ic_menu_info_details)
}

/**
 * Overall risk classification for an app.
 */
enum class RiskLevel(val displayName: String, val colorRes: String) {
    SAFE("Safe", "#4CAF50"),
    LOW("Low Risk", "#8BC34A"),
    MODERATE("Moderate Risk", "#FF9800"),
    HIGH("High Risk", "#F44336"),
    CRITICAL("Critical Risk", "#B71C1C")
}

/**
 * Specific spyware-like behavior flags.
 */
enum class SpywareFlag(val title: String, val description: String) {
    EXCESSIVE_PERMISSIONS(
        "Excessive Permissions",
        "This app requests far more permissions than typical apps of its type."
    ),
    LOCATION_AND_MICROPHONE(
        "Location + Microphone Combo",
        "Accessing both location and microphone simultaneously is a common spyware pattern."
    ),
    BACKGROUND_LOCATION(
        "Background Location Access",
        "This app can track your location even when you're not using it."
    ),
    CONTACTS_AND_MESSAGES(
        "Contacts + Messages Access",
        "Combined access to contacts and messages can enable data harvesting."
    ),
    CAMERA_AND_MICROPHONE(
        "Camera + Microphone Combo",
        "Simultaneous camera and microphone access raises surveillance concerns."
    ),
    READ_CALL_LOG(
        "Call Log Access",
        "This app can read your complete call history."
    ),
    SEND_SMS(
        "Can Send SMS",
        "This app has permission to send text messages on your behalf, potentially incurring costs."
    ),
    BOOT_AUTOSTART(
        "Auto-starts on Boot",
        "This app launches automatically when your phone starts."
    ),
    BACKGROUND_SERVICES(
        "Persistent Background Services",
        "This app runs services in the background that may monitor your device activity."
    ),
    ACCESSIBILITY_SERVICE(
        "Accessibility Service",
        "Accessibility services can read and control everything on your screen."
    ),
    DEVICE_ADMIN(
        "Device Administrator",
        "Device admin apps have elevated control over your device and can be difficult to uninstall."
    ),
    ADVERTISING_TRACKERS(
        "Advertising Trackers Detected",
        "This app contains known advertising or analytics tracking libraries."
    ),
    LOW_TARGET_SDK(
        "Outdated Security Standards",
        "This app targets an old Android version, potentially bypassing modern security protections."
    )
}

/**
 * Scan summary results shown after a full device scan.
 */
data class ScanResult(
    val totalApps: Int,
    val safeApps: Int,
    val moderateRiskApps: Int,
    val highRiskApps: Int,
    val criticalRiskApps: Int,
    val devicePrivacyScore: Int,       // 0–100 (higher = more private/safe)
    val scanDurationMs: Long,
    val scanTimestamp: Long,
    val flaggedApps: List<AppInfo>
) {
    val riskyAppsCount: Int
        get() = highRiskApps + criticalRiskApps

    val allRiskyApps: Int
        get() = moderateRiskApps + highRiskApps + criticalRiskApps
}
