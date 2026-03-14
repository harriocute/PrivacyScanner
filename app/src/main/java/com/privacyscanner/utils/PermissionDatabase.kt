package com.privacyscanner.utils

import com.privacyscanner.data.model.PermissionCategory
import com.privacyscanner.data.model.PermissionInfo

/**
 * Comprehensive database of Android permissions with human-readable descriptions,
 * risk weights, and categorization.
 *
 * Risk weights (1–10):
 *   1–3: Low risk (network state, vibrate, etc.)
 *   4–6: Moderate risk (storage, bluetooth)
 *   7–8: High risk (camera, contacts, location)
 *   9–10: Critical risk (microphone, SMS send, call log, accessibility)
 */
object PermissionDatabase {

    data class PermissionEntry(
        val simpleName: String,
        val description: String,
        val isDangerous: Boolean,
        val category: PermissionCategory,
        val riskWeight: Int
    )

    private val permissionMap: Map<String, PermissionEntry> = mapOf(
        // ── LOCATION ──────────────────────────────────────────────────────────────
        "android.permission.ACCESS_FINE_LOCATION" to PermissionEntry(
            "Precise Location",
            "Allows the app to access your exact GPS location at all times.",
            true, PermissionCategory.LOCATION, 8
        ),
        "android.permission.ACCESS_COARSE_LOCATION" to PermissionEntry(
            "Approximate Location",
            "Allows the app to know your general location using Wi-Fi or cell towers.",
            true, PermissionCategory.LOCATION, 6
        ),
        "android.permission.ACCESS_BACKGROUND_LOCATION" to PermissionEntry(
            "Background Location",
            "Allows the app to access your location even when you're not using it — a serious privacy risk.",
            true, PermissionCategory.LOCATION, 10
        ),

        // ── CAMERA ────────────────────────────────────────────────────────────────
        "android.permission.CAMERA" to PermissionEntry(
            "Camera",
            "Allows the app to access your device camera and take photos or record video.",
            true, PermissionCategory.CAMERA, 8
        ),

        // ── MICROPHONE ────────────────────────────────────────────────────────────
        "android.permission.RECORD_AUDIO" to PermissionEntry(
            "Microphone",
            "Allows the app to record audio from your device microphone.",
            true, PermissionCategory.MICROPHONE, 9
        ),

        // ── CONTACTS ──────────────────────────────────────────────────────────────
        "android.permission.READ_CONTACTS" to PermissionEntry(
            "Read Contacts",
            "Allows the app to read all contacts saved on your device.",
            true, PermissionCategory.CONTACTS, 7
        ),
        "android.permission.WRITE_CONTACTS" to PermissionEntry(
            "Modify Contacts",
            "Allows the app to add, edit, or delete your contacts.",
            true, PermissionCategory.CONTACTS, 7
        ),
        "android.permission.GET_ACCOUNTS" to PermissionEntry(
            "Access Accounts",
            "Allows the app to see the list of all accounts linked to your device (Google, email, etc.).",
            true, PermissionCategory.ACCOUNTS, 6
        ),

        // ── SMS / CALLS ───────────────────────────────────────────────────────────
        "android.permission.READ_SMS" to PermissionEntry(
            "Read Text Messages",
            "Allows the app to read all SMS and MMS messages on your device.",
            true, PermissionCategory.MESSAGES, 9
        ),
        "android.permission.SEND_SMS" to PermissionEntry(
            "Send Text Messages",
            "Allows the app to send SMS messages, potentially costing you money without your knowledge.",
            true, PermissionCategory.MESSAGES, 10
        ),
        "android.permission.RECEIVE_SMS" to PermissionEntry(
            "Receive Text Messages",
            "Allows the app to intercept and read incoming SMS messages (including OTP codes).",
            true, PermissionCategory.MESSAGES, 9
        ),
        "android.permission.READ_CALL_LOG" to PermissionEntry(
            "Read Call History",
            "Allows the app to view your complete call history, including who you called and when.",
            true, PermissionCategory.MESSAGES, 8
        ),
        "android.permission.WRITE_CALL_LOG" to PermissionEntry(
            "Modify Call History",
            "Allows the app to edit or delete your call history.",
            true, PermissionCategory.MESSAGES, 7
        ),
        "android.permission.PROCESS_OUTGOING_CALLS" to PermissionEntry(
            "Intercept Outgoing Calls",
            "Allows the app to see the number you're calling and potentially redirect calls.",
            true, PermissionCategory.MESSAGES, 9
        ),
        "android.permission.READ_PHONE_STATE" to PermissionEntry(
            "Read Phone State",
            "Allows the app to access your phone number, current call status, and device IDs.",
            true, PermissionCategory.PHONE, 7
        ),
        "android.permission.READ_PHONE_NUMBERS" to PermissionEntry(
            "Read Phone Number",
            "Allows the app to read your phone number.",
            true, PermissionCategory.PHONE, 6
        ),
        "android.permission.CALL_PHONE" to PermissionEntry(
            "Make Phone Calls",
            "Allows the app to make phone calls directly, without your intervention.",
            true, PermissionCategory.PHONE, 8
        ),
        "android.permission.ANSWER_PHONE_CALLS" to PermissionEntry(
            "Answer Phone Calls",
            "Allows the app to answer incoming phone calls.",
            true, PermissionCategory.PHONE, 7
        ),

        // ── STORAGE ───────────────────────────────────────────────────────────────
        "android.permission.READ_EXTERNAL_STORAGE" to PermissionEntry(
            "Read Storage",
            "Allows the app to read all files on your device storage, including photos and documents.",
            true, PermissionCategory.STORAGE, 6
        ),
        "android.permission.WRITE_EXTERNAL_STORAGE" to PermissionEntry(
            "Write to Storage",
            "Allows the app to create, modify, and delete files on your device storage.",
            true, PermissionCategory.STORAGE, 6
        ),
        "android.permission.MANAGE_EXTERNAL_STORAGE" to PermissionEntry(
            "Full Storage Access",
            "Grants the app full, unrestricted access to all files on your device — a significant privacy risk.",
            true, PermissionCategory.STORAGE, 9
        ),
        "android.permission.READ_MEDIA_IMAGES" to PermissionEntry(
            "Access Photos",
            "Allows the app to view all photos stored on your device.",
            true, PermissionCategory.STORAGE, 6
        ),
        "android.permission.READ_MEDIA_VIDEO" to PermissionEntry(
            "Access Videos",
            "Allows the app to view all video files stored on your device.",
            true, PermissionCategory.STORAGE, 6
        ),
        "android.permission.READ_MEDIA_AUDIO" to PermissionEntry(
            "Access Audio Files",
            "Allows the app to view all audio files stored on your device.",
            true, PermissionCategory.STORAGE, 5
        ),

        // ── CALENDAR ──────────────────────────────────────────────────────────────
        "android.permission.READ_CALENDAR" to PermissionEntry(
            "Read Calendar",
            "Allows the app to read all your calendar events and appointments.",
            true, PermissionCategory.CALENDAR, 6
        ),
        "android.permission.WRITE_CALENDAR" to PermissionEntry(
            "Modify Calendar",
            "Allows the app to create, edit, or delete calendar events.",
            true, PermissionCategory.CALENDAR, 5
        ),

        // ── BODY SENSORS ──────────────────────────────────────────────────────────
        "android.permission.BODY_SENSORS" to PermissionEntry(
            "Body Sensors",
            "Allows the app to access data from health sensors like heart rate monitors.",
            true, PermissionCategory.SENSORS, 7
        ),
        "android.permission.ACTIVITY_RECOGNITION" to PermissionEntry(
            "Physical Activity",
            "Allows the app to detect your physical activity (walking, running, driving).",
            true, PermissionCategory.SENSORS, 6
        ),

        // ── NETWORK (Normal) ──────────────────────────────────────────────────────
        "android.permission.INTERNET" to PermissionEntry(
            "Internet Access",
            "Allows the app to access the internet and potentially send your data to external servers.",
            false, PermissionCategory.NETWORK, 3
        ),
        "android.permission.ACCESS_NETWORK_STATE" to PermissionEntry(
            "Check Network State",
            "Allows the app to check if your device is connected to the internet.",
            false, PermissionCategory.NETWORK, 1
        ),
        "android.permission.ACCESS_WIFI_STATE" to PermissionEntry(
            "Wi-Fi Information",
            "Allows the app to view information about Wi-Fi networks and your device's Wi-Fi status.",
            false, PermissionCategory.NETWORK, 2
        ),
        "android.permission.CHANGE_WIFI_STATE" to PermissionEntry(
            "Modify Wi-Fi Settings",
            "Allows the app to connect to or disconnect from Wi-Fi networks.",
            false, PermissionCategory.NETWORK, 4
        ),
        "android.permission.BLUETOOTH" to PermissionEntry(
            "Bluetooth",
            "Allows the app to connect to paired Bluetooth devices.",
            false, PermissionCategory.NETWORK, 3
        ),
        "android.permission.BLUETOOTH_SCAN" to PermissionEntry(
            "Bluetooth Scanning",
            "Allows the app to scan for nearby Bluetooth devices, which can be used for location tracking.",
            true, PermissionCategory.NETWORK, 5
        ),
        "android.permission.BLUETOOTH_CONNECT" to PermissionEntry(
            "Bluetooth Connection",
            "Allows the app to connect to paired Bluetooth devices.",
            true, PermissionCategory.NETWORK, 4
        ),

        // ── SYSTEM / DEVICE ───────────────────────────────────────────────────────
        "android.permission.RECEIVE_BOOT_COMPLETED" to PermissionEntry(
            "Auto-start on Boot",
            "Allows the app to start automatically every time your phone boots up.",
            false, PermissionCategory.OTHER, 5
        ),
        "android.permission.FOREGROUND_SERVICE" to PermissionEntry(
            "Run in Foreground",
            "Allows the app to run persistent background tasks visible in the notification bar.",
            false, PermissionCategory.OTHER, 4
        ),
        "android.permission.BIND_ACCESSIBILITY_SERVICE" to PermissionEntry(
            "Accessibility Service",
            "Accessibility services can read and interact with everything on your screen, including passwords.",
            false, PermissionCategory.OTHER, 10
        ),
        "android.permission.BIND_DEVICE_ADMIN" to PermissionEntry(
            "Device Administrator",
            "Device admin permissions give the app elevated control over your device and can make it hard to uninstall.",
            false, PermissionCategory.OTHER, 10
        ),
        "android.permission.SYSTEM_ALERT_WINDOW" to PermissionEntry(
            "Draw Over Other Apps",
            "Allows the app to display content on top of other applications, which can be used for phishing.",
            false, PermissionCategory.OTHER, 7
        ),
        "android.permission.REQUEST_INSTALL_PACKAGES" to PermissionEntry(
            "Install Apps",
            "Allows the app to install other applications on your device without going through the app store.",
            false, PermissionCategory.OTHER, 8
        ),
        "android.permission.USE_BIOMETRIC" to PermissionEntry(
            "Use Biometrics",
            "Allows the app to use your fingerprint or face recognition for authentication.",
            false, PermissionCategory.OTHER, 4
        ),
        "android.permission.USE_FINGERPRINT" to PermissionEntry(
            "Use Fingerprint",
            "Allows the app to use your fingerprint sensor.",
            false, PermissionCategory.OTHER, 4
        ),
        "android.permission.VIBRATE" to PermissionEntry(
            "Vibrate",
            "Allows the app to make your device vibrate.",
            false, PermissionCategory.OTHER, 1
        ),
        "android.permission.WAKE_LOCK" to PermissionEntry(
            "Keep Device Awake",
            "Allows the app to prevent your phone's screen or processor from sleeping.",
            false, PermissionCategory.OTHER, 3
        ),
        "android.permission.NFC" to PermissionEntry(
            "NFC",
            "Allows the app to perform NFC (near field communication) transactions.",
            false, PermissionCategory.NETWORK, 4
        ),
        "android.permission.USE_CREDENTIALS" to PermissionEntry(
            "Use Account Credentials",
            "Allows the app to request authentication tokens for your accounts.",
            false, PermissionCategory.ACCOUNTS, 5
        ),
        "android.permission.AUTHENTICATE_ACCOUNTS" to PermissionEntry(
            "Authenticate Accounts",
            "Allows the app to create accounts and manage passwords for your device.",
            false, PermissionCategory.ACCOUNTS, 6
        ),
        "android.permission.MANAGE_ACCOUNTS" to PermissionEntry(
            "Manage Accounts",
            "Allows the app to add or remove accounts linked to your device.",
            false, PermissionCategory.ACCOUNTS, 5
        )
    )

    /**
     * Look up permission info by its full Android permission string.
     * Returns a default entry for unknown permissions.
     */
    fun getPermissionInfo(permissionName: String): PermissionInfo {
        val entry = permissionMap[permissionName]
        return if (entry != null) {
            PermissionInfo(
                name = permissionName,
                simpleName = entry.simpleName,
                description = entry.description,
                isDangerous = entry.isDangerous,
                category = entry.category,
                riskWeight = entry.riskWeight
            )
        } else {
            // Default for unknown permissions
            val shortName = permissionName
                .substringAfterLast(".")
                .replace("_", " ")
                .lowercase()
                .replaceFirstChar { it.uppercase() }
            PermissionInfo(
                name = permissionName,
                simpleName = shortName,
                description = "This permission allows the app access to a system feature. Tap to learn more.",
                isDangerous = false,
                category = PermissionCategory.OTHER,
                riskWeight = 2
            )
        }
    }

    /** Returns all known dangerous permission names */
    val dangerousPermissions: Set<String> = permissionMap
        .filter { it.value.isDangerous }
        .keys
        .toSet()

    /** Critical spyware-indicator permissions */
    val spywareIndicatorPermissions: Set<String> = setOf(
        "android.permission.RECORD_AUDIO",
        "android.permission.ACCESS_BACKGROUND_LOCATION",
        "android.permission.READ_SMS",
        "android.permission.SEND_SMS",
        "android.permission.READ_CALL_LOG",
        "android.permission.PROCESS_OUTGOING_CALLS",
        "android.permission.MANAGE_EXTERNAL_STORAGE",
        "android.permission.BIND_ACCESSIBILITY_SERVICE",
        "android.permission.BIND_DEVICE_ADMIN",
        "android.permission.REQUEST_INSTALL_PACKAGES"
    )

    /** Known advertising/tracker library package prefixes */
    val trackerLibrarySignatures: List<String> = listOf(
        "com.google.android.gms.ads",
        "com.facebook.ads",
        "com.appsflyer",
        "com.adjust.sdk",
        "com.mopub",
        "com.unity3d.ads",
        "com.chartboost",
        "net.flurry",
        "com.flurry",
        "com.moat",
        "com.crashlytics",
        "io.fabric",
        "com.mixpanel",
        "com.amplitude",
        "io.branch",
        "com.branchmetrics",
        "com.kochava",
        "com.singular"
    )
}
