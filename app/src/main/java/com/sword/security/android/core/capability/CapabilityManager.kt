package com.sword.security.android.core.capability

import android.content.Context
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import com.sword.security.android.core.model.CapabilityItem
import com.sword.security.android.core.model.CapabilityState
import com.sword.security.android.core.model.DeviceCapabilitiesSummary
import com.sword.security.android.core.model.ToolDescriptor
import com.sword.security.android.core.python.PythonBridge
import java.io.File

class CapabilityManager(
    private val context: Context,
    private val permissionManager: PermissionManager,
    private val pythonBridge: PythonBridge
) {

    suspend fun inspectDeviceCapabilities(): DeviceCapabilitiesSummary {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val activeNetwork = cm?.activeNetwork
        val caps = cm?.getNetworkCapabilities(activeNetwork)

        val hasInternet = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        val isWifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        val isCellular = caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
        val isEthernet = caps?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true
        val isVpn = caps?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true

        val transportLabel = buildList {
            if (isWifi) add("Wi-Fi")
            if (isCellular) add("Cellular")
            if (isEthernet) add("Ethernet")
            if (isVpn) add("VPN")
        }.ifEmpty { listOf("Offline / None") }.joinToString(" + ")

        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val wifiEnabled = runCatching { wifiManager?.isWifiEnabled == true }.getOrDefault(false)

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        val locationEnabled = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                locationManager?.isLocationEnabled == true
            } else {
                @Suppress("DEPRECATION")
                locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true ||
                    locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
            }
        }.getOrDefault(false)

        val rootDetected = checkRootBinaryPresence()
        val pythonStatus = pythonBridge.getRuntimeStatus()
        val wifiPermissionGranted = permissionManager.hasWifiScanPermission()

        val items = listOf(
            CapabilityItem(
                id = "cap_network_stack",
                title = "Android Network & DoH Stack",
                state = if (hasInternet) CapabilityState.AVAILABLE else CapabilityState.AVAILABLE_WITH_LIMITATIONS,
                detail = if (hasInternet) "Validated connectivity via $transportLabel." else "No validated internet connection; local interface diagnostics still work.",
                androidApiNote = "Uses ConnectivityManager, NetworkInterface, and HTTPS DoH (no raw CLI binaries)."
            ),
            CapabilityItem(
                id = "cap_tls_http",
                title = "TLS & HTTP Security Inspection",
                state = if (hasInternet) CapabilityState.AVAILABLE else CapabilityState.AVAILABLE_WITH_LIMITATIONS,
                detail = "OkHttp + Java X.509 SSLSocket trust validation active (cleartext disabled in app).",
                androidApiNote = "Android API ${Build.VERSION.SDK_INT} system trust store."
            ),
            CapabilityItem(
                id = "cap_wifi_diag",
                title = "Wi-Fi Connection & Nearby Scan",
                state = when {
                    !wifiPermissionGranted -> CapabilityState.REQUIRES_PERMISSION
                    !locationEnabled -> CapabilityState.AVAILABLE_WITH_LIMITATIONS
                    else -> CapabilityState.AVAILABLE_WITH_LIMITATIONS
                },
                detail = when {
                    !wifiPermissionGranted -> "Grant Location / Nearby Wi-Fi permission to inspect SSID/BSSID and scan results."
                    !locationEnabled -> "Permission granted, but device Location Services are OFF; Android redacts SSID/scan results."
                    else -> "Available with Android 9+ foreground scan throttling (max 4 scans per 2 minutes)."
                },
                androidApiNote = "WifiManager API subjected to Android location & background scan throttling."
            ),
            CapabilityItem(
                id = "cap_python_bridge",
                title = "Embedded Python 3.10 Engine",
                state = if (pythonStatus.available) CapabilityState.AVAILABLE else CapabilityState.UNSUPPORTED,
                detail = if (pythonStatus.available) {
                    "${pythonStatus.engine} (${pythonStatus.pythonVersion}) bundled inside APK."
                } else {
                    "Embedded Python unavailable: ${pythonStatus.error ?: "not initialized"}."
                },
                androidApiNote = "Chaquopy in-process Python runtime; executes on Dispatchers.IO."
            ),
            CapabilityItem(
                id = "cap_saf_forensics",
                title = "SAF File & EXIF Forensics",
                state = CapabilityState.AVAILABLE,
                detail = "Inspects user-selected files via Storage Access Framework without broad storage permissions.",
                androidApiNote = "Scoped Storage compliant (ACTION_OPEN_DOCUMENT + ExifInterface)."
            ),
            CapabilityItem(
                id = "cap_raw_sockets",
                title = "Raw Packet Crafting / Monitor Mode",
                state = CapabilityState.UNSUPPORTED,
                detail = "Android non-rooted sandbox blocks CAP_NET_RAW / monitor mode; Sword excludes offensive deauth/evil-twin tools.",
                androidApiNote = if (rootDetected) "su binary detected, but Sword operates safely without root elevation." else "Standard non-rooted Android sandbox."
            )
        )

        return DeviceCapabilitiesSummary(
            androidVersion = Build.VERSION.RELEASE ?: "Unknown",
            apiLevel = Build.VERSION.SDK_INT,
            deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
            abiList = Build.SUPPORTED_ABIS.toList(),
            hasInternetConnection = hasInternet,
            activeTransport = transportLabel,
            isVpnActive = isVpn,
            wifiEnabled = wifiEnabled,
            locationServicesEnabled = locationEnabled,
            rootBinaryDetected = rootDetected,
            pythonRuntimeReady = pythonStatus.available,
            pythonVersion = pythonStatus.pythonVersion,
            capabilities = items
        )
    }

    fun resolveToolCapability(tool: ToolDescriptor, summary: DeviceCapabilitiesSummary? = null): CapabilityState {
        if (!tool.isExecutable) {
            return CapabilityState.UNSUPPORTED
        }
        for (perm in tool.requiredPermissions) {
            if (!permissionManager.isPermissionGranted(perm)) {
                return CapabilityState.REQUIRES_PERMISSION
            }
        }
        if (tool.id == "wifi_nearby_scan" || tool.id == "wifi_connection_info") {
            if (!permissionManager.hasWifiScanPermission()) {
                return CapabilityState.REQUIRES_PERMISSION
            }
            if (summary != null && !summary.locationServicesEnabled) {
                return CapabilityState.AVAILABLE_WITH_LIMITATIONS
            }
            if (tool.id == "wifi_nearby_scan") {
                return CapabilityState.AVAILABLE_WITH_LIMITATIONS
            }
        }
        if (tool.id == "osint_username_check" || tool.id == "custom_python_runner") {
            if (summary != null && !summary.pythonRuntimeReady) {
                return CapabilityState.AVAILABLE_WITH_LIMITATIONS
            }
        }
        if (tool.requiresNetwork && summary != null && !summary.hasInternetConnection) {
            return CapabilityState.AVAILABLE_WITH_LIMITATIONS
        }
        if (tool.staticLimitationReason != null) {
            return CapabilityState.AVAILABLE_WITH_LIMITATIONS
        }
        return CapabilityState.AVAILABLE
    }

    private fun checkRootBinaryPresence(): Boolean {
        val paths = listOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/su/bin/su",
            "/magisk/.core/bin/su"
        )
        return paths.any { path ->
            runCatching { File(path).exists() }.getOrDefault(false)
        }
    }
}
