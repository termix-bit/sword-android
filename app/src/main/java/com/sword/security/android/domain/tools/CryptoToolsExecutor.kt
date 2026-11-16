package com.sword.security.android.domain.tools

import android.util.Base64
import com.sword.security.android.core.model.SecurityFinding
import com.sword.security.android.core.validation.InputValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLDecoder
import java.net.URLEncoder
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID

class CryptoToolsExecutor {

    private val secureRandom = SecureRandom()

    suspend fun generateHashes(
        params: Map<String, String>,
        onLog: suspend (String) -> Unit
    ): ToolExecutionPayload = withContext(Dispatchers.Default) {
        val input = params["text"].orEmpty()
        require(input.isNotEmpty()) { "Input text cannot be empty" }
        onLog("Computing SHA-256, SHA-512, and legacy reference digests...")

        val bytes = input.toByteArray(Charsets.UTF_8)
        val sha256 = digestHex(bytes, "SHA-256")
        val sha512 = digestHex(bytes, "SHA-512")
        val sha1 = digestHex(bytes, "SHA-1")
        val md5 = digestHex(bytes, "MD5")

        val sb = StringBuilder()
        sb.appendLine("Cryptographic Hash Digest Report")
        sb.appendLine("Input Length : ${bytes.size} byte(s)")
        sb.appendLine()
        sb.appendLine("SHA-256      : $sha256")
        sb.appendLine("SHA-512      : $sha512")
        sb.appendLine()
        sb.appendLine("=== Legacy Reference Only (Do Not Use for Security) ===")
        sb.appendLine("SHA-1        : $sha1")
        sb.appendLine("MD5          : $md5")

        val findings = listOf(
            SecurityFinding(
                title = "SHA-256 & SHA-512 Digests Computed",
                severity = "Info",
                detail = "Generated collision-resistant SHA-256 and SHA-512 checksums.",
                recommendation = "Use SHA-256 or SHA-512 for integrity checks; avoid MD5 and SHA-1 for security guarantees."
            )
        )

        ToolExecutionPayload(
            targetSummary = "${bytes.size} bytes",
            rawOutput = sb.toString().trim(),
            findings = findings
        )
    }

    suspend fun verifyHash(
        params: Map<String, String>,
        onLog: suspend (String) -> Unit
    ): ToolExecutionPayload = withContext(Dispatchers.Default) {
        val input = params["text"].orEmpty()
        val expectedRaw = InputValidator.validateHexString(params["expected_hash"].orEmpty()).lowercase()
        val algorithm = (params["algorithm"] ?: "SHA-256").uppercase().trim()
        require(algorithm in listOf("SHA-256", "SHA-512")) { "Only SHA-256 or SHA-512 verification is supported" }
        require(input.isNotEmpty()) { "Input text cannot be empty" }

        onLog("Performing constant-time $algorithm comparison...")
        val actual = digestHex(input.toByteArray(Charsets.UTF_8), algorithm)
        val matches = MessageDigest.isEqual(
            actual.toByteArray(Charsets.US_ASCII),
            expectedRaw.toByteArray(Charsets.US_ASCII)
        )

        val sb = StringBuilder()
        sb.appendLine("Constant-Time Hash Verification ($algorithm)")
        sb.appendLine("Expected : $expectedRaw")
        sb.appendLine("Computed : $actual")
        sb.appendLine("Result   : ${if (matches) "MATCH VERIFIED" else "MISMATCH"}")

        val findings = listOf(
            if (matches) {
                SecurityFinding(
                    title = "Integrity Digest Verified ($algorithm)",
                    severity = "Info",
                    detail = "Computed $algorithm digest matches the expected checksum in constant time.",
                    recommendation = "Data integrity verified against provided digest."
                )
            } else {
                SecurityFinding(
                    title = "Integrity Digest Mismatch ($algorithm)",
                    severity = "High",
                    detail = "Computed $algorithm digest does NOT match the expected hash.",
                    recommendation = "Do not trust modified or corrupted input data."
                )
            }
        )

        ToolExecutionPayload(
            targetSummary = "$algorithm (${if (matches) "MATCH" else "MISMATCH"})",
            rawOutput = sb.toString().trim(),
            findings = findings
        )
    }

    suspend fun encodeDecode(
        params: Map<String, String>,
        onLog: suspend (String) -> Unit
    ): ToolExecutionPayload = withContext(Dispatchers.Default) {
        val input = params["text"].orEmpty()
        val mode = params["mode"] ?: "Base64 Encode"
        require(input.isNotEmpty()) { "Input text cannot be empty" }
        onLog("Applying encoding transform: $mode...")

        val output = when (mode) {
            "Base64 Encode" -> Base64.encodeToString(input.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
            "Base64 Decode" -> {
                val decoded = Base64.decode(input.trim(), Base64.DEFAULT)
                String(decoded, Charsets.UTF_8)
            }
            "Hex Encode" -> input.toByteArray(Charsets.UTF_8).joinToString("") { "%02x".format(it) }
            "Hex Decode" -> {
                val clean = InputValidator.validateHexString(input)
                val bytes = ByteArray(clean.length / 2) { i ->
                    clean.substring(i * 2, i * 2 + 2).toInt(16).toByte()
                }
                String(bytes, Charsets.UTF_8)
            }
            "URL Encode" -> URLEncoder.encode(input, "UTF-8")
            "URL Decode" -> URLDecoder.decode(input, "UTF-8")
            "ROT13" -> input.map { ch ->
                when (ch) {
                    in 'a'..'z' -> ('a'.code + (ch.code - 'a'.code + 13) % 26).toChar()
                    in 'A'..'Z' -> ('A'.code + (ch.code - 'A'.code + 13) % 26).toChar()
                    else -> ch
                }
            }.joinToString("")
            else -> throw IllegalArgumentException("Unknown mode: $mode")
        }

        val sb = StringBuilder()
        sb.appendLine("Encoding Utility ($mode)")
        sb.appendLine("Note: Encodings and ROT13 are NOT encryption and provide zero confidentiality.")
        sb.appendLine()
        sb.appendLine("Output:")
        sb.appendLine(output)

        val findings = listOf(
            SecurityFinding(
                title = "$mode Completed",
                severity = "Info",
                detail = "Transformed ${input.length} character(s) via $mode.",
                recommendation = "Never rely on Base64, Hex, or ROT13 to protect sensitive data."
            )
        )

        ToolExecutionPayload(
            targetSummary = mode,
            rawOutput = sb.toString().trim(),
            findings = findings
        )
    }

    suspend fun generateSecureRandom(
        params: Map<String, String>,
        onLog: suspend (String) -> Unit
    ): ToolExecutionPayload = withContext(Dispatchers.Default) {
        val byteLen = InputValidator.validateIntegerInRange(params["bytes"] ?: "32", 8, 128, "Byte Length")
        onLog("Generating $byteLen cryptographically secure random bytes via SecureRandom...")

        val bytes = ByteArray(byteLen)
        secureRandom.nextBytes(bytes)

        val hex = bytes.joinToString("") { "%02x".format(it) }
        val b64 = Base64.encodeToString(bytes, Base64.NO_WRAP or Base64.URL_SAFE)
        val uuid = UUID.randomUUID().toString()

        val sb = StringBuilder()
        sb.appendLine("CSPRNG Output (java.security.SecureRandom)")
        sb.appendLine("Entropy Bytes : $byteLen (${byteLen * 8} bits)")
        sb.appendLine()
        sb.appendLine("Hex Token     : $hex")
        sb.appendLine("Base64URL     : $b64")
        sb.appendLine("UUID v4       : $uuid")

        val findings = listOf(
            SecurityFinding(
                title = "Generated ${byteLen * 8}-bit Cryptographic Secret",
                severity = "Info",
                detail = "Generated high-entropy random values using Android's SecureRandom CSPRNG.",
                recommendation = "Store generated tokens in Android Keystore or EncryptedSharedPreferences; do not log secrets."
            )
        )

        ToolExecutionPayload(
            targetSummary = "${byteLen * 8}-bit CSPRNG",
            rawOutput = sb.toString().trim(),
            findings = findings
        )
    }

    private fun digestHex(bytes: ByteArray, algorithm: String): String {
        val md = MessageDigest.getInstance(algorithm)
        return md.digest(bytes).joinToString("") { "%02x".format(it) }
    }
}
