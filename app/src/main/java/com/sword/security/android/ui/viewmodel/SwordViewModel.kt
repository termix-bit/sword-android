package com.sword.security.android.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sword.security.android.SwordApplication
import com.sword.security.android.core.model.CapabilityState
import com.sword.security.android.core.model.DeviceCapabilitiesSummary
import com.sword.security.android.core.model.ExecutionProgress
import com.sword.security.android.core.model.ModuleCategory
import com.sword.security.android.core.model.PermissionStatusItem
import com.sword.security.android.core.model.ToolDescriptor
import com.sword.security.android.data.local.db.ActivityLogEntity
import com.sword.security.android.data.local.db.EvidenceRecordEntity
import com.sword.security.android.data.local.db.ReportMetadataEntity
import com.sword.security.android.data.local.db.ScanRecordEntity
import com.sword.security.android.data.local.prefs.UserPreferences
import com.sword.security.android.data.reports.SavedJsonReport
import com.sword.security.android.data.repository.ModuleRegistry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SwordViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as SwordApplication
    private val db = app.database
    private val prefsRepo = app.preferencesRepository
    private val capManager = app.capabilityManager
    private val permManager = app.permissionManager
    private val engine = app.executionEngine
    private val reportStorage = app.reportStorageManager

    val preferences: StateFlow<UserPreferences> = prefsRepo.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserPreferences())

    val recentScans: StateFlow<List<ScanRecordEntity>> = db.scanDao().observeAllScans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReports: StateFlow<List<ReportMetadataEntity>> = db.reportDao().observeAllReports()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentActivity: StateFlow<List<ActivityLogEntity>> = db.activityDao().observeRecentActivity(15)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val evidenceRecords: StateFlow<List<EvidenceRecordEntity>> = db.evidenceDao().observeAllEvidence()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val executionProgress: StateFlow<ExecutionProgress> = engine.progress

    private val _capabilitiesSummary = MutableStateFlow<DeviceCapabilitiesSummary?>(null)
    val capabilitiesSummary: StateFlow<DeviceCapabilitiesSummary?> = _capabilitiesSummary.asStateFlow()

    private val _permissions = MutableStateFlow<List<PermissionStatusItem>>(emptyList())
    val permissions: StateFlow<List<PermissionStatusItem>> = _permissions.asStateFlow()

    private val _selectedReportContent = MutableStateFlow<SavedJsonReport?>(null)
    val selectedReportContent: StateFlow<SavedJsonReport?> = _selectedReportContent.asStateFlow()

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    init {
        refreshCapabilitiesAndPermissions()
    }

    fun refreshCapabilitiesAndPermissions() {
        viewModelScope.launch {
            _permissions.value = permManager.getAllManagedPermissions()
            _capabilitiesSummary.value = capManager.inspectDeviceCapabilities()
        }
    }

    fun getToolCapability(tool: ToolDescriptor): CapabilityState {
        return capManager.resolveToolCapability(tool, _capabilitiesSummary.value)
    }

    fun getWifiPermissionsToRequest(): Array<String> = permManager.getRequiredWifiPermissionsForRequest()

    fun completeOnboarding() {
        viewModelScope.launch {
            prefsRepo.setOnboardingCompleted(true)
        }
    }

    fun runTool(tool: ToolDescriptor, params: Map<String, String>): String {
        return engine.startExecution(tool, params)
    }

    fun cancelExecution(taskId: String) {
        engine.cancelExecution(taskId)
    }

    fun getTools(): List<ToolDescriptor> = ModuleRegistry.allTools

    fun getToolsByCategory(category: ModuleCategory): List<ToolDescriptor> =
        ModuleRegistry.getToolsByCategory(category)

    fun findTool(id: String): ToolDescriptor? = ModuleRegistry.findTool(id)

    fun loadReportDetail(report: ReportMetadataEntity) {
        viewModelScope.launch {
            _selectedReportContent.value = reportStorage.readSavedReport(report.jsonFileName)
        }
    }

    fun clearSelectedReport() {
        _selectedReportContent.value = null
    }

    fun getReportShareUri(fileName: String): Uri? = reportStorage.getShareUriForFile(fileName)

    fun exportReportToUri(fileName: String, destUri: Uri) {
        viewModelScope.launch {
            val ok = reportStorage.exportToUri(fileName, destUri)
            _statusBanner.value = if (ok) "Report exported successfully." else "Failed to export report."
        }
    }

    fun deleteSingleReport(report: ReportMetadataEntity) {
        viewModelScope.launch {
            reportStorage.deleteReportFiles(report.jsonFileName, report.htmlFileName)
            db.reportDao().deleteReportById(report.id)
            _statusBanner.value = "Deleted report '${report.title}'."
        }
    }

    fun deleteAllReports() {
        viewModelScope.launch {
            reportStorage.deleteAllReportFiles()
            db.reportDao().deleteAllReports()
            _statusBanner.value = "All reports deleted."
        }
    }

    fun deleteSingleScan(scanId: Long) {
        viewModelScope.launch {
            db.scanDao().deleteScanById(scanId)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            db.scanDao().deleteAllScans()
            db.activityDao().deleteAllActivity()
            db.evidenceDao().deleteAllEvidence()
            _statusBanner.value = "Scan history and evidence logs cleared."
        }
    }

    fun clearAllAppData() {
        viewModelScope.launch {
            reportStorage.deleteAllReportFiles()
            db.reportDao().deleteAllReports()
            db.scanDao().deleteAllScans()
            db.activityDao().deleteAllActivity()
            db.evidenceDao().deleteAllEvidence()
            _statusBanner.value = "All reports and history cleared."
        }
    }

    fun setAnimationsEnabled(enabled: Boolean) {
        viewModelScope.launch { prefsRepo.setAnimationsEnabled(enabled) }
    }

    fun setRequireAuthorizationConfirmation(required: Boolean) {
        viewModelScope.launch { prefsRepo.setRequireAuthorizationConfirmation(required) }
    }

    fun setNetworkTimeout(seconds: Int) {
        viewModelScope.launch { prefsRepo.setNetworkTimeoutSeconds(seconds) }
    }

    fun setMaxConcurrency(limit: Int) {
        viewModelScope.launch { prefsRepo.setMaxConcurrency(limit) }
    }

    fun setDohProvider(provider: String) {
        viewModelScope.launch { prefsRepo.setDohProvider(provider) }
    }

    fun setLanguageCode(code: String) {
        viewModelScope.launch { prefsRepo.setLanguageCode(code) }
    }

    fun saveProxyUrl(proxy: String) {
        viewModelScope.launch {
            runCatching {
                prefsRepo.setProxyUrl(proxy)
                _statusBanner.value = if (proxy.isBlank()) "Proxy disabled." else "Proxy updated."
            }.onFailure { err ->
                _statusBanner.value = err.message ?: "Invalid proxy URL."
            }
        }
    }

    fun resetSettings() {
        viewModelScope.launch {
            prefsRepo.resetDefaults()
            _statusBanner.value = "Settings reset to defaults."
        }
    }

    fun consumeStatusBanner() {
        _statusBanner.value = null
    }
}
