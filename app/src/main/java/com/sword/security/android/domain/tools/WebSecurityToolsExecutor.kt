package com.sword.security.android.domain.tools

import com.sword.security.android.core.model.SecurityFinding
import com.sword.security.android.core.network.HttpClientFactory
import com.sword.security.android.core.validation.InputValidator
import com.sword.security.android.data.local.prefs.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.net.InetSocketAddress
import java.security.cert.X509Certificate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SNIHostName
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

class WebSecurityToolsExecutor {

    suspend fun inspectHttpHeaders(
        params: Map<String, String>,
        prefs: UserPreferences,
        onLog: suspend (String) -> Unit
    ): ToolExecutionPayload = withContext(Dispatchers.IO) {
        val url = InputValidator.validateHttpUrl(params["url"].orEmpty())
        onLog("Sending HTTP GET inspection request to $url...")

        val client = HttpClientFactory.create(
            timeoutSeconds = prefs.networkTimeoutSeconds,
            proxyUrl = prefs.proxyUrl,
            followRedirects = true
        )
        val req = Request.Builder()
            .url(url)
            .header("User-Agent", "SwordAndroidToolkit/1.0")
            .get()
            .build()

        val sb = StringBuilder()
        val findings = mutableListOf<SecurityFinding>()

        client.newCall(req).execute().use { resp ->
            val finalUrl = resp.request.url.toString()
            val headers = resp.headers
            sb.appendLine("Requested URL : $url")
            sb.appendLine("Final URL     : $finalUrl")
            sb.appendLine("HTTP Status   : ${resp.code} ${resp.message}")
            sb.appendLine("Protocol      : ${resp.protocol}")
            sb.appendLine()
            sb.appendLine("=== Response Headers ===")
            for (i in 0 until headers.size) {
                sb.appendLine("${headers.name(i)}: ${headers.value(i)}")
            }

            if (!finalUrl.startsWith("https://")) {
                findings += SecurityFinding(
                    title = "Endpoint Not Enforcing HTTPS",
                    severity = "High",
                    detail = "Final response URL ($finalUrl) is served over unencrypted HTTP.",
                    recommendation = "Redirect all HTTP traffic to HTTPS and enable HSTS."
                )
            }

            val hsts = headers["Strict-Transport-Security"]
            if (hsts == null) {
                findings += SecurityFinding(
                    title = "Missing Strict-Transport-Security (HSTS)",
                    severity = "Medium",
                    detail = "Strict-Transport-Security header was not returned.",
                    recommendation = "Add 'Strict-Transport-Security: max-age=31536000; includeSubDomains'."
                )
            }

            if (headers["Content-Security-Policy"] == null) {
                findings += SecurityFinding(
                    title = "Missing Content-Security-Policy (CSP)",
                    severity = "Medium",
                    detail = "No Content-Security-Policy header found to mitigate XSS and injection.",
                    recommendation = "Define a restrictive Content-Security-Policy header."
                )
            }

            if (headers["X-Frame-Options"] == null &&
                headers["Content-Security-Policy"]?.contains("frame-ancestors", ignoreCase = true) != true
            ) {
                findings += SecurityFinding(
                    title = "Missing Clickjacking Protection",
                    severity = "Low",
                    detail = "Neither X-Frame-Options nor CSP frame-ancestors is configured.",
                    recommendation = "Set X-Frame-Options: DENY or SAMEORIGIN, or CSP frame-ancestors."
                )
            }

            if (headers["X-Content-Type-Options"]?.equals("nosniff", ignoreCase = true) != true) {
                findings += SecurityFinding(
                    title = "Missing X-Content-Type-Options: nosniff",
                    severity = "Low",
                    detail = "Browser MIME-sniffing protection header is absent or misconfigured.",
                    recommendation = "Set 'X-Content-Type-Options: nosniff'."
                )
            }

            if (headers["Referrer-Policy"] == null) {
                findings += SecurityFinding(
                    title = "Missing Referrer-Policy",
                    severity = "Info",
                    detail = "Referrer-Policy header is not explicitly declared.",
                    recommendation = "Set 'Referrer-Policy: strict-origin-when-cross-origin' or 'no-referrer'."
                )
            }

            val serverBanner = headers["Server"]
            if (!serverBanner.isNullOrBlank()) {
                findings += SecurityFinding(
                    title = "Server Banner Disclosed ($serverBanner)",
                    severity = "Info",
                    detail = "Server header exposes software/platform details: $serverBanner.",
                    recommendation = "Consider minimizing or stripping detailed server version headers."
                )
            }

            val cookies = headers.values("Set-Cookie")
            for (cookie in cookies) {
                val lower = cookie.lowercase(Locale.US)
                if (!lower.contains("secure") || !lower.contains("httponly")) {
                    val cookieName = cookie.substringBefore("=")
                    findings += SecurityFinding(
                        title = "Cookie Missing Security Attributes ($cookieName)",
                        severity = "Medium",
                        detail = "Set-Cookie for '$cookieName' is missing Secure and/or HttpOnly flags.",
                        recommendation = "Mark sensitive session cookies with Secure, HttpOnly, and SameSite attributes."
                    )
                }
            }

            if (findings.isEmpty()) {
                findings += SecurityFinding(
                    title = "Strong Baseline Security Headers Present",
                    severity = "Info",
                    detail = "HSTS, CSP, clickjacking, and MIME-sniffing headers are all present.",
                    recommendation = "Periodically audit CSP directives for overly permissive sources."
                )
            }
        }

        ToolExecutionPayload(
            targetSummary = url,
            rawOutput = sb.toString().trim(),
            findings = findings
        )
    }

    suspend fun inspectTlsCertificate(
        params: Map<String, String>,
        prefs: UserPreferences,
        onLog: suspend (String) -> Unit
    ): ToolExecutionPayload = withContext(Dispatchers.IO) {
        val domain = InputValidator.validateDomain(params["domain"].orEmpty())
        val port = InputValidator.validatePort(params["port"] ?: "443")
        val timeoutMs = prefs.networkTimeoutSeconds * 1000

        onLog("Establishing TLS handshake with $domain:$port...")
        val factory = SSLSocketFactory.getDefault() as SSLSocketFactory
        val sb = StringBuilder()
        val findings = mutableListOf<SecurityFinding>()

        val socket = factory.createSocket() as SSLSocket
        socket.use { tlsSocket ->
            val sslParams = tlsSocket.sslParameters
            sslParams.serverNames = listOf(SNIHostName(domain))
            sslParams.endpointIdentificationAlgorithm = "HTTPS"
            tlsSocket.sslParameters = sslParams

            tlsSocket.connect(InetSocketAddress(domain, port), timeoutMs)
            tlsSocket.soTimeout = timeoutMs
            tlsSocket.startHandshake()

            val session = tlsSocket.session
            val hostnameVerified = HttpsURLConnection.getDefaultHostnameVerifier().verify(domain, session)
            val certs = session.peerCertificates.filterIsInstance<X509Certificate>()
            val leaf = certs.firstOrNull() ?: error("No X.509 certificate presented by peer")

            val dateFmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.US)
            val nowMs = System.currentTimeMillis()
            val daysRemaining = TimeUnit.MILLISECONDS.toDays(leaf.notAfter.time - nowMs)

            val sans = runCatching {
                leaf.subjectAlternativeNames?.mapNotNull { entry ->
                    entry.getOrNull(1)?.toString()
                }.orEmpty()
            }.getOrDefault(emptyList())

            sb.appendLine("Target          : $domain:$port")
            sb.appendLine("TLS Protocol    : ${session.protocol}")
            sb.appendLine("Cipher Suite    : ${session.cipherSuite}")
            sb.appendLine("Hostname Match  : $hostnameVerified")
            sb.appendLine("Chain Length    : ${certs.size}")
            sb.appendLine()
            sb.appendLine("=== Leaf X.509 Certificate ===")
            sb.appendLine("Subject         : ${leaf.subjectX500Principal.name}")
            sb.appendLine("Issuer          : ${leaf.issuerX500Principal.name}")
            sb.appendLine("Serial (Hex)    : ${leaf.serialNumber.toString(16)}")
            sb.appendLine("Sig Algorithm   : ${leaf.sigAlgName}")
            sb.appendLine("Valid From      : ${dateFmt.format(leaf.notBefore)}")
            sb.appendLine("Valid Until     : ${dateFmt.format(leaf.notAfter)} ($daysRemaining days remaining)")
            if (sans.isNotEmpty()) {
                sb.appendLine("SANs (${sans.size})      : ${sans.take(12).joinToString(", ")}")
            }

            if (!hostnameVerified) {
                findings += SecurityFinding(
                    title = "TLS Hostname Verification Mismatch",
                    severity = "Critical",
                    detail = "Certificate does not match hostname $domain.",
                    recommendation = "Install a valid certificate containing $domain in SANs."
                )
            }
            if (daysRemaining < 0) {
                findings += SecurityFinding(
                    title = "Expired TLS Certificate",
                    severity = "Critical",
                    detail = "Certificate expired ${-daysRemaining} day(s) ago (${dateFmt.format(leaf.notAfter)}).",
                    recommendation = "Renew and deploy a valid TLS certificate immediately."
                )
            } else if (daysRemaining < 21) {
                findings += SecurityFinding(
                    title = "TLS Certificate Expiring Soon ($daysRemaining days)",
                    severity = "Medium",
                    detail = "Certificate expires on ${dateFmt.format(leaf.notAfter)}.",
                    recommendation = "Renew certificate or verify automated ACME/Certbot renewal."
                )
            }
            if (session.protocol in listOf("TLSv1", "TLSv1.1")) {
                findings += SecurityFinding(
                    title = "Legacy TLS Protocol Negotiated (${session.protocol})",
                    severity = "High",
                    detail = "Server negotiated deprecated ${session.protocol}.",
                    recommendation = "Disable TLS 1.0/1.1 and enforce TLS 1.2 or TLS 1.3."
                )
            }
            if (leaf.sigAlgName.contains("SHA1", ignoreCase = true)) {
                findings += SecurityFinding(
                    title = "Weak Certificate Signature Algorithm (${leaf.sigAlgName})",
                    severity = "High",
                    detail = "Leaf certificate uses deprecated SHA-1 signature.",
                    recommendation = "Re-issue certificate using SHA-256 or stronger."
                )
            }
            if (findings.isEmpty()) {
                findings += SecurityFinding(
                    title = "Valid TLS Configuration (${session.protocol})",
                    severity = "Info",
                    detail = "Certificate valid for $daysRemaining more days using ${session.cipherSuite}.",
                    recommendation = "Continue monitoring certificate expiration and TLS cipher posture."
                )
            }
        }

        ToolExecutionPayload(
            targetSummary = "$domain:$port",
            rawOutput = sb.toString().trim(),
            findings = findings
        )
    }

    suspend fun authorizedSiteConfigCheck(
        params: Map<String, String>,
        prefs: UserPreferences,
        onLog: suspend (String) -> Unit
    ): ToolExecutionPayload = withContext(Dispatchers.IO) {
        val domain = InputValidator.validateDomain(params["domain"].orEmpty())
        val client = HttpClientFactory.create(
            timeoutSeconds = prefs.networkTimeoutSeconds,
            proxyUrl = prefs.proxyUrl,
            followRedirects = false
        )

        val sb = StringBuilder()
        val findings = mutableListOf<SecurityFinding>()

        onLog("Checking HTTP -> HTTPS redirect behavior for $domain...")
        // Check well-known security.txt and robots.txt over HTTPS
        val secTxtUrl = "https://$domain/.well-known/security.txt"
        onLog("Fetching $secTxtUrl...")
        val secResp = runCatching {
            client.newCall(Request.Builder().url(secTxtUrl).get().build()).execute().use { r ->
                r.code to (r.body?.string()?.take(2000).orEmpty())
            }
        }.getOrNull()

        val robotsUrl = "https://$domain/robots.txt"
        onLog("Fetching $robotsUrl...")
        val robotsResp = runCatching {
            client.newCall(Request.Builder().url(robotsUrl).get().build()).execute().use { r ->
                r.code to (r.body?.string()?.take(2000).orEmpty())
            }
        }.getOrNull()

        sb.appendLine("Authorized Website Configuration Audit: $domain")
        sb.appendLine()
        sb.appendLine("=== /.well-known/security.txt ===")
        if (secResp != null && secResp.first == 200) {
            sb.appendLine("Status: 200 OK")
            sb.appendLine(secResp.second.trim())
            findings += SecurityFinding(
                title = "RFC 9116 security.txt Published",
                severity = "Info",
                detail = "Domain publishes security contact metadata at /.well-known/security.txt.",
                recommendation = "Ensure security.txt contact and Expires fields remain up to date."
            )
        } else {
            sb.appendLine("Status: ${secResp?.first ?: "Unreachable"} (Not published)")
            findings += SecurityFinding(
                title = "No /.well-known/security.txt Found",
                severity = "Low",
                detail = "Domain does not publish a security disclosure contact file.",
                recommendation = "Consider publishing /.well-known/security.txt per RFC 9116."
            )
        }

        sb.appendLine()
        sb.appendLine("=== /robots.txt ===")
        if (robotsResp != null && robotsResp.first == 200) {
            sb.appendLine("Status: 200 OK")
            sb.appendLine(robotsResp.second.trim())
            val sensitiveHints = listOf("admin", "backup", ".git", ".env", "config", "internal")
                .filter { robotsResp.second.contains(it, ignoreCase = true) }
            if (sensitiveHints.isNotEmpty()) {
                findings += SecurityFinding(
                    title = "Sensitive Path Hints in robots.txt",
                    severity = "Low",
                    detail = "robots.txt references paths containing: ${sensitiveHints.joinToString(", ")}.",
                    recommendation = "Never rely on robots.txt for access control; enforce authentication server-side."
                )
            }
        } else {
            sb.appendLine("Status: ${robotsResp?.first ?: "Unreachable"}")
        }

        ToolExecutionPayload(
            targetSummary = domain,
            rawOutput = sb.toString().trim(),
            findings = findings
        )
    }
}
