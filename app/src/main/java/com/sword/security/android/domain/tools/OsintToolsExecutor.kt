package com.sword.security.android.domain.tools

import com.sword.security.android.core.model.SecurityFinding
import com.sword.security.android.core.network.HttpClientFactory
import com.sword.security.android.core.python.PythonBridge
import com.sword.security.android.core.validation.InputValidator
import com.sword.security.android.data.local.prefs.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Request

class OsintToolsExecutor(
    private val pythonBridge: PythonBridge,
    private val networkToolsExecutor: NetworkToolsExecutor
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun rdapWhoisLookup(
        params: Map<String, String>,
        prefs: UserPreferences,
        onLog: suspend (String) -> Unit
    ): ToolExecutionPayload = withContext(Dispatchers.IO) {
        val domain = InputValidator.validateDomain(params["domain"].orEmpty())
        val url = "https://rdap.org/domain/$domain"
        onLog("Querying HTTPS RDAP registry for $domain...")

        val client = HttpClientFactory.create(prefs.networkTimeoutSeconds, prefs.proxyUrl)
        val body = client.newCall(
            Request.Builder().url(url).header("Accept", "application/rdap+json").get().build()
        ).execute().use { resp ->
            if (!resp.isSuccessful) error("RDAP lookup failed with HTTP ${resp.code}")
            resp.body?.string().orEmpty()
        }

        val root = json.parseToJsonElement(body).jsonObject
        val ldhName = root["ldhName"]?.jsonPrimitive?.content ?: domain
        val statuses = root["status"]?.jsonArray?.mapNotNull { it.jsonPrimitive.content }.orEmpty()
        val nameservers = root["nameservers"]?.jsonArray?.mapNotNull {
            it.jsonObject["ldhName"]?.jsonPrimitive?.content
        }.orEmpty()
        val events = root["events"]?.jsonArray?.mapNotNull { ev ->
            val obj = ev.jsonObject
            val action = obj["eventAction"]?.jsonPrimitive?.content ?: return@mapNotNull null
            val date = obj["eventDate"]?.jsonPrimitive?.content ?: return@mapNotNull null
            "$action: $date"
        }.orEmpty()

        val sb = StringBuilder()
        sb.appendLine("RDAP Domain Intelligence (HTTPS)")
        sb.appendLine("Domain      : $ldhName")
        sb.appendLine("Statuses    : ${statuses.joinToString(", ").ifBlank { "N/A" }}")
        sb.appendLine("Nameservers : ${nameservers.joinToString(", ").ifBlank { "N/A" }}")
        sb.appendLine()
        sb.appendLine("=== Registry Events ===")
        if (events.isEmpty()) sb.appendLine("None reported") else events.forEach { sb.appendLine("  • $it") }

        val findings = mutableListOf<SecurityFinding>()
        val hasTransferLock = statuses.any { it.contains("TransferProhibited", ignoreCase = true) }
        if (!hasTransferLock && statuses.isNotEmpty()) {
            findings += SecurityFinding(
                title = "Domain Transfer Lock Not Observed in RDAP Status",
                severity = "Low",
                detail = "clientTransferProhibited status was not listed for $domain.",
                recommendation = "Enable registrar transfer lock to reduce domain hijacking risk."
            )
        } else {
            findings += SecurityFinding(
                title = "RDAP Domain Record Retrieved",
                severity = "Info",
                detail = "Retrieved ${nameservers.size} nameserver(s) and ${events.size} lifecycle event(s) for $domain.",
                recommendation = "Monitor domain expiration dates and registrar lock flags."
            )
        }

        ToolExecutionPayload(
            targetSummary = domain,
            rawOutput = sb.toString().trim(),
            findings = findings
        )
    }

    suspend fun ipIntelligence(
        params: Map<String, String>,
        prefs: UserPreferences,
        onLog: suspend (String) -> Unit
    ): ToolExecutionPayload = withContext(Dispatchers.IO) {
        val ip = InputValidator.validateIpAddress(params["ip"].orEmpty())
        val url = "https://ipwho.is/$ip"
        onLog("Querying HTTPS IP intelligence for $ip...")

        val client = HttpClientFactory.create(prefs.networkTimeoutSeconds, prefs.proxyUrl)
        val body = client.newCall(
            Request.Builder().url(url).header("User-Agent", "SwordAndroidToolkit/1.0").get().build()
        ).execute().use { resp ->
            if (!resp.isSuccessful) error("IP lookup failed with HTTP ${resp.code}")
            resp.body?.string().orEmpty()
        }

        val root = json.parseToJsonElement(body).jsonObject
        val success = root["success"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: true
        if (!success) {
            val msg = root["message"]?.jsonPrimitive?.content ?: "Lookup failed"
            error(msg)
        }

        val country = root["country"]?.jsonPrimitive?.content ?: "Unknown"
        val region = root["region"]?.jsonPrimitive?.content ?: "Unknown"
        val city = root["city"]?.jsonPrimitive?.content ?: "Unknown"
        val connObj = root["connection"]?.jsonObject
        val asn = connObj?.get("asn")?.jsonPrimitive?.content ?: "Unknown"
        val org = connObj?.get("org")?.jsonPrimitive?.content ?: "Unknown"
        val isp = connObj?.get("isp")?.jsonPrimitive?.content ?: "Unknown"

        val sb = StringBuilder()
        sb.appendLine("Public IP Intelligence (HTTPS)")
        sb.appendLine("IP Address : $ip")
        sb.appendLine("Location   : $city, $region, $country")
        sb.appendLine("ASN        : AS$asn")
        sb.appendLine("Org / ISP  : $org / $isp")

        val findings = listOf(
            SecurityFinding(
                title = "Public IP & ASN Mapped ($country / AS$asn)",
                severity = "Info",
                detail = "IP $ip maps to $org ($isp) in $city, $country.",
                recommendation = "Verify expected ASN and geographic routing for authorized infrastructure."
            )
        )

        ToolExecutionPayload(
            targetSummary = ip,
            rawOutput = sb.toString().trim(),
            findings = findings
        )
    }

    suspend fun dnsEmailPosture(
        params: Map<String, String>,
        prefs: UserPreferences,
        onLog: suspend (String) -> Unit
    ): ToolExecutionPayload = withContext(Dispatchers.IO) {
        val domain = InputValidator.validateDomain(params["domain"].orEmpty())
        onLog("Auditing TXT / SPF / MX / DMARC DNS posture for $domain...")

        val txtRes = networkToolsExecutor.dnsLookup(mapOf("domain" to domain, "record_type" to "TXT"), prefs, onLog)
        val mxRes = networkToolsExecutor.dnsLookup(mapOf("domain" to domain, "record_type" to "MX"), prefs, onLog)
        val dmarcRes = networkToolsExecutor.dnsLookup(mapOf("domain" to "_dmarc.$domain", "record_type" to "TXT"), prefs, onLog)

        val hasSpf = txtRes.rawOutput.contains("v=spf1", ignoreCase = true)
        val hasDmarc = dmarcRes.rawOutput.contains("v=DMARC1", ignoreCase = true)

        val sb = StringBuilder()
        sb.appendLine("=== Domain DNS & Email Security Posture: $domain ===")
        sb.appendLine()
        sb.appendLine("--- MX Records ---")
        sb.appendLine(mxRes.rawOutput)
        sb.appendLine()
        sb.appendLine("--- Root TXT / SPF ---")
        sb.appendLine(txtRes.rawOutput)
        sb.appendLine()
        sb.appendLine("--- _dmarc.$domain ---")
        sb.appendLine(dmarcRes.rawOutput)

        val findings = mutableListOf<SecurityFinding>()
        if (!hasSpf) {
            findings += SecurityFinding(
                title = "Missing SPF Record (v=spf1)",
                severity = "Medium",
                detail = "No SPF TXT policy detected for $domain.",
                recommendation = "Publish an explicit SPF TXT record to restrict authorized mail senders."
            )
        }
        if (!hasDmarc) {
            findings += SecurityFinding(
                title = "Missing DMARC Policy (_dmarc.$domain)",
                severity = "Medium",
                detail = "No v=DMARC1 TXT record found at _dmarc.$domain.",
                recommendation = "Publish a DMARC record starting with p=none or p=quarantine/reject."
            )
        }
        if (hasSpf && hasDmarc) {
            findings += SecurityFinding(
                title = "SPF & DMARC Records Configured",
                severity = "Info",
                detail = "Both SPF and DMARC records are present for $domain.",
                recommendation = "Ensure DMARC enforcement is set to p=quarantine or p=reject when ready."
            )
        }

        ToolExecutionPayload(
            targetSummary = domain,
            rawOutput = sb.toString().trim(),
            findings = findings
        )
    }

    suspend fun usernameAvailabilityCheck(
        params: Map<String, String>,
        prefs: UserPreferences,
        onLog: suspend (String) -> Unit
    ): ToolExecutionPayload {
        val username = InputValidator.validateUsername(params["username"].orEmpty())
        val maxSites = InputValidator.validateIntegerInRange(params["max_sites"] ?: "25", 5, 50, "Site Count")

        onLog("Launching embedded Python 3.10 OSINT engine (WhatsMyName dataset, $maxSites sites)...")
        val scanResult = pythonBridge.runUsernameScan(
            username = username,
            maxSites = maxSites,
            timeoutSeconds = prefs.networkTimeoutSeconds.coerceIn(3, 10).toDouble()
        ).getOrElse { err ->
            throw IllegalStateException("Python OSINT engine error: ${err.message}", err)
        }

        val sb = StringBuilder()
        sb.appendLine("Username OSINT Availability Check (Embedded Python 3.10)")
        sb.appendLine("Username      : ${scanResult.username}")
        sb.appendLine("Sites Checked : ${scanResult.sites_checked}")
        sb.appendLine("Matches Found : ${scanResult.matches_found}")
        sb.appendLine()
        if (scanResult.matches.isEmpty()) {
            sb.appendLine("No public profile matches confirmed across the checked subset.")
        } else {
            for (m in scanResult.matches) {
                sb.appendLine("  [+] ${m.site} (${m.category}) -> ${m.url}")
            }
        }

        val findings = listOf(
            SecurityFinding(
                title = "Checked ${scanResult.sites_checked} Public Platforms (${scanResult.matches_found} Matches)",
                severity = "Info",
                detail = "Wildcard-filtered HTTPS checks found ${scanResult.matches_found} public profile(s) for '$username'.",
                recommendation = "Verify each public URL manually before drawing attribution conclusions."
            )
        )

        return ToolExecutionPayload(
            targetSummary = "$username (${scanResult.sites_checked} sites)",
            rawOutput = sb.toString().trim(),
            findings = findings
        )
    }
}
