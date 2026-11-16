package com.sword.security.android.core.capability

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.sword.security.android.core.model.PermissionStatusItem

class PermissionManager(private val context: Context) {

    fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    fun hasWifiScanPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            isPermissionGranted(Manifest.permission.NEARBY_WIFI_DEVICES) &&
                isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    fun getRequiredWifiPermissionsForRequest(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.NEARBY_WIFI_DEVICES,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
    }

    fun getAllManagedPermissions(): List<PermissionStatusItem> {
        val items = mutableListOf<PermissionStatusItem>()

        items += PermissionStatusItem(
            permission = Manifest.permission.INTERNET,
            displayName = "Internet Access",
            isGranted = isPermissionGranted(Manifest.permission.INTERNET),
            isApplicableToDevice = true,
            rationale = "Required for DoH DNS lookups, HTTP/TLS inspection, RDAP WHOIS, and OSINT checks.",
            affectedModules = listOf("Network", "Web Security", "OSINT")
        )

        items += PermissionStatusItem(
            permission = Manifest.permission.ACCESS_NETWORK_STATE,
            displayName = "Network State",
            isGranted = isPermissionGranted(Manifest.permission.ACCESS_NETWORK_STATE),
            isApplicableToDevice = true,
            rationale = "Reads active transport (Wi-Fi/Cellular/VPN), DNS servers, and link properties.",
            affectedModules = listOf("Network", "Device Capabilities")
        )

        items += PermissionStatusItem(
            permission = Manifest.permission.ACCESS_WIFI_STATE,
            displayName = "Wi-Fi State",
            isGranted = isPermissionGranted(Manifest.permission.ACCESS_WIFI_STATE),
            isApplicableToDevice = true,
            rationale = "Reads current Wi-Fi link speed, frequency band, signal strength (RSSI), and scan results.",
            affectedModules = listOf("Wireless Info")
        )

        items += PermissionStatusItem(
            permission = Manifest.permission.ACCESS_FINE_LOCATION,
            displayName = "Precise Location (Wi-Fi Diagnostics)",
            isGranted = isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION),
            isApplicableToDevice = true,
            rationale = "Android requires Location permission to read Wi-Fi SSID/BSSID and nearby AP scan results.",
            affectedModules = listOf("Wireless Info", "LAN Discovery")
        )

        val nearbyApplicable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        items += PermissionStatusItem(
            permission = Manifest.permission.NEARBY_WIFI_DEVICES,
            displayName = "Nearby Wi-Fi Devices (Android 13+)",
            isGranted = if (nearbyApplicable) isPermissionGranted(Manifest.permission.NEARBY_WIFI_DEVICES) else true,
            isApplicableToDevice = nearbyApplicable,
            rationale = "Required on Android 13+ for nearby Wi-Fi diagnostics via WifiManager.",
            affectedModules = listOf("Wireless Info")
        )

        return items
    }
}
