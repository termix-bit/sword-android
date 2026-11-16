package com.sword.security.android.domain.tools

import android.content.Context
import android.net.ConnectivityManager
import com.sword.security.android.core.model.SecurityFinding
import com.sword.security.android.core.network.HttpClientFactory
import com.sword.security.android.core.validation.InputValidator
import com.sword.security.android.data.local.prefs.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Request
import java.net.Inet4Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket
import kotlin.coroutines.coroutineContext
import kotlin.system.measureTimeMillis

data class ToolExecutionPayload(
    val targetSummary: String,
    val rawOutput: String,
    val findings: List<SecurityFinding>
)

class NetworkToolsExecutor(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun inspectInterfaces(onLog: suspend (String) -> Unit): ToolExecutionPayload =
        withContext(Dispatchers.IO) {
            onLog("Enumerating active Android network interfaces and LinkProperties...")
            val sb = StringBuilder()
            val findings = mutableListOf<SecurityFinding>()

            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val activeNetwork = cm?.activeNetwork
            val linkProps = cm?.getLinkProperties(activeNetwork)

            if (linkProps != null) {
                sb.appendLine("=== Active Default Route (ConnectivityManager) ===")
                sb.appendLine("Interface Name : ${linkProps.interfaceName ?: "Unknown"}")
                sb.appendLine("MTU            : ${linkProps.mtu}")
                sb.appendLine("DNS Servers    : ${linkProps.dnsServers.joinToString { it.hostAddress ?: "" }.ifBlank { "None" }}")
                sb.appendLine("Domains        : ${linkProps.domains ?: "None"}")
                sb.appendLine("Link Addresses : ${linkProps.linkAddresses.joinToString { it.toString() }}")
                sb.appendLine("Routes Count   : ${linkProps.routes.size}")
                sb.appendLine()
            }

            sb.appendLine("=== System Network Interfaces ===")
            val interfaces = NetworkInterface.getNetworkInterfaces()?.toList().orEmpty()
            var activeCount = 0
            for (iface in interfaces) {
                coroutineContext.ensureActive()
                val addrs = iface.inetAddresses.toList()
                if (addrs.isEmpty() && !iface.isUp) continue
                if (iface.isUp && !iface.isLoopback) activeCount++
                sb.appendLine("• ${iface.name} (${iface.displayName})")
                sb.appendLine("  Up=${iface.isUp}, Loopback=${iface.isLoopback}, Multicast=${iface.supportsMulticast()}, MTU=${iface.mtu}")
                for (addr in addrs) {
                    val family = if (addr is Inet4Address) "IPv4" else "IPv6"
                    sb.appendLine("  [$family] ${addr.hostAddress}")
                }
            }

            findings += SecurityFinding(
                title = "Active Non-Loopback Interfaces Detected ($activeCount)",
                severity = "Info",
                detail = "Enumerated ${interfaces.size} total network interfaces via Android NetworkInterface API.",
                recommendation = "Verify unexpected tunnel or secondary interfaces when auditing device connectivity."
            )

            ToolExecutionPayload(
                targetSummary = linkProps?.interfaceName ?: "Device Interfaces",
                rawOutput = sb.toString().trim(),
                findings = findings
            )
        }

    suspend fun dnsLookup(
        params: Map<String, String>,
        prefs: UserPreferences,
        onLog: suspend (String) -> Unit
    ): ToolExecutionPayload = withContext(Dispatchers.IO) {
        val domain = InputValidator.validateDomain(params["domain"].orEmpty())
        val recordType = (params["record_type"] ?: "A").uppercase().trim()
        val allowedTypes = listOf("A", "AAAA", "MX", "TXT", "NS", "CNAME", "SOA")
        require(recordType in allowedTypes) { "Unsupported DNS record type: $recordType" }

        val endpoint = when (prefs.dohProvider) {
            "Google" -> "https://dns.google/resolve?name=$domain&type=$recordType"
            "Quad9" -> "https://dns.quad9.net:5053/dns-query?name=$domain&type=$recordType"
            else -> "https://cloudflare-dns.com/dns-query?name=$domain&type=$recordType"
        }

        onLog("Querying ${prefs.dohProvider} DoH for $domain ($recordType)...")
        val client = HttpClientFactory.create(prefs.networkTimeoutSeconds, prefs.proxyUrl)
        val request = Request.Builder()
            .url(endpoint)
            .header("Accept", "application/dns-json")
            .get()
            .build()

        val bodyStr = client.newCall(request).execute().use { resp ->
            if (!resp.isSuccessful) {
                error("DoH resolver returned HTTP ${resp.code}")
            }
            resp.body?.string().orEmpty()
        }

        val root = json.parseToJsonElement(bodyStr).jsonObject
        val status = root["Status"]?.jsonPrimitive?.content ?: "Unknown"
        val answers = root["Answer"]?.jsonArray.orEmpty()

        val sb = StringBuilder()
        sb.appendLine("Resolver : ${prefs.dohProvider} (DNS-over-HTTPS)")
        sb.appendLine("Domain   : $domain")
        sb.appendLine("Type     : $recordType")
        sb.appendLine("RCODE    : $status (0 = NOERROR)")
        sb.appendLine()
        if (answers.isEmpty()) {
            sb.appendLine("No $recordType records returned.")
        } else {
            sb.appendLine("=== Answers (${answers.size}) ===")
            for (ans in answers) {
                val obj = ans.jsonObject
                val name = obj["name"]?.jsonPrimitive?.content ?: domain
                val ttl = obj["TTL"]?.jsonPrimitive?.content ?: "-"
                val data = obj["data"]?.jsonPrimitive?.content ?: "-"
                sb.appendLine("$name  TTL=$ttl  $recordType  $data")
            }
        }

        val findings = listOf(
            SecurityFinding(
                title = "DNS-over-HTTPS Lookup Completed ($recordType)",
                severity = "Info",
                detail = "Resolved ${answers.size} $recordType record(s) for $domain via encrypted ${prefs.dohProvider} DoH.",
                recommendation = "Verify DNS records match authoritative hosting and mail configurations."
            )
        )

        ToolExecutionPayload(
            targetSummary = "$domain ($recordType)",
            rawOutput = sb.toString().trim(),
            findings = findings
        )
    }

    suspend fun connectivityCheck(
        params: Map<String, String>,
        prefs: UserPreferences,
        onLog: suspend (String) -> Unit
    ): ToolExecutionPayload = withContext(Dispatchers.IO) {
        val host = InputValidator.validateHostOrIp(params["host"].orEmpty())
        val port = InputValidator.validatePort(params["port"] ?: "443")
        val timeoutMs = prefs.networkTimeoutSeconds * 1000

        onLog("Resolving $host...")
        var resolvedAddresses: List<InetAddress> = emptyList()
        val dnsTime = measureTimeMillis {
            resolvedAddresses = InetAddress.getAllByName(host).toList()
        }
        val primaryIp = resolvedAddresses.firstOrNull()?.hostAddress ?: error("Failed to resolve $host")
        onLog("Resolved $host -> $primaryIp in $dnsTime ms")

        coroutineContext.ensureActive()
        onLog("Testing ICMP/echo reachability (best effort on Android)...")
        var icmpReachable = false
        val icmpTime = measureTimeMillis {
            icmpReachable = runCatching {
                InetAddress.getByName(primaryIp).isReachable(timeoutMs.coerceAtMost(4000))
            }.getOrDefault(false)
        }

        val tcpLatencies = mutableListOf<Long>()
        var tcpSuccess = 0
        repeat(3) { attempt ->
            coroutineContext.ensureActive()
            onLog("TCP handshake probe #${attempt + 1} to $primaryIp:$port...")
            val ok = runCatching {
                val elapsed = measureTimeMillis {
                    Socket().use { socket ->
                        socket.connect(InetSocketAddress(primaryIp, port), timeoutMs)
                    }
                }
                tcpLatencies += elapsed
                true
            }.getOrDefault(false)
            if (ok) tcpSuccess++
        }

        val avgTcp = if (tcpLatencies.isNotEmpty()) tcpLatencies.average().toLong() else -1L
        val sb = StringBuilder()
        sb.appendLine("Target Host    : $host")
        sb.appendLine("Resolved IPs   : ${resolvedAddresses.joinToString { it.hostAddress ?: "" }}")
        sb.appendLine("DNS Lookup     : $dnsTime ms")
        sb.appendLine("Echo Reachable : $icmpReachable (${icmpTime} ms)")
        sb.appendLine("TCP Port $port : $tcpSuccess/3 succeeded")
        if (avgTcp >= 0) {
            sb.appendLine("TCP Avg RTT    : $avgTcp ms (samples: ${tcpLatencies.joinToString { "${it}ms" }})")
        }

        val findings = mutableListOf<SecurityFinding>()
        if (tcpSuccess == 0) {
            findings += SecurityFinding(
                title = "Target TCP Port $port Unreachable",
                severity = "Low",
                detail = "Host $host resolved to $primaryIp, but TCP port $port did not accept connections.",
                recommendation = "Verify firewall rules, service state, or VPN routing if connectivity is expected."
            )
        } else {
            findings += SecurityFinding(
                title = "Host Reachable via TCP/$port",
                severity = "Info",
                detail = "$host ($primaryIp) responded in avg $avgTcp ms ($tcpSuccess/3 attempts).",
                recommendation = "No connectivity issues detected for TCP/$port."
            )
        }

        ToolExecutionPayload(
            targetSummary = "$host:$port",
            rawOutput = sb.toString().trim(),
            findings = findings
        )
    }

    suspend fun authorizedPortProbe(
        params: Map<String, String>,
        prefs: UserPreferences,
        onLog: suspend (String) -> Unit
    ): ToolExecutionPayload = withContext(Dispatchers.IO) {
        val target = InputValidator.validateHostOrIp(params["host"].orEmpty())
        val ports = InputValidator.validatePortList(params["ports"] ?: "22,80,443,8080,8443", maxPorts = 64)
        val timeoutMs = (prefs.networkTimeoutSeconds.coerceIn(2, 10)) * 500

        onLog("Resolving target $target...")
        val resolvedIp = InetAddress.getByName(target).hostAddress ?: error("Unable to resolve $target")
        onLog("Probing ${ports.size} ports on $resolvedIp (concurrency=${prefs.maxConcurrency})...")

        val semaphore = Semaphore(prefs.maxConcurrency.coerceIn(1, 8))
        val openPorts = mutableListOf<Pair<Int, Long>>()

        coroutineScope {
            ports.map { port ->
                async {
                    semaphore.withPermit {
                        coroutineContext.ensureActive()
                        var isOpen = false
                        val rtt = measureTimeMillis {
                            isOpen = runCatching {
                                Socket().use { socket ->
                                    socket.connect(InetSocketAddress(resolvedIp, port), timeoutMs)
                                }
                                true
                            }.getOrDefault(false)
                        }
                        if (isOpen) {
                            synchronized(openPorts) {
                                openPorts += port to rtt
                            }
                            onLog("[OPEN] TCP/$port ($rtt ms)")
                        }
                    }
                }
            }.awaitAll()
        }

        val sortedOpen = openPorts.sortedBy { it.first }
        val sb = StringBuilder()
        sb.appendLine("Authorized TCP Port Probe")
        sb.appendLine("Target     : $target ($resolvedIp)")
        sb.appendLine("Ports Scanned : ${ports.size}")
        sb.appendLine("Open Ports    : ${sortedOpen.size}")
        sb.appendLine()
        if (sortedOpen.isEmpty()) {
            sb.appendLine("No open TCP ports detected in the requested range.")
        } else {
            for ((port, rtt) in sortedOpen) {
                sb.appendLine("  OPEN  TCP/$port  (${guessService(port)})  rtt=${rtt}ms")
            }
        }

        val findings = mutableListOf<SecurityFinding>()
        for ((port, _) in sortedOpen) {
            val (sev, rec) = when (port) {
                21, 23 -> "High" to "Disable unencrypted legacy service (${guessService(port)}) or restrict behind VPN."
                80, 8080 -> "Low" to "Verify HTTP redirects cleanly to HTTPS and is intentionally exposed."
                22, 3389 -> "Medium" to "Restrict administrative access ($port) via firewall allow-lists or MFA/key auth."
                else -> "Info" to "Confirm TCP/$port (${guessService(port)}) is intentionally exposed."
            }
            findings += SecurityFinding(
                title = "Open TCP Port $port (${guessService(port)})",
                severity = sev,
                detail = "TCP handshake succeeded on $target ($resolvedIp) port $port.",
                recommendation = rec
            )
        }
        if (findings.isEmpty()) {
            findings += SecurityFinding(
                title = "No Open Ports in Tested Set",
                severity = "Info",
                detail = "None of the ${ports.size} tested TCP ports accepted connections on $target.",
                recommendation = "Continue maintaining minimal exposed attack surface."
            )
        }

        ToolExecutionPayload(
            targetSummary = "$target (${ports.size} ports)",
            rawOutput = sb.toString().trim(),
            findings = findings
        )
    }

    suspend fun authorizedLanDiscovery(
        params: Map<String, String>,
        prefs: UserPreferences,
        onLog: suspend (String) -> Unit
    ): ToolExecutionPayload = withContext(Dispatchers.IO) {
        val rawSubnet = params["subnet"]?.trim().orEmpty()
        val cidr = if (rawSubnet.isBlank()) {
            detectLocalWifiSubnet() ?: throw IllegalArgumentException(
                "No active IPv4 Wi-Fi/LAN subnet detected. Enter a CIDR such as 192.168.1.0/24."
            )
        } else {
            InputValidator.validateIpv4Subnet(rawSubnet, minPrefix = 24, maxPrefix = 30)
        }

        val hosts = InputValidator.expandIpv4SubnetHosts(cidr, maxHosts = 254)
        onLog("Scanning ${hosts.size} hosts in authorized local subnet $cidr...")

        val semaphore = Semaphore(prefs.maxConcurrency.coerceIn(2, 8) * 2)
        val aliveHosts = mutableListOf<Pair<String, String>>()

        coroutineScope {
            hosts.map { ip ->
                async {
                    semaphore.withPermit {
                        coroutineContext.ensureActive()
                        val reachable = runCatching {
                            val addr = InetAddress.getByName(ip)
                            if (addr.isReachable(450)) {
                                true
                            } else {
                                // Lightweight probe on common local gateway/device ports
                                listOf(80, 443, 53, 22).any { p ->
                                    runCatching {
                                        Socket().use { s ->
                                            s.connect(InetSocketAddress(ip, p), 350)
                                        }
                                        true
                                    }.getOrDefault(false)
                                }
                            }
                        }.getOrDefault(false)

                        if (reachable) {
                            val hostName = runCatching {
                                val h = InetAddress.getByName(ip).canonicalHostName
                                if (h != ip) h else ""
                            }.getOrDefault("")
                            synchronized(aliveHosts) {
                                aliveHosts += ip to hostName
                            }
                            onLog("[ACTIVE] $ip ${if (hostName.isNotEmpty()) "($hostName)" else ""}")
                        }
                    }
                }
            }.awaitAll()
        }

        val sorted = aliveHosts.sortedBy { ipToSortKey(it.first) }
        val sb = StringBuilder()
        sb.appendLine("Authorized Local Network Discovery")
        sb.appendLine("Subnet       : $cidr")
        sb.appendLine("Hosts Probed : ${hosts.size}")
        sb.appendLine("Active Hosts : ${sorted.size}")
        sb.appendLine()
        if (sorted.isEmpty()) {
            sb.appendLine("No responsive hosts found (devices may block ICMP and common TCP probes).")
        } else {
            for ((ip, name) in sorted) {
                sb.appendLine("  • $ip${if (name.isNotEmpty()) "  [$name]" else ""}")
            }
        }

        val findings = listOf(
            SecurityFinding(
                title = "Discovered ${sorted.size} Responsive Host(s) in $cidr",
                severity = "Info",
                detail = "Non-root Android LAN probe (ICMP + TCP 22/53/80/443) identified ${sorted.size} active endpoints.",
                recommendation = "Review discovered hosts against your authorized local network inventory."
            )
        )

        ToolExecutionPayload(
            targetSummary = cidr,
            rawOutput = sb.toString().trim(),
            findings = findings
        )
    }

    private fun detectLocalWifiSubnet(): String? {
        val ifaces = NetworkInterface.getNetworkInterfaces()?.toList().orEmpty()
        for (iface in ifaces) {
            if (!iface.isUp || iface.isLoopback) continue
            for (addr in iface.inetAddresses.toList()) {
                if (addr is Inet4Address && !addr.isLoopbackAddress && addr.isSiteLocalAddress) {
                    val octets = addr.hostAddress?.split(".") ?: continue
                    if (octets.size == 4) {
                        return "${octets[0]}.${octets[1]}.${octets[2]}.0/24"
                    }
                }
            }
        }
        return null
    }

    private fun ipToSortKey(ip: String): Long {
        val parts = ip.split(".").mapNotNull { it.toLongOrNull() }
        if (parts.size != 4) return 0L
        return (parts[0] shl 24) or (parts[1] shl 16) or (parts[2] shl 8) or parts[3]
    }

    private fun guessService(port: Int): String = when (port) {
        21 -> "FTP"
        22 -> "SSH"
        23 -> "Telnet"
        25 -> "SMTP"
        53 -> "DNS"
        80 -> "HTTP"
        110 -> "POP3"
        143 -> "IMAP"
        443 -> "HTTPS"
        445 -> "SMB"
        3306 -> "MySQL"
        3389 -> "RDP"
        5432 -> "PostgreSQL"
        6379 -> "Redis"
        8080 -> "HTTP-Alt"
        8443 -> "HTTPS-Alt"
        else -> "TCP"
    }
}
