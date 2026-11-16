package com.sword.security.android.core.execution

import com.sword.security.android.core.capability.CapabilityManager
import com.sword.security.android.core.model.CapabilityState
import com.sword.security.android.core.model.ExecutionErrorType
import com.sword.security.android.core.model.ExecutionProgress
import com.sword.security.android.core.model.ExecutionResult
import com.sword.security.android.core.model.ExecutionStatus
import com.sword.security.android.core.model.SeverityLevel
import com.sword.security.android.core.model.ToolDescriptor
import com.sword.security.android.core.validation.ValidationException
import com.sword.security.android.data.local.db.ActivityDao
import com.sword.security.android.data.local.db.ActivityLogEntity
import com.sword.security.android.data.local.db.ReportDao
import com.sword.security.android.data.local.db.ReportMetadataEntity
import com.sword.security.android.data.local.db.ScanDao
import com.sword.security.android.data.local.db.ScanRecordEntity
import com.sword.security.android.data.local.prefs.UserPreferences
import com.sword.security.android.data.local.prefs.UserPreferencesRepository
import com.sword.security.android.data.reports.ReportStorageManager
import com.sword.security.android.domain.tools.CryptoToolsExecutor
import com.sword.security.android.domain.tools.CustomToolsExecutor
import com.sword.security.android.domain.tools.ForensicsToolsExecutor
import com.sword.security.android.domain.tools.NetworkToolsExecutor
import com.sword.security.android.domain.tools.OsintToolsExecutor
import com.sword.security.android.domain.tools.ToolExecutionPayload
import com.sword.security.android.domain.tools.WebSecurityToolsExecutor
import com.sword.security.android.domain.tools.WirelessToolsExecutor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.net.ssl.SSLException

class ExecutionEngine(
    private val capabilityManager: CapabilityManager,
    private val preferencesRepository: UserPreferencesRepository,
    private val scanDao: ScanDao,
    private val reportDao: ReportDao,
    private val activityDao: ActivityDao,
    private val reportStorageManager: ReportStorageManager,
    private val networkExecutor: NetworkToolsExecutor,
    private val webExecutor: WebSecurityToolsExecutor,
    private val osintExecutor: OsintToolsExecutor,
    private val wirelessExecutor: WirelessToolsExecutor,
    private val cryptoExecutor: CryptoToolsExecutor,
    private val forensicsExecutor: ForensicsToolsExecutor,
    private val customExecutor: CustomToolsExecutor
) {
    private val engineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val executionSemaphore = Semaphore(2)
    private val activeJobs = ConcurrentHashMap<String, Job>()

    private val _progress = MutableStateFlow(ExecutionProgress())
    val progress: StateFlow<ExecutionProgress> = _progress.asStateFlow()

    fun startExecution(tool: ToolDescriptor, params: Map<String, String>): String {
        val taskId = UUID.randomUUID().toString().take(8)

        // Cancel previous active execution if user starts a new primary foreground task
        activeJobs.values.forEach { it.cancel() }
        activeJobs.clear()

        _progress.value = ExecutionProgress(
            taskId = taskId,
            toolId = tool.id,
            toolName = tool.name,
            status = ExecutionStatus.RUNNING,
            progressFraction = 0.08f,
            stageMessage = "Validating device capabilities and parameters...",
            liveLogs = listOf("Task #$taskId initialized for ${tool.name}")
        )

        val job = engineScope.launch {
            val startMs = System.currentTimeMillis()
            try {
                executionSemaphore.withPermit {
                    val prefs = preferencesRepository.preferencesFlow.first()
                    val summary = capabilityManager.inspectDeviceCapabilities()
                    val capState = capabilityManager.resolveToolCapability(tool, summary)

                    if (capState == CapabilityState.UNSUPPORTED || capState == CapabilityState.NOT_IMPLEMENTED) {
                        throw UnsupportedOperationException(
                            "Tool '${tool.name}' is ${capState.label} on this Android device."
                        )
                    }
                    if (capState == CapabilityState.REQUIRES_PERMISSION) {
                        throw SecurityException(
                            "Required Android runtime permission is missing for '${tool.name}'."
                        )
                    }

                    appendLog("Capability check passed (${capState.label}). Executing...")
                    updateProgress(0.25f, "Running ${tool.name}...")

                    val maxTaskMs = (prefs.networkTimeoutSeconds * 8_000L).coerceIn(30_000L, 180_000L)
                    val payload = withTimeout(maxTaskMs) {
                        dispatchTool(tool, params, prefs) { line ->
                            appendLog(line)
                        }
                    }

                    updateProgress(0.85f, "Persisting structured scan report...")
                    val durationMs = (System.currentTimeMillis() - startMs).coerceAtLeast(1L)
                    val highest = computeHighestSeverity(payload.findings.map { it.severity })

                    val prelimResult = ExecutionResult(
                        taskId = taskId,
                        toolId = tool.id,
                        toolName = tool.name,
                        category = tool.category,
                        targetSummary = payload.targetSummary,
                        success = true,
                        durationMs = durationMs,
                        timestampEpochMs = System.currentTimeMillis(),
                        rawOutput = payload.rawOutput,
                        findings = payload.findings,
                        highestSeverity = highest
                    )

                    val (jsonFile, htmlFile) = reportStorageManager.saveReportFiles(prelimResult)
                    val scanId = scanDao.insertScan(
                        ScanRecordEntity(
                            taskId = taskId,
                            toolId = tool.id,
                            toolName = tool.name,
                            categoryId = tool.category.id,
                            targetSummary = payload.targetSummary,
                            status = ExecutionStatus.COMPLETED.name,
                            highestSeverity = highest.label,
                            durationMs = durationMs,
                            timestampEpochMs = prelimResult.timestampEpochMs,
                            summaryOutput = payload.rawOutput.take(2000)
                        )
                    )
                    val reportId = reportDao.insertReport(
                        ReportMetadataEntity(
                            scanRecordId = scanId,
                            title = "${tool.name} — ${payload.targetSummary}",
                            toolId = tool.id,
                            toolName = tool.name,
                            categoryId = tool.category.id,
                            targetSummary = payload.targetSummary,
                            highestSeverity = highest.label,
                            findingsCount = payload.findings.size,
                            jsonFileName = jsonFile,
                            htmlFileName = htmlFile,
                            createdEpochMs = prelimResult.timestampEpochMs
                        )
                    )
                    activityDao.insertActivity(
                        ActivityLogEntity(
                            categoryId = tool.category.id,
                            title = tool.name,
                            detail = "Completed for ${payload.targetSummary} in ${durationMs}ms",
                            severity = highest.label,
                            timestampEpochMs = prelimResult.timestampEpochMs
                        )
                    )

                    val finalResult = prelimResult.copy(reportId = reportId)
                    appendLog("Completed in ${durationMs} ms. Report #$reportId saved.")
                    _progress.update {
                        it.copy(
                            status = ExecutionStatus.COMPLETED,
                            progressFraction = 1f,
                            stageMessage = "Completed (${durationMs} ms)",
                            result = finalResult
                        )
                    }
                }
            } catch (ce: CancellationException) {
                val durationMs = (System.currentTimeMillis() - startMs).coerceAtLeast(1L)
                recordFailure(
                    taskId = taskId,
                    tool = tool,
                    durationMs = durationMs,
                    status = ExecutionStatus.CANCELLED,
                    errorType = ExecutionErrorType.CANCELLED,
                    message = "Execution cancelled by user."
                )
                throw ce
            } catch (t: Throwable) {
                val durationMs = (System.currentTimeMillis() - startMs).coerceAtLeast(1L)
                val (errType, safeMsg) = classifyError(t)
                recordFailure(
                    taskId = taskId,
                    tool = tool,
                    durationMs = durationMs,
                    status = ExecutionStatus.FAILED,
                    errorType = errType,
                    message = safeMsg
                )
            } finally {
                activeJobs.remove(taskId)
            }
        }

        activeJobs[taskId] = job
        return taskId
    }

    fun cancelExecution(taskId: String) {
        activeJobs[taskId]?.cancel()
    }

    fun resetProgress() {
        _progress.value = ExecutionProgress()
    }

    private suspend fun dispatchTool(
        tool: ToolDescriptor,
        params: Map<String, String>,
        prefs: UserPreferences,
        onLog: suspend (String) -> Unit
    ): ToolExecutionPayload = when (tool.id) {
        "net_interfaces" -> networkExecutor.inspectInterfaces(onLog)
        "net_dns_lookup" -> networkExecutor.dnsLookup(params, prefs, onLog)
        "net_connectivity" -> networkExecutor.connectivityCheck(params, prefs, onLog)
        "net_port_probe" -> networkExecutor.authorizedPortProbe(params, prefs, onLog)
        "net_lan_discovery" -> networkExecutor.authorizedLanDiscovery(params, prefs, onLog)

        "web_header_audit" -> webExecutor.inspectHttpHeaders(params, prefs, onLog)
        "web_tls_inspector" -> webExecutor.inspectTlsCertificate(params, prefs, onLog)
        "web_config_check" -> webExecutor.authorizedSiteConfigCheck(params, prefs, onLog)

        "osint_rdap_whois" -> osintExecutor.rdapWhoisLookup(params, prefs, onLog)
        "osint_ip_intel" -> osintExecutor.ipIntelligence(params, prefs, onLog)
        "osint_dns_posture" -> osintExecutor.dnsEmailPosture(params, prefs, onLog)
        "osint_username_check" -> osintExecutor.usernameAvailabilityCheck(params, prefs, onLog)

        "wifi_connection_info" -> wirelessExecutor.inspectWifiConnection(onLog)
        "wifi_nearby_scan" -> wirelessExecutor.scanNearbyWifi(onLog)

        "crypto_hash_gen" -> cryptoExecutor.generateHashes(params, onLog)
        "crypto_hash_verify" -> cryptoExecutor.verifyHash(params, onLog)
        "crypto_codec" -> cryptoExecutor.encodeDecode(params, onLog)
        "crypto_random_gen" -> cryptoExecutor.generateSecureRandom(params, onLog)

        "forensics_file_inspect" -> forensicsExecutor.inspectFileAndRecordEvidence(params, onLog)
        "forensics_exif_audit" -> forensicsExecutor.inspectExifMetadata(params, onLog)
        "forensics_strings_hex" -> forensicsExecutor.extractStringsAndHex(params, onLog)

        "custom_web_audit" -> customExecutor.runWebsiteAuditPreset(params, prefs, onLog)
        "custom_domain_osint" -> customExecutor.runDomainOsintPreset(params, prefs, onLog)
        "custom_python_runner" -> customExecutor.runSandboxedPythonDiagnostic(params, onLog)

        else -> throw UnsupportedOperationException("No execution handler registered for '${tool.id}'")
    }

    private suspend fun recordFailure(
        taskId: String,
        tool: ToolDescriptor,
        durationMs: Long,
        status: ExecutionStatus,
        errorType: ExecutionErrorType,
        message: String
    ) {
        val now = System.currentTimeMillis()
        runCatching {
            scanDao.insertScan(
                ScanRecordEntity(
                    taskId = taskId,
                    toolId = tool.id,
                    toolName = tool.name,
                    categoryId = tool.category.id,
                    targetSummary = "-",
                    status = status.name,
                    highestSeverity = SeverityLevel.INFO.label,
                    durationMs = durationMs,
                    timestampEpochMs = now,
                    summaryOutput = message,
                    errorMessage = message
                )
            )
            activityDao.insertActivity(
                ActivityLogEntity(
                    categoryId = tool.category.id,
                    title = tool.name,
                    detail = "${status.name}: $message",
                    severity = SeverityLevel.INFO.label,
                    timestampEpochMs = now
                )
            )
        }
        appendLog("[${status.name}] $message")
        _progress.update {
            it.copy(
                status = status,
                progressFraction = 1f,
                stageMessage = message,
                result = ExecutionResult(
                    taskId = taskId,
                    toolId = tool.id,
                    toolName = tool.name,
                    category = tool.category,
                    targetSummary = "-",
                    success = false,
                    durationMs = durationMs,
                    timestampEpochMs = now,
                    rawOutput = message,
                    findings = emptyList(),
                    highestSeverity = SeverityLevel.INFO,
                    errorType = errorType,
                    errorMessage = message
                )
            )
        }
    }

    private fun classifyError(t: Throwable): Pair<ExecutionErrorType, String> = when (t) {
        is ValidationException, is IllegalArgumentException ->
            ExecutionErrorType.VALIDATION_ERROR to (t.message ?: "Invalid input parameters.")
        is SecurityException ->
            ExecutionErrorType.PERMISSION_DENIED to (t.message ?: "Required Android permission denied.")
        is UnsupportedOperationException ->
            ExecutionErrorType.CAPABILITY_UNAVAILABLE to (t.message ?: "Unsupported capability on this device.")
        is SocketTimeoutException, is kotlinx.coroutines.TimeoutCancellationException ->
            ExecutionErrorType.TIMEOUT to "Operation timed out waiting for network response."
        is SSLException ->
            ExecutionErrorType.NETWORK_ERROR to "TLS verification failed: ${t.message ?: "Untrusted or invalid SSL handshake"}"
        is IOException ->
            ExecutionErrorType.NETWORK_ERROR to (t.message ?: "Network I/O error.")
        else ->
            ExecutionErrorType.INTERNAL_ERROR to (t.message ?: "Unexpected execution error.")
    }

    private fun computeHighestSeverity(labels: List<String>): SeverityLevel {
        var best = SeverityLevel.INFO
        for (lbl in labels) {
            val matched = SeverityLevel.entries.firstOrNull { it.label.equals(lbl, ignoreCase = true) }
            if (matched != null && matched.priority > best.priority) {
                best = matched
            }
        }
        return best
    }

    private fun appendLog(line: String) {
        _progress.update { current ->
            val updated = (current.liveLogs + line).takeLast(120)
            current.copy(liveLogs = updated)
        }
    }

    private fun updateProgress(fraction: Float, stage: String) {
        _progress.update { it.copy(progressFraction = fraction, stageMessage = stage) }
    }
}
