package com.sword.security.android.data.reports

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.sword.security.android.core.model.ExecutionResult
import com.sword.security.android.core.model.SecurityFinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Serializable
data class SavedJsonReport(
    val schemaVersion: Int = 1,
    val generator: String = "Sword Android Security Toolkit",
    val taskId: String,
    val toolId: String,
    val toolName: String,
    val category: String,
    val target: String,
    val timestampIso: String,
    val durationMs: Long,
    val success: Boolean,
    val highestSeverity: String,
    val findings: List<SecurityFinding>,
    val output: String
)

data class ReportViewerData(
    val reportMetaId: Long,
    val jsonFileName: String,
    val htmlFileName: String,
    val parsedReport: SavedJsonReport?,
    val rawJsonText: String,
    val formattedJsonText: String,
    val jsonError: String?,
    val htmlText: String,
    val htmlError: String?
)

class ReportStorageManager(private val context: Context) {

    private val json = Json { prettyPrint = true }

    private fun getReportsDir(): File {
        val dir = File(context.filesDir, "reports")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    private fun resolveSafeReportFile(fileName: String): File {
        val dir = getReportsDir().canonicalFile
        val sanitized = File(fileName).name
        val candidate = File(dir, sanitized).canonicalFile
        require(candidate.parentFile == dir) { "Invalid report path traversal attempt" }
        return candidate
    }

    suspend fun saveReportFiles(result: ExecutionResult): Pair<String, String> = withContext(Dispatchers.IO) {
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date(result.timestampEpochMs))
        val isoStamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date(result.timestampEpochMs))
        val shortId = UUID.randomUUID().toString().take(6)
        val safeTool = result.toolId.replace(Regex("[^a-zA-Z0-9_-]"), "_")

        val jsonName = "sword_${safeTool}_${stamp}_$shortId.json"
        val htmlName = "sword_${safeTool}_${stamp}_$shortId.html"

        val reportPayload = SavedJsonReport(
            taskId = result.taskId,
            toolId = result.toolId,
            toolName = result.toolName,
            category = result.category.title,
            target = result.targetSummary,
            timestampIso = isoStamp,
            durationMs = result.durationMs,
            success = result.success,
            highestSeverity = result.highestSeverity.label,
            findings = result.findings,
            output = result.rawOutput
        )

        val jsonFile = resolveSafeReportFile(jsonName)
        jsonFile.writeText(json.encodeToString(reportPayload), Charsets.UTF_8)

        val htmlFile = resolveSafeReportFile(htmlName)
        htmlFile.writeText(buildBrandedHtml(reportPayload), Charsets.UTF_8)

        jsonName to htmlName
    }

    suspend fun readReportJsonText(fileName: String): String = withContext(Dispatchers.IO) {
        if (fileName.isBlank()) return@withContext ""
        val file = resolveSafeReportFile(fileName)
        if (!file.exists() || !file.isFile) return@withContext ""
        file.readText(Charsets.UTF_8)
    }

    suspend fun readReportHtmlText(fileName: String): String = withContext(Dispatchers.IO) {
        if (fileName.isBlank()) return@withContext ""
        val file = resolveSafeReportFile(fileName)
        if (!file.exists() || !file.isFile) return@withContext ""
        file.readText(Charsets.UTF_8)
    }

    suspend fun readSavedReport(fileName: String): SavedJsonReport? = withContext(Dispatchers.IO) {
        val text = readReportJsonText(fileName)
        if (text.isBlank()) return@withContext null
        runCatching { json.decodeFromString<SavedJsonReport>(text) }.getOrNull()
    }

    suspend fun loadReportViewerData(
        reportMetaId: Long,
        jsonFileName: String,
        htmlFileName: String
    ): ReportViewerData = withContext(Dispatchers.IO) {
        var rawJson = ""
        var formattedJson = ""
        var parsed: SavedJsonReport? = null
        var jsonErr: String? = null

        val jsonRead = runCatching { readReportJsonText(jsonFileName) }
        jsonRead.onSuccess { text ->
            rawJson = text
            if (text.isBlank()) {
                jsonErr = "JSON report file is missing or empty."
            } else {
                val trimmed = text.trimStart()
                val prettyResult = runCatching {
                    when {
                        trimmed.startsWith("{") -> org.json.JSONObject(trimmed).toString(2)
                        trimmed.startsWith("[") -> org.json.JSONArray(trimmed).toString(2)
                        else -> throw org.json.JSONException("JSON must begin with '{' or '['")
                    }
                }
                prettyResult.onSuccess { pretty ->
                    formattedJson = pretty
                    parsed = runCatching { json.decodeFromString<SavedJsonReport>(text) }.getOrNull()
                }.onFailure { err ->
                    jsonErr = err.message ?: "Invalid or corrupted JSON content."
                    formattedJson = text
                }
            }
        }.onFailure { err ->
            jsonErr = err.message ?: "Failed to read JSON report file."
        }

        var htmlText = ""
        var htmlErr: String? = null
        val htmlRead = runCatching { readReportHtmlText(htmlFileName) }
        htmlRead.onSuccess { text ->
            htmlText = text
            if (text.isBlank()) {
                htmlErr = "HTML report file is missing or empty."
            }
        }.onFailure { err ->
            htmlErr = err.message ?: "Failed to read HTML report file."
        }

        ReportViewerData(
            reportMetaId = reportMetaId,
            jsonFileName = jsonFileName,
            htmlFileName = htmlFileName,
            parsedReport = parsed,
            rawJsonText = rawJson,
            formattedJsonText = formattedJson,
            jsonError = jsonErr,
            htmlText = htmlText,
            htmlError = htmlErr
        )
    }

    suspend fun deleteReportFiles(jsonFileName: String, htmlFileName: String) = withContext(Dispatchers.IO) {
        runCatching {
            val j = resolveSafeReportFile(jsonFileName)
            if (j.exists()) j.delete()
        }
        runCatching {
            val h = resolveSafeReportFile(htmlFileName)
            if (h.exists()) h.delete()
        }
        Unit
    }

    suspend fun deleteAllReportFiles() = withContext(Dispatchers.IO) {
        val dir = getReportsDir()
        dir.listFiles()?.forEach { file ->
            if (file.isFile && (file.name.endsWith(".json") || file.name.endsWith(".html"))) {
                file.delete()
            }
        }
    }

    fun getShareUriForFile(fileName: String): Uri? {
        return runCatching {
            val file = resolveSafeReportFile(fileName)
            if (!file.exists()) return null
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        }.getOrNull()
    }

    suspend fun exportToUri(fileName: String, destinationUri: Uri): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val source = resolveSafeReportFile(fileName)
            if (!source.exists()) return@runCatching false
            context.contentResolver.openOutputStream(destinationUri)?.use { out ->
                source.inputStream().use { input ->
                    input.copyTo(out)
                }
            } ?: return@runCatching false
            true
        }.getOrDefault(false)
    }

    private fun buildBrandedHtml(report: SavedJsonReport): String {
        val findingsRows = if (report.findings.isEmpty()) {
            """<p style="color:#AAB4C0;">No structured findings recorded.</p>"""
        } else {
            report.findings.joinToString("\n") { finding ->
                val badgeColor = when (finding.severity.uppercase(Locale.US)) {
                    "CRITICAL" -> "#FF4D6D"
                    "HIGH" -> "#FF8A3D"
                    "MEDIUM" -> "#FFD166"
                    "LOW" -> "#00ADB5"
                    else -> "#12D5E5"
                }
                """
                <div class="finding">
                  <div class="finding-head">
                    <span class="badge" style="border-color:$badgeColor;color:$badgeColor;">${escapeHtml(finding.severity)}</span>
                    <strong>${escapeHtml(finding.title)}</strong>
                  </div>
                  <p>${escapeHtml(finding.detail)}</p>
                  <p class="rec"><span>Recommendation:</span> ${escapeHtml(finding.recommendation)}</p>
                </div>
                """.trimIndent()
            }
        }

        return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
          <meta charset="UTF-8" />
          <meta name="viewport" content="width=device-width, initial-scale=1.0" />
          <title>Sword Security Report - ${escapeHtml(report.toolName)}</title>
          <style>
            body { background:#101820; color:#F0F6FC; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif; margin:0; padding:24px; }
            .wrap { max-width:920px; margin:0 auto; background:#071C2E; border:1px solid #243747; border-radius:12px; padding:24px; }
            .header { border-bottom:1px solid #243747; padding-bottom:16px; margin-bottom:20px; }
            .brand { color:#12D5E5; font-size:22px; font-weight:700; letter-spacing:2px; }
            .sub { color:#AAB4C0; font-size:13px; margin-top:4px; }
            .meta-grid { display:grid; grid-template-columns:repeat(auto-fit,minmax(200px,1fr)); gap:12px; margin-bottom:20px; }
            .meta-card { background:#121F2D; border:1px solid #243747; border-radius:8px; padding:12px; }
            .meta-label { color:#AAB4C0; font-size:11px; text-transform:uppercase; letter-spacing:1px; }
            .meta-val { color:#F0F6FC; font-size:14px; font-weight:600; margin-top:4px; word-break:break-word; }
            h2 { color:#12D5E5; font-size:16px; margin:20px 0 10px 0; border-left:3px solid #00ADB5; padding-left:10px; }
            .finding { background:#121F2D; border:1px solid #243747; border-radius:8px; padding:14px; margin-bottom:10px; }
            .finding-head { display:flex; align-items:center; gap:10px; margin-bottom:8px; }
            .badge { border:1px solid #12D5E5; padding:2px 8px; border-radius:4px; font-size:11px; font-weight:700; text-transform:uppercase; }
            .rec { color:#AAB4C0; font-size:13px; }
            .rec span { color:#00ADB5; font-weight:600; }
            pre { background:#0B131B; border:1px solid #243747; border-radius:8px; padding:16px; color:#E6F4F8; font-family:monospace; font-size:12px; white-space:pre-wrap; word-break:break-word; overflow-x:auto; }
          </style>
        </head>
        <body>
          <div class="wrap">
            <div class="header">
              <div class="brand">SWORD // ANDROID SECURITY TOOLKIT</div>
              <div class="sub">Authorized Security Assessment Report</div>
            </div>
            <div class="meta-grid">
              <div class="meta-card"><div class="meta-label">Module</div><div class="meta-val">${escapeHtml(report.toolName)} (${escapeHtml(report.category)})</div></div>
              <div class="meta-card"><div class="meta-label">Target</div><div class="meta-val">${escapeHtml(report.target)}</div></div>
              <div class="meta-card"><div class="meta-label">Timestamp (UTC)</div><div class="meta-val">${escapeHtml(report.timestampIso)}</div></div>
              <div class="meta-card"><div class="meta-label">Duration / Highest Severity</div><div class="meta-val">${report.durationMs} ms · ${escapeHtml(report.highestSeverity)}</div></div>
            </div>
            <h2>Findings &amp; Recommendations</h2>
            $findingsRows
            <h2>Structured Diagnostic Output</h2>
            <pre>${escapeHtml(report.output)}</pre>
          </div>
        </body>
        </html>
        """.trimIndent()
    }

    private fun escapeHtml(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            when (ch) {
                '&' -> sb.append("&amp;")
                '<' -> sb.append("&lt;")
                '>' -> sb.append("&gt;")
                '"' -> sb.append("&quot;")
                '\'' -> sb.append("&#39;")
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }
}
