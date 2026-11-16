package com.sword.security.android.domain.tools

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.exifinterface.media.ExifInterface
import com.sword.security.android.core.model.SecurityFinding
import com.sword.security.android.core.validation.InputValidator
import com.sword.security.android.data.local.db.EvidenceDao
import com.sword.security.android.data.local.db.EvidenceRecordEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.security.MessageDigest
import kotlin.coroutines.coroutineContext

class ForensicsToolsExecutor(
    private val context: Context,
    private val evidenceDao: EvidenceDao
) {

    private data class FileMeta(
        val name: String,
        val sizeBytes: Long,
        val mimeType: String
    )

    suspend fun inspectFileAndRecordEvidence(
        params: Map<String, String>,
        onLog: suspend (String) -> Unit
    ): ToolExecutionPayload = withContext(Dispatchers.IO) {
        val uriStr = params["file_uri"].orEmpty().trim()
        require(uriStr.isNotEmpty()) { "Select a file using the document picker first" }
        val uri = Uri.parse(uriStr)

        val meta = readMetadata(uri)
        onLog("Inspecting '${meta.name}' (${meta.sizeBytes} bytes, ${meta.mimeType})...")

        val sha256 = MessageDigest.getInstance("SHA-256")
        val sha512 = MessageDigest.getInstance("SHA-512")
        val md5 = MessageDigest.getInstance("MD5")
        val header = ByteArray(16)
        var headerLen = 0
        var totalRead = 0L

        openStream(uri).use { stream ->
            val buf = ByteArray(16 * 1024)
            while (true) {
                coroutineContext.ensureActive()
                val n = stream.read(buf)
                if (n <= 0) break
                if (headerLen < header.size) {
                    val copy = minOf(n, header.size - headerLen)
                    System.arraycopy(buf, 0, header, headerLen, copy)
                    headerLen += copy
                }
                sha256.update(buf, 0, n)
                sha512.update(buf, 0, n)
                md5.update(buf, 0, n)
                totalRead += n
            }
        }

        val sig = detectMagicSignature(header.copyOf(headerLen))
        val hex256 = sha256.digest().toHex()
        val hex512 = sha512.digest().toHex()
        val hexMd5 = md5.digest().toHex()

        evidenceDao.insertEvidence(
            EvidenceRecordEntity(
                fileName = meta.name,
                mimeType = meta.mimeType,
                sizeBytes = totalRead,
                detectedSignature = sig,
                sha256 = hex256,
                sha512 = hex512,
                recordedEpochMs = System.currentTimeMillis()
            )
        )

        val sb = StringBuilder()
        sb.appendLine("Digital Forensics File & Evidence Record")
        sb.appendLine("File Name          : ${meta.name}")
        sb.appendLine("MIME Type (OS)     : ${meta.mimeType}")
        sb.appendLine("Observed Size      : $totalRead bytes")
        sb.appendLine("Magic Signature    : $sig")
        sb.appendLine("Header Bytes (16B) : ${header.copyOf(headerLen).joinToString(" ") { "%02X".format(it) }}")
        sb.appendLine()
        sb.appendLine("=== Chain-of-Custody Digests (Saved to Room Evidence Log) ===")
        sb.appendLine("SHA-256 : $hex256")
        sb.appendLine("SHA-512 : $hex512")
        sb.appendLine("MD5*    : $hexMd5 (*legacy compatibility only)")

        val findings = mutableListOf<SecurityFinding>()
        findings += SecurityFinding(
            title = "Evidence Integrity Record Saved (SHA-256)",
            severity = "Info",
            detail = "Computed streaming hashes for '${meta.name}' ($totalRead bytes) and persisted chain-of-custody record.",
            recommendation = "Preserve original evidence file read-only and verify SHA-256 before external transfer."
        )

        if (sig.contains("ELF") || sig.contains("PE/DOS")) {
            findings += SecurityFinding(
                title = "Executable Binary Signature Detected ($sig)",
                severity = "Medium",
                detail = "Selected file begins with native executable headers ($sig).",
                recommendation = "Analyze suspicious binaries only inside an isolated sandbox."
            )
        }

        ToolExecutionPayload(
            targetSummary = "${meta.name} ($sig)",
            rawOutput = sb.toString().trim(),
            findings = findings
        )
    }

    suspend fun inspectExifMetadata(
        params: Map<String, String>,
        onLog: suspend (String) -> Unit
    ): ToolExecutionPayload = withContext(Dispatchers.IO) {
        val uriStr = params["file_uri"].orEmpty().trim()
        require(uriStr.isNotEmpty()) { "Select an image file using the picker first" }
        val uri = Uri.parse(uriStr)
        val meta = readMetadata(uri)

        onLog("Extracting EXIF metadata from '${meta.name}'...")
        val exif = openStream(uri).use { ExifInterface(it) }

        val tags = listOf(
            "Make" to exif.getAttribute(ExifInterface.TAG_MAKE),
            "Model" to exif.getAttribute(ExifInterface.TAG_MODEL),
            "Software" to exif.getAttribute(ExifInterface.TAG_SOFTWARE),
            "DateTime" to exif.getAttribute(ExifInterface.TAG_DATETIME),
            "DateTimeOriginal" to exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL),
            "Width x Height" to listOfNotNull(
                exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH),
                exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH)
            ).joinToString(" x ").takeIf { it.isNotBlank() },
            "Orientation" to exif.getAttribute(ExifInterface.TAG_ORIENTATION),
            "Camera Owner" to exif.getAttribute(ExifInterface.TAG_CAMERA_OWNER_NAME)
        )
        val latLong = exif.latLong

        val sb = StringBuilder()
        sb.appendLine("EXIF Metadata Audit: ${meta.name}")
        sb.appendLine("Size : ${meta.sizeBytes} bytes  MIME : ${meta.mimeType}")
        sb.appendLine()
        var foundTags = 0
        for ((k, v) in tags) {
            if (!v.isNullOrBlank()) {
                foundTags++
                sb.appendLine("• $k : $v")
            }
        }
        if (latLong != null) {
            foundTags++
            sb.appendLine("• GPS Coordinates : ${latLong[0]}, ${latLong[1]}")
        }
        if (foundTags == 0) {
            sb.appendLine("No EXIF metadata tags detected in this file.")
        }

        val findings = mutableListOf<SecurityFinding>()
        if (latLong != null) {
            findings += SecurityFinding(
                title = "Embedded GPS Location Found in Image EXIF",
                severity = "Medium",
                detail = "Image contains precise latitude/longitude coordinates (${latLong[0]}, ${latLong[1]}).",
                recommendation = "Strip EXIF GPS metadata before sharing images publicly."
            )
        }
        if (tags.any { (k, v) -> k in listOf("Make", "Model", "Software") && !v.isNullOrBlank() }) {
            findings += SecurityFinding(
                title = "Device & Software Fingerprint in EXIF",
                severity = "Low",
                detail = "Image exposes hardware model or editing software tags.",
                recommendation = "Sanitize EXIF headers when publishing sensitive media."
            )
        }
        if (findings.isEmpty()) {
            findings += SecurityFinding(
                title = "No Sensitive EXIF Location/Device Tags Detected",
                severity = "Info",
                detail = "Scanned standard EXIF metadata blocks for '${meta.name}'.",
                recommendation = "File appears clean of common EXIF location identifiers."
            )
        }

        ToolExecutionPayload(
            targetSummary = meta.name,
            rawOutput = sb.toString().trim(),
            findings = findings
        )
    }

    suspend fun extractStringsAndHex(
        params: Map<String, String>,
        onLog: suspend (String) -> Unit
    ): ToolExecutionPayload = withContext(Dispatchers.IO) {
        val uriStr = params["file_uri"].orEmpty().trim()
        require(uriStr.isNotEmpty()) { "Select a file using the picker first" }
        val minLen = InputValidator.validateIntegerInRange(params["min_length"] ?: "6", 4, 24, "Min String Length")
        val uri = Uri.parse(uriStr)
        val meta = readMetadata(uri)

        onLog("Reading bounded bytes from '${meta.name}' for hex dump & strings extraction...")
        val maxInspectBytes = 256 * 1024 // Cap inspection at 256 KB to protect memory
        val data = openStream(uri).use { stream ->
            val out = ByteArray(maxInspectBytes)
            var offset = 0
            while (offset < maxInspectBytes) {
                val n = stream.read(out, offset, maxInspectBytes - offset)
                if (n <= 0) break
                offset += n
            }
            out.copyOf(offset)
        }

        val hexLines = StringBuilder()
        val hexSlice = data.take(320).toByteArray()
        for (row in hexSlice.indices step 16) {
            val end = minOf(row + 16, hexSlice.size)
            val hexPart = (row until end).joinToString(" ") { "%02X".format(hexSlice[it]) }.padEnd(47, ' ')
            val asciiPart = (row until end).map { i ->
                val c = hexSlice[i].toInt() and 0xFF
                if (c in 32..126) c.toChar() else '.'
            }.joinToString("")
            hexLines.appendLine("%04X:  %s  |%s|".format(row, hexPart, asciiPart))
        }

        val stringsList = mutableListOf<String>()
        val cur = StringBuilder()
        for (b in data) {
            val c = b.toInt() and 0xFF
            if (c in 32..126) {
                cur.append(c.toChar())
            } else {
                if (cur.length >= minLen) {
                    stringsList += cur.toString()
                    if (stringsList.size >= 80) break
                }
                cur.clear()
            }
        }
        if (cur.length >= minLen && stringsList.size < 80) {
            stringsList += cur.toString()
        }

        val sb = StringBuilder()
        sb.appendLine("Safe File Inspection (Hex & Printable Strings): ${meta.name}")
        sb.appendLine("Inspected Bytes : ${data.size} / ${meta.sizeBytes} (bounded)")
        sb.appendLine()
        sb.appendLine("=== Hex Dump (first ${hexSlice.size} bytes) ===")
        sb.appendLine(hexLines.toString().trimEnd())
        sb.appendLine()
        sb.appendLine("=== Printable ASCII Strings (minLen=$minLen, max 80) ===")
        if (stringsList.isEmpty()) {
            sb.appendLine("No printable strings >= $minLen chars found in inspected window.")
        } else {
            stringsList.forEach { sb.appendLine(it) }
        }

        val findings = listOf(
            SecurityFinding(
                title = "Extracted ${stringsList.size} Printable String(s)",
                severity = "Info",
                detail = "Inspected ${data.size} bytes of '${meta.name}' safely inside memory cap.",
                recommendation = "Review extracted strings for hardcoded URLs, identifiers, or metadata."
            )
        )

        ToolExecutionPayload(
            targetSummary = "${meta.name} (${data.size}B)",
            rawOutput = sb.toString().trim(),
            findings = findings
        )
    }

    private fun openStream(uri: Uri): InputStream {
        return context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Unable to open selected document URI")
    }

    private fun readMetadata(uri: Uri): FileMeta {
        var name = uri.lastPathSegment ?: "unknown_file"
        var size = -1L
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst()) {
                if (nameIdx >= 0) name = cursor.getString(nameIdx) ?: name
                if (sizeIdx >= 0) size = cursor.getLong(sizeIdx)
            }
        }
        val mime = context.contentResolver.getType(uri) ?: "application/octet-stream"
        return FileMeta(name = name, sizeBytes = size, mimeType = mime)
    }

    private fun detectMagicSignature(bytes: ByteArray): String {
        if (bytes.size >= 4 && bytes[0] == 0x25.toByte() && bytes[1] == 0x50.toByte() && bytes[2] == 0x44.toByte() && bytes[3] == 0x46.toByte()) {
            return "PDF Document (%PDF)"
        }
        if (bytes.size >= 8 && bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte() && bytes[2] == 0x4E.toByte() && bytes[3] == 0x47.toByte()) {
            return "PNG Image"
        }
        if (bytes.size >= 3 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte() && bytes[2] == 0xFF.toByte()) {
            return "JPEG Image"
        }
        if (bytes.size >= 4 && bytes[0] == 0x50.toByte() && bytes[1] == 0x4B.toByte() && bytes[2] == 0x03.toByte() && bytes[3] == 0x04.toByte()) {
            return "ZIP / APK / DOCX Archive (PK)"
        }
        if (bytes.size >= 4 && bytes[0] == 0x7F.toByte() && bytes[1] == 0x45.toByte() && bytes[2] == 0x4C.toByte() && bytes[3] == 0x46.toByte()) {
            return "ELF Native Executable"
        }
        if (bytes.size >= 2 && bytes[0] == 0x4D.toByte() && bytes[1] == 0x5A.toByte()) {
            return "PE/DOS Executable (MZ)"
        }
        if (bytes.size >= 16 && String(bytes.copyOf(15), Charsets.US_ASCII) == "SQLite format 3") {
            return "SQLite 3 Database"
        }
        if (bytes.size >= 4 && bytes[0] == 0x64.toByte() && bytes[1] == 0x65.toByte() && bytes[2] == 0x78.toByte() && bytes[3] == 0x0A.toByte()) {
            return "Android Dalvik Executable (DEX)"
        }
        if (bytes.size >= 2 && bytes[0] == 0x1F.toByte() && bytes[1] == 0x8B.toByte()) {
            return "GZIP Compressed Stream"
        }
        val isText = bytes.isNotEmpty() && bytes.all {
            val c = it.toInt() and 0xFF
            c == 9 || c == 10 || c == 13 || c in 32..126
        }
        return if (isText) "Plain Text / UTF-8" else "Binary / Unknown"
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
}
