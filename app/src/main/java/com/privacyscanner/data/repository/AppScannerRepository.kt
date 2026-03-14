package com.privacyscanner.data.repository

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.privacyscanner.data.model.AppInfo
import com.privacyscanner.data.model.ScanResult
import com.privacyscanner.utils.PermissionDatabase
import com.privacyscanner.utils.RiskAnalyzer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Repository that uses Android's PackageManager to scan installed apps
 * and analyze their privacy risk.
 *
 * NO data is transmitted externally — all analysis is done on-device.
 */
class AppScannerRepository(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    // Cache the last scan result
    private var cachedScanResult: ScanResult? = null
    private var cachedAppList: List<AppInfo> = emptyList()

    /**
     * Emits scan progress (0.0–1.0) as apps are scanned, then emits the full ScanResult.
     * All processing happens on the IO dispatcher.
     */
    fun scanInstalledApps(): Flow<ScanProgress> = flow {
        val startTime = System.currentTimeMillis()
        emit(ScanProgress.Started)

        val packages = getInstalledPackages()
        val totalCount = packages.size
        val scannedApps = mutableListOf<AppInfo>()

        packages.forEachIndexed { index, packageInfo ->
            val appInfo = analyzePackage(packageInfo)
            scannedApps.add(appInfo)

            val progress = (index + 1).toFloat() / totalCount
            emit(ScanProgress.Progress(progress, appInfo.appName, index + 1, totalCount))
        }

        // Build summary
        val scanDuration = System.currentTimeMillis() - startTime
        val result = buildScanResult(scannedApps, scanDuration)
        cachedScanResult = result
        cachedAppList = scannedApps

        emit(ScanProgress.Completed(result, scannedApps))
    }.flowOn(Dispatchers.IO)

    /**
     * Returns cached app list or rescans synchronously.
     */
    suspend fun getAppList(): List<AppInfo> = withContext(Dispatchers.IO) {
        if (cachedAppList.isNotEmpty()) return@withContext cachedAppList
        val packages = getInstalledPackages()
        packages.map { analyzePackage(it) }.also { cachedAppList = it }
    }

    /**
     * Get detailed info for a single package.
     */
    suspend fun getAppDetail(packageName: String): AppInfo? = withContext(Dispatchers.IO) {
        return@withContext try {
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                PackageManager.GET_PERMISSIONS or PackageManager.GET_SIGNING_CERTIFICATES
            } else {
                @Suppress("DEPRECATION")
                PackageManager.GET_PERMISSIONS or PackageManager.GET_SIGNATURES
            }
            val packageInfo = packageManager.getPackageInfo(packageName, flags)
            analyzePackage(packageInfo)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    fun getCachedScanResult(): ScanResult? = cachedScanResult

    // ── Private helpers ─────────────────────────────────────────────────────────

    private fun getInstalledPackages(): List<PackageInfo> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getInstalledPackages(
                PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS)
        }
    }

    private fun analyzePackage(packageInfo: PackageInfo): AppInfo {
        val packageName = packageInfo.packageName

        // Get app icon and name
        val appName = try {
            packageManager.getApplicationLabel(packageInfo.applicationInfo).toString()
        } catch (e: Exception) {
            packageName
        }

        val icon = try {
            packageManager.getApplicationIcon(packageName)
        } catch (e: Exception) {
            null
        }

        // Parse permissions
        val permissions = (packageInfo.requestedPermissions ?: emptyArray())
            .map { permName -> PermissionDatabase.getPermissionInfo(permName) }

        // Check for running services
        val hasRunningServices = isAppRunningService(packageName)

        // System app check
        val isSystemApp = (packageInfo.applicationInfo.flags and
                android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0

        // Target SDK
        val targetSdk = packageInfo.applicationInfo.targetSdkVersion

        // Install/update times
        val installTime = packageInfo.firstInstallTime
        val updateTime = packageInfo.lastUpdateTime

        // Version
        val versionName = packageInfo.versionName ?: "Unknown"

        // Risk analysis
        val (score, flags) = RiskAnalyzer.analyze(
            permissions = permissions,
            hasRunningServices = hasRunningServices,
            targetSdkVersion = targetSdk,
            isSystemApp = isSystemApp
        )
        val riskLevel = RiskAnalyzer.scoreToRiskLevel(score)

        return AppInfo(
            packageName = packageName,
            appName = appName,
            icon = icon,
            permissions = permissions,
            privacyScore = score,
            riskLevel = riskLevel,
            isSystemApp = isSystemApp,
            installDate = installTime,
            lastUpdateDate = updateTime,
            spywareFlags = flags,
            hasRunningServices = hasRunningServices,
            versionName = versionName,
            targetSdkVersion = targetSdk
        )
    }

    @Suppress("DEPRECATION")
    private fun isAppRunningService(packageName: String): Boolean {
        return try {
            val runningServices = activityManager.getRunningServices(Integer.MAX_VALUE)
            runningServices?.any { it.service.packageName == packageName } ?: false
        } catch (e: SecurityException) {
            false
        }
    }

    private fun buildScanResult(apps: List<AppInfo>, durationMs: Long): ScanResult {
        val nonSystem = apps.filter { !it.isSystemApp }
        return ScanResult(
            totalApps = nonSystem.size,
            safeApps = nonSystem.count { it.riskLevel == com.privacyscanner.data.model.RiskLevel.SAFE
                    || it.riskLevel == com.privacyscanner.data.model.RiskLevel.LOW },
            moderateRiskApps = nonSystem.count { it.riskLevel == com.privacyscanner.data.model.RiskLevel.MODERATE },
            highRiskApps = nonSystem.count { it.riskLevel == com.privacyscanner.data.model.RiskLevel.HIGH },
            criticalRiskApps = nonSystem.count { it.riskLevel == com.privacyscanner.data.model.RiskLevel.CRITICAL },
            devicePrivacyScore = RiskAnalyzer.computeDevicePrivacyScore(apps),
            scanDurationMs = durationMs,
            scanTimestamp = System.currentTimeMillis(),
            flaggedApps = nonSystem.filter { it.isFlagged }
                .sortedByDescending { it.privacyScore }
        )
    }
}

/**
 * Sealed class for scan progress events.
 */
sealed class ScanProgress {
    object Started : ScanProgress()

    data class Progress(
        val fraction: Float,
        val currentAppName: String,
        val scannedCount: Int,
        val totalCount: Int
    ) : ScanProgress()

    data class Completed(
        val result: ScanResult,
        val apps: List<AppInfo>
    ) : ScanProgress()

    data class Error(val message: String) : ScanProgress()
}
