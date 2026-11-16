package com.sword.security.android.domain.tools

import com.sword.security.android.core.model.SecurityFinding
import com.sword.security.android.core.python.PythonBridge
import com.sword.security.android.core.validation.InputValidator
import com.sword.security.android.data.local.prefs.UserPreferences
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext

class CustomToolsExecutor(
    private val webExecutor: WebSecurityToolsExecutor,
    private val osintExecutor: OsintToolsExecutor,
    private val networkExecutor: NetworkToolsExecutor,
    private val pythonBridge: PythonBridge
) {

    suspend fun runWebsiteAuditPreset(
        params: Map<String, String>,
        prefs: UserPreferences,
        onLog: suspend (String) -> Unit
    ): ToolExecutionPayload {
        val domain = InputValidator.validateDomain(params["domain"].orEmpty())
        val url = "https://$domain"

        onLog("[1/4] Auditing TLS certificate for $domain...")
        val tls = webExecutor.inspectTlsCertificate(mapOf("domain" to domain, "port" to "443"), prefs, onLog)
        coroutineContext.ensureActive()

        onLog("[2/4] Auditing HTTP response & security headers for $url...")
        val headers = webExecutor.inspectHttpHeaders(mapOf("url" to url), prefs, onLog)
        coroutineContext.ensureActive()

        onLog("[3/4] Auditing DNS & Email SPF/DMARC posture for $domain...")
        val dns = osintExecutor.dnsEmailPosture(mapOf("domain" to domain), prefs, onLog)
        coroutineContext.ensureActive()

        onLog("[4/4] Checking /.well-known/security.txt and robots.txt...")
        val cfg = webExecutor.authorizedSiteConfigCheck(mapOf("domain" to domain), prefs, onLog)

        val combinedOutput = buildString {
            appendLine("=== SWORD AUTHORIZED WEBSITE AUDIT PRESET ===")
            appendLine("Target Domain: $domain")
            appendLine()
            appendLine("--- [1/4] TLS Certificate ---")
            appendLine(tls.rawOutput)
            appendLine()
            appendLine("--- [2/4] Security Headers ---")
            appendLine(headers.rawOutput)
            appendLine()
            appendLine("--- [3/4] DNS & Mail Posture ---")
            appendLine(dns.rawOutput)
            appendLine()
            appendLine("--- [4/4] Site Configuration ---")
            appendLine(cfg.rawOutput)
        }.trim()

        val allFindings = tls.findings + headers.findings + dns.findings + cfg.findings
        return ToolExecutionPayload(
            targetSummary = domain,
            rawOutput = combinedOutput,
            findings = allFindings
        )
    }

    suspend fun runDomainOsintPreset(
        params: Map<String, String>,
        prefs: UserPreferences,
        onLog: suspend (String) -> Unit
    ): ToolExecutionPayload {
        val domain = InputValidator.validateDomain(params["domain"].orEmpty())

        onLog("[1/3] Querying RDAP WHOIS for $domain...")
        val rdap = osintExecutor.rdapWhoisLookup(mapOf("domain" to domain), prefs, onLog)
        coroutineContext.ensureActive()

        onLog("[2/3] Querying A & NS DNS records via DoH...")
        val aRecords = networkExecutor.dnsLookup(mapOf("domain" to domain, "record_type" to "A"), prefs, onLog)
        val nsRecords = networkExecutor.dnsLookup(mapOf("domain" to domain, "record_type" to "NS"), prefs, onLog)
        coroutineContext.ensureActive()

        onLog("[3/3] Checking SPF & DMARC posture...")
        val mail = osintExecutor.dnsEmailPosture(mapOf("domain" to domain), prefs, onLog)

        val combinedOutput = buildString {
            appendLine("=== SWORD DOMAIN OSINT PROFILE ===")
            appendLine("Domain: $domain")
            appendLine()
            appendLine("--- [1/3] RDAP Registry ---")
            appendLine(rdap.rawOutput)
            appendLine()
            appendLine("--- [2/3] DNS A & NS Records ---")
            appendLine(aRecords.rawOutput)
            appendLine()
            appendLine(nsRecords.rawOutput)
            appendLine()
            appendLine("--- [3/3] Email Security Posture ---")
            appendLine(mail.rawOutput)
        }.trim()

        return ToolExecutionPayload(
            targetSummary = domain,
            rawOutput = combinedOutput,
            findings = rdap.findings + aRecords.findings + mail.findings
        )
    }

    suspend fun runSandboxedPythonDiagnostic(
        params: Map<String, String>,
        onLog: suspend (String) -> Unit
    ): ToolExecutionPayload {
        val mode = params["operation"] ?: "Runtime Health Check"
        val input = params["input"].orEmpty()

        onLog("Invoking Chaquopy Embedded Python 3.10 bridge ($mode)...")
        val status = pythonBridge.getRuntimeStatus()
        check(status.available) { status.error ?: "Embedded Python runtime unavailable" }

        val sb = StringBuilder()
        sb.appendLine("Sword Sandboxed Python 3.10 Diagnostic")
        sb.appendLine("Engine  : ${status.engine}")
        sb.appendLine("Version : ${status.pythonVersion}")
        sb.appendLine("Mode    : $mode")
        sb.appendLine()

        val resultDetail = when (mode) {
            "Runtime Health Check" -> {
                "Python 3.10 runtime initialized inside Android APK without Termux or external Python."
            }
            "Validate Domain (Python)" -> {
                val res = pythonBridge.validateWithPython("validate_domain", input).getOrThrow()
                "Validated Domain: $res"
            }
            "Validate URL (Python)" -> {
                val res = pythonBridge.validateWithPython("validate_url", input).getOrThrow()
                "Validated URL: $res"
            }
            "Compute Hashes (Python)" -> {
                require(input.isNotEmpty()) { "Input text required for hashing" }
                val res = pythonBridge.computePythonHashes(input).getOrThrow()
                "Python hashlib output:\n$res"
            }
            else -> throw IllegalArgumentException("Unsupported safe Python operation: $mode")
        }
        sb.appendLine(resultDetail)

        return ToolExecutionPayload(
            targetSummary = mode,
            rawOutput = sb.toString().trim(),
            findings = listOf(
                SecurityFinding(
                    title = "Embedded Python Bridge Executed Safely",
                    severity = "Info",
                    detail = "Executed '$mode' via Chaquopy Python ${status.pythonVersion} without eval/exec or shell access.",
                    recommendation = "Only run pre-audited Python modules inside Android app-private sandbox."
                )
            )
        )
    }
}
