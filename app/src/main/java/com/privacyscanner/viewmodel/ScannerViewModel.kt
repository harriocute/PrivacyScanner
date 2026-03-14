package com.privacyscanner.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.privacyscanner.data.model.AppInfo
import com.privacyscanner.data.model.RiskLevel
import com.privacyscanner.data.model.ScanResult
import com.privacyscanner.data.repository.AppScannerRepository
import com.privacyscanner.data.repository.ScanProgress
import kotlinx.coroutines.launch

/**
 * MVVM ViewModel — manages scanning state and app data for the UI.
 * All UI components observe LiveData; no UI references held here.
 */
class ScannerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppScannerRepository(application)

    // ── Scan state ───────────────────────────────────────────────────────────────
    private val _scanState = MutableLiveData<ScanState>(ScanState.Idle)
    val scanState: LiveData<ScanState> = _scanState

    private val _scanProgress = MutableLiveData<Float>(0f)
    val scanProgress: LiveData<Float> = _scanProgress

    private val _currentScanningApp = MutableLiveData<String>("")
    val currentScanningApp: LiveData<String> = _currentScanningApp

    private val _scanCountText = MutableLiveData<String>("")
    val scanCountText: LiveData<String> = _scanCountText

    // ── Results ──────────────────────────────────────────────────────────────────
    private val _scanResult = MutableLiveData<ScanResult?>()
    val scanResult: LiveData<ScanResult?> = _scanResult

    private val _allApps = MutableLiveData<List<AppInfo>>(emptyList())
    val allApps: LiveData<List<AppInfo>> = _allApps

    private val _filteredApps = MutableLiveData<List<AppInfo>>(emptyList())
    val filteredApps: LiveData<List<AppInfo>> = _filteredApps

    // ── Filter / Sort state ──────────────────────────────────────────────────────
    private val _activeFilter = MutableLiveData<FilterMode>(FilterMode.ALL)
    val activeFilter: LiveData<FilterMode> = _activeFilter

    private val _sortMode = MutableLiveData<SortMode>(SortMode.RISK_HIGH_FIRST)
    val sortMode: LiveData<SortMode> = _sortMode

    private val _searchQuery = MutableLiveData<String>("")
    val searchQuery: LiveData<String> = _searchQuery

    // ── App detail ───────────────────────────────────────────────────────────────
    private val _selectedApp = MutableLiveData<AppInfo?>()
    val selectedApp: LiveData<AppInfo?> = _selectedApp

    // ── Error ─────────────────────────────────────────────────────────────────────
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // ────────────────────────────────────────────────────────────────────────────

    /**
     * Starts a full device scan.
     */
    fun startScan() {
        if (_scanState.value == ScanState.Scanning) return

        viewModelScope.launch {
            _scanState.value = ScanState.Scanning
            _scanProgress.value = 0f

            try {
                repository.scanInstalledApps().collect { progress ->
                    when (progress) {
                        is ScanProgress.Started -> {
                            _currentScanningApp.value = "Preparing scan..."
                        }
                        is ScanProgress.Progress -> {
                            _scanProgress.value = progress.fraction
                            _currentScanningApp.value = progress.currentAppName
                            _scanCountText.value = "${progress.scannedCount}/${progress.totalCount}"
                        }
                        is ScanProgress.Completed -> {
                            _allApps.value = progress.apps
                            _scanResult.value = progress.result
                            applyFiltersAndSort()
                            _scanState.value = ScanState.Completed(progress.result)
                        }
                        is ScanProgress.Error -> {
                            _errorMessage.value = progress.message
                            _scanState.value = ScanState.Error(progress.message)
                        }
                    }
                }
            } catch (e: Exception) {
                val msg = e.message ?: "Unknown error during scan"
                _errorMessage.value = msg
                _scanState.value = ScanState.Error(msg)
            }
        }
    }

    /**
     * Load a single app's detail into selectedApp.
     */
    fun loadAppDetail(packageName: String) {
        viewModelScope.launch {
            // Try from cache first
            val cached = _allApps.value?.find { it.packageName == packageName }
            if (cached != null) {
                _selectedApp.value = cached
                return@launch
            }
            // Otherwise fetch individually
            val detail = repository.getAppDetail(packageName)
            _selectedApp.value = detail
        }
    }

    // ── Filtering & sorting ──────────────────────────────────────────────────────

    fun setFilter(filter: FilterMode) {
        _activeFilter.value = filter
        applyFiltersAndSort()
    }

    fun setSortMode(sort: SortMode) {
        _sortMode.value = sort
        applyFiltersAndSort()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        applyFiltersAndSort()
    }

    private fun applyFiltersAndSort() {
        val allApps = _allApps.value ?: return
        val query = _searchQuery.value?.lowercase() ?: ""
        val filter = _activeFilter.value ?: FilterMode.ALL
        val sort = _sortMode.value ?: SortMode.RISK_HIGH_FIRST

        // Filter by risk level
        var filtered = when (filter) {
            FilterMode.ALL -> allApps.filter { !it.isSystemApp }
            FilterMode.HIGH_RISK -> allApps.filter {
                !it.isSystemApp && (it.riskLevel == RiskLevel.HIGH || it.riskLevel == RiskLevel.CRITICAL)
            }
            FilterMode.MODERATE -> allApps.filter {
                !it.isSystemApp && it.riskLevel == RiskLevel.MODERATE
            }
            FilterMode.SAFE -> allApps.filter {
                !it.isSystemApp && (it.riskLevel == RiskLevel.SAFE || it.riskLevel == RiskLevel.LOW)
            }
            FilterMode.FLAGGED -> allApps.filter {
                !it.isSystemApp && it.isFlagged
            }
            FilterMode.SYSTEM -> allApps.filter { it.isSystemApp }
        }

        // Search filter
        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.appName.lowercase().contains(query) ||
                        it.packageName.lowercase().contains(query)
            }
        }

        // Sort
        filtered = when (sort) {
            SortMode.RISK_HIGH_FIRST -> filtered.sortedByDescending { it.privacyScore }
            SortMode.RISK_LOW_FIRST  -> filtered.sortedBy { it.privacyScore }
            SortMode.NAME_A_Z        -> filtered.sortedBy { it.appName.lowercase() }
            SortMode.NAME_Z_A        -> filtered.sortedByDescending { it.appName.lowercase() }
            SortMode.MOST_PERMISSIONS -> filtered.sortedByDescending { it.dangerousPermissionCount }
            SortMode.INSTALL_DATE    -> filtered.sortedByDescending { it.installDate }
        }

        _filteredApps.value = filtered
    }

    fun clearError() {
        _errorMessage.value = null
    }
}

// ── Enums ─────────────────────────────────────────────────────────────────────

sealed class ScanState {
    object Idle : ScanState()
    object Scanning : ScanState()
    data class Completed(val result: ScanResult) : ScanState()
    data class Error(val message: String) : ScanState()
}

enum class FilterMode(val displayName: String) {
    ALL("All Apps"),
    HIGH_RISK("High Risk"),
    MODERATE("Moderate"),
    SAFE("Safe"),
    FLAGGED("Flagged"),
    SYSTEM("System Apps")
}

enum class SortMode(val displayName: String) {
    RISK_HIGH_FIRST("Highest Risk First"),
    RISK_LOW_FIRST("Lowest Risk First"),
    NAME_A_Z("Name A–Z"),
    NAME_Z_A("Name Z–A"),
    MOST_PERMISSIONS("Most Permissions"),
    INSTALL_DATE("Install Date")
}
