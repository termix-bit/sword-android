package com.sword.security.android.domain.tools

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import com.sword.security.android.core.capability.PermissionManager
import com.sword.security.android.core.model.SecurityFinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WirelessToolsExecutor(
    private val context: Context,
    private val permissionManager: PermissionManager
) {

    @Suppress("DEPRECATION")
    @SuppressLint("MissingPermission")
    suspend fun inspectWifiConnection(onLog: suspend (String) -> Unit): ToolExecutionPayload =
        withContext(Dispatchers.IO) {
            onLog("Reading Android ConnectivityManager & WifiManager state...")
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            val hasPerm = permissionManager.hasWifiScanPermission()

            val activeNet = cm?.activeNetwork
            val netCaps = cm?.getNetworkCapabilities(activeNet)
            val linkProps = cm?.getLinkProperties(activeNet)
            val isWifi = netCaps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true

            val wifiInfo = runCatching { wifiManager?.connectionInfo }.getOrNull()
            val sb = StringBuilder()
            val findings = mutableListOf<SecurityFinding>()

            sb.appendLine("Android Wi-Fi Connection Diagnostics")
            sb.appendLine("Wi-Fi Radio Enabled : ${wifiManager?.isWifiEnabled == true}")
            sb.appendLine("Active Wi-Fi Route  : $isWifi")
            sb.appendLine("Location/Wi-Fi Perm : ${if (hasPerm) "Granted" else "Not Granted (SSID/BSSID redacted by Android)"}")
            sb.appendLine()

            if (wifiInfo != null && isWifi) {
                val ssid = if (hasPerm) wifiInfo.ssid?.removeSurrounding("\"") ?: "<unknown>" else "<redacted - permission required>"
                val bssid = if (hasPerm) wifiInfo.bssid ?: "<unknown>" else "<redacted>"
                val band = when {
                    wifiInfo.frequency in 2400..2500 -> "2.4 GHz"
                    wifiInfo.frequency in 4900..5900 -> "5 GHz"
                    wifiInfo.frequency >= 5925 -> "6 GHz"
                    else -> "Unknown"
                }
                sb.appendLine("SSID        : $ssid")
                sb.appendLine("BSSID       : $bssid")
                sb.appendLine("RSSI        : ${wifiInfo.rssi} dBm")
                sb.appendLine("Link Speed  : ${wifiInfo.linkSpeed} Mbps")
                sb.appendLine("Frequency   : ${wifiInfo.frequency} MHz ($band)")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    sb.appendLine("Wi-Fi Std   : ${wifiStandardName(wifiInfo.wifiStandard)}")
                }
                if (linkProps != null) {
                    sb.appendLine("IP Addresses: ${linkProps.linkAddresses.joinToString { it.toString() }}")
                    sb.appendLine("DNS Servers : ${linkProps.dnsServers.joinToString { it.hostAddress ?: "" }}")
                }

                if (wifiInfo.rssi < -78) {
                    findings += SecurityFinding(
                        title = "Weak Wi-Fi Signal Strength (${wifiInfo.rssi} dBm)",
                        severity = "Low",
                        detail = "Current RSSI indicates weak radio signal or high attenuation.",
                        recommendation = "Move closer to the access point or verify 2.4/5 GHz roaming."
                    )
                }
            } else {
                sb.appendLine("Device is not currently associated with an active Wi-Fi network.")
            }

            if (!hasPerm) {
                findings += SecurityFinding(
                    title = "Wi-Fi Identifiers Redacted by Android Privacy Policy",
                    severity = "Info",
                    detail = "Android hides SSID and BSSID until Location / Nearby Wi-Fi permission is granted.",
                    recommendation = "Open Permissions Manager to grant Wi-Fi diagnostic permissions if needed."
                )
            } else if (findings.isEmpty()) {
                findings += SecurityFinding(
                    title = "Wi-Fi Connection Diagnostics Collected",
                    severity = "Info",
                    detail = "Inspected active wireless link attributes using official Android WifiManager APIs.",
                    recommendation = "Verify DNS servers and network encryption (WPA2/WPA3) on the connected AP."
                )
            }

            ToolExecutionPayload(
                targetSummary = if (isWifi && hasPerm) (wifiInfo?.ssid?.removeSurrounding("\"") ?: "Active Wi-Fi") else "Wi-Fi Diagnostics",
                rawOutput = sb.toString().trim(),
                findings = findings
            )
        }

    @Suppress("DEPRECATION")
    @SuppressLint("MissingPermission")
    suspend fun scanNearbyWifi(onLog: suspend (String) -> Unit): ToolExecutionPayload =
        withContext(Dispatchers.IO) {
            if (!permissionManager.hasWifiScanPermission()) {
                throw SecurityException("Location / Nearby Wi-Fi Devices permission is required to read scan results.")
            }

            onLog("Requesting cached/fresh Wi-Fi scan results via Android WifiManager...")
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                ?: error("WifiManager service unavailable")

            val scanInitiated = runCatching { wifiManager.startScan() }.getOrDefault(false)
            val results = runCatching { wifiManager.scanResults.orEmpty() }.getOrDefault(emptyList())
                .sortedByDescending { it.level }

            val sb = StringBuilder()
            val findings = mutableListOf<SecurityFinding>()
            sb.appendLine("Nearby Wi-Fi Access Points (Android WifiManager)")
            sb.appendLine("Active Scan Triggered : $scanInitiated (Android 9+ throttles to 4 scans / 2 min)")
            sb.appendLine("Access Points Visible : ${results.size}")
            sb.appendLine()

            var openCount = 0
            var legacyWepCount = 0
            for (ap in results) {
                val ssid = ap.SSID?.takeIf { it.isNotBlank() } ?: "<Hidden SSID>"
                val caps = ap.capabilities.orEmpty()
                val isOpen = !caps.contains("WPA", ignoreCase = true) && !caps.contains("WEP", ignoreCase = true) && !caps.contains("SAE", ignoreCase = true)
                val isWep = caps.contains("WEP", ignoreCase = true)
                if (isOpen) openCount++
                if (isWep) legacyWepCount++

                sb.appendLine("• $ssid (${ap.BSSID})")
                sb.appendLine("  Signal=${ap.level} dBm  Freq=${ap.frequency} MHz  Security=$caps")
            }

            if (results.isEmpty()) {
                sb.appendLine("No scan results returned. Ensure Wi-Fi AND device Location Services are enabled.")
                findings += SecurityFinding(
                    title = "No Nearby Wi-Fi Results Available",
                    severity = "Info",
                    detail = "WifiManager returned 0 APs (Location toggle may be off or scan throttled).",
                    recommendation = "Turn on device Location Services and retry."
                )
            } else {
                if (legacyWepCount > 0) {
                    findings += SecurityFinding(
                        title = "Legacy WEP Network Detected Nearby ($legacyWepCount)",
                        severity = "High",
                        detail = "Detected $legacyWepCount access point(s) advertising deprecated WEP security.",
                        recommendation = "Upgrade access points from WEP to WPA2-AES or WPA3-SAE."
                    )
                }
                if (openCount > 0) {
                    findings += SecurityFinding(
                        title = "Open / Unencrypted Wi-Fi Networks Visible ($openCount)",
                        severity = "Medium",
                        detail = "$openCount nearby network(s) lack link-layer encryption.",
                        recommendation = "Avoid transmitting unencrypted traffic or use a trusted VPN on open Wi-Fi."
                    )
                }
                if (findings.isEmpty()) {
                    findings += SecurityFinding(
                        title = "Enumerated ${results.size} Nearby Wireless Network(s)",
                        severity = "Info",
                        detail = "All visible access points advertise WPA/WPA2/WPA3 protection.",
                        recommendation = "Prefer WPA3-SAE or WPA2-PSK (AES) networks."
                    )
                }
            }

            ToolExecutionPayload(
                targetSummary = "${results.size} APs",
                rawOutput = sb.toString().trim(),
                findings = findings
            )
        }

    private fun wifiStandardName(std: Int): String = when (std) {
        4 -> "Wi-Fi 4 (802.11n)"
        5 -> "Wi-Fi 5 (802.11ac)"
        6 -> "Wi-Fi 6 (802.11ax)"
        7 -> "Wi-Fi 7 (802.11be)"
        else -> "Legacy / Unknown ($std)"
    }
}
