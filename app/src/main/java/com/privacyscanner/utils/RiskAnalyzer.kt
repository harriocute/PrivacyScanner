package com.privacyscanner.utils

import com.privacyscanner.data.model.AppInfo
import com.privacyscanner.data.model.PermissionInfo
import com.privacyscanner.data.model.RiskLevel
import com.privacyscanner.data.model.SpywareFlag

/**
 * Core privacy risk analysis engine.
 *
 * Calculates a 0–100 privacy risk score for each app based on:
 *  - Dangerous permission count and weight
 *  - Suspicious permission combinations (spyware patterns)
 *  - Background service behavior
 *  - Target SDK version (older = less secure)
 *  - Auto-start capability
 *
 * Score interpretation:
 *   0–20   → SAFE
 *   21–40  → LOW
 *   41–60  → MODERATE
 *   61–80  → HIGH
 *   81–100 → CRITICAL
 */
object RiskAnalyzer {

    // Permission name shortcuts for readability
    private const val PERM_LOCATION_FINE   = "android.permission.ACCESS_FINE_LOCATION"
    private const val PERM_LOCATION_COARSE = "android.permission.ACCESS_COARSE_LOCATION"
    private const val PERM_LOCATION_BG     = "android.permission.ACCESS_BACKGROUND_LOCATION"
    private const val PERM_MICROPHONE      = "android.permission.RECORD_AUDIO"
    private const val PERM_CAMERA          = "android.permission.CAMERA"
    private const val PERM_CONTACTS_READ   = "android.permission.READ_CONTACTS"
    private const val PERM_SMS_READ        = "android.permission.READ_SMS"
    private const val PERM_SMS_SEND        = "android.permission.SEND_SMS"
    private const val PERM_SMS_RECEIVE     = "android.permission.RECEIVE_SMS"
    private const val PERM_CALL_LOG        = "android.permission.READ_CALL_LOG"
    private const val PERM_CALL_OUTGOING   = "android.permission.PROCESS_OUTGOING_CALLS"
    private const val PERM_STORAGE_FULL    = "android.permission.MANAGE_EXTERNAL_STORAGE"
    private const val PERM_ACCESSIBILITY   = "android.permission.BIND_ACCESSIBILITY_SERVICE"
    private const val PERM_DEVICE_ADMIN    = "android.permission.BIND_DEVICE_ADMIN"
    private const val PERM_INSTALL_PKGS    = "android.permission.REQUEST_INSTALL_PACKAGES"
    private const val PERM_BOOT            = "android.permission.RECEIVE_BOOT_COMPLETED"
    private const val PERM_OVERLAY         = "android.permission.SYSTEM_ALERT_WINDOW"

    /**
     * Calculates privacy risk score (0–100) and detects spyware flags.
     * Returns Pair<score, flags>.
     */
    fun analyze(
        permissions: List<PermissionInfo>,
        hasRunningServices: Boolean,
        targetSdkVersion: Int,
        isSystemApp: Boolean
    ): Pair<Int, List<SpywareFlag>> {
        if (isSystemApp) {
            // System apps get a pass on scoring (they're trusted by the OS)
            return Pair(0, emptyList())
        }

        val permNames = permissions.map { it.name }.toSet()
        val flags = mutableListOf<SpywareFlag>()
        var score = 0

        // ── 1. Base score from dangerous permission weights ────────────────────
        val dangerousPerms = permissions.filter { it.isDangerous }
        val weightSum = dangerousPerms.sumOf { it.riskWeight }
        // Normalize: max expected weight sum ~50 (5 high-risk perms × 10)
        val permScore = (weightSum.toFloat() / 50f * 40f).coerceIn(0f, 40f).toInt()
        score += permScore

        // ── 2. Excessive permissions bonus ────────────────────────────────────
        val dangerousCount = dangerousPerms.size
        when {
            dangerousCount >= 10 -> { score += 20; flags.add(SpywareFlag.EXCESSIVE_PERMISSIONS) }
            dangerousCount >= 7  -> { score += 12; flags.add(SpywareFlag.EXCESSIVE_PERMISSIONS) }
            dangerousCount >= 5  -> { score += 6 }
        }

        // ── 3. Spyware combination patterns ───────────────────────────────────
        val hasLocation = PERM_LOCATION_FINE in permNames || PERM_LOCATION_COARSE in permNames
        val hasMicrophone = PERM_MICROPHONE in permNames
        val hasCamera = PERM_CAMERA in permNames
        val hasContacts = PERM_CONTACTS_READ in permNames
        val hasSmsRead = PERM_SMS_READ in permNames
        val hasSmsReceive = PERM_SMS_RECEIVE in permNames

        // Location + Microphone (classic surveillance combo)
        if (hasLocation && hasMicrophone) {
            score += 15
            flags.add(SpywareFlag.LOCATION_AND_MICROPHONE)
        }

        // Camera + Microphone (covert recording)
        if (hasCamera && hasMicrophone) {
            score += 10
            flags.add(SpywareFlag.CAMERA_AND_MICROPHONE)
        }

        // Contacts + Messages (data harvesting)
        if (hasContacts && (hasSmsRead || hasSmsReceive)) {
            score += 10
            flags.add(SpywareFlag.CONTACTS_AND_MESSAGES)
        }

        // Background location
        if (PERM_LOCATION_BG in permNames) {
            score += 12
            flags.add(SpywareFlag.BACKGROUND_LOCATION)
        }

        // Call log access
        if (PERM_CALL_LOG in permNames || PERM_CALL_OUTGOING in permNames) {
            score += 8
            flags.add(SpywareFlag.READ_CALL_LOG)
        }

        // Can send SMS
        if (PERM_SMS_SEND in permNames) {
            score += 8
            flags.add(SpywareFlag.SEND_SMS)
        }

        // Accessibility service (very dangerous)
        if (PERM_ACCESSIBILITY in permNames) {
            score += 20
            flags.add(SpywareFlag.ACCESSIBILITY_SERVICE)
        }

        // Device admin
        if (PERM_DEVICE_ADMIN in permNames) {
            score += 15
            flags.add(SpywareFlag.DEVICE_ADMIN)
        }

        // Auto-start
        if (PERM_BOOT in permNames) {
            score += 5
            flags.add(SpywareFlag.BOOT_AUTOSTART)
        }

        // ── 4. Background services ─────────────────────────────────────────────
        if (hasRunningServices) {
            score += 8
            flags.add(SpywareFlag.BACKGROUND_SERVICES)
        }

        // ── 5. Old target SDK penalty ──────────────────────────────────────────
        if (targetSdkVersion < 26) {
            score += 10
            flags.add(SpywareFlag.LOW_TARGET_SDK)
        } else if (targetSdkVersion < 29) {
            score += 5
        }

        // Clamp to 0–100
        return Pair(score.coerceIn(0, 100), flags)
    }

    /**
     * Converts a raw risk score to a RiskLevel enum.
     */
    fun scoreToRiskLevel(score: Int): RiskLevel = when {
        score <= 20 -> RiskLevel.SAFE
        score <= 40 -> RiskLevel.LOW
        score <= 60 -> RiskLevel.MODERATE
        score <= 80 -> RiskLevel.HIGH
        else        -> RiskLevel.CRITICAL
    }

    /**
     * Computes an overall device privacy score (0–100, where 100 = fully private).
     * Inverts the average app risk score and weights it toward high-risk apps.
     */
    fun computeDevicePrivacyScore(apps: List<AppInfo>): Int {
        if (apps.isEmpty()) return 100
        val nonSystem = apps.filter { !it.isSystemApp }
        if (nonSystem.isEmpty()) return 100

        val avgRisk = nonSystem.sumOf { it.privacyScore }.toFloat() / nonSystem.size
        val criticalPenalty = nonSystem.count { it.riskLevel == RiskLevel.CRITICAL } * 5
        val highPenalty = nonSystem.count { it.riskLevel == RiskLevel.HIGH } * 2
        val rawScore = (100 - avgRisk - criticalPenalty - highPenalty).toInt()
        return rawScore.coerceIn(0, 100)
    }
}
