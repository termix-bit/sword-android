package com.sword.security.android.core.model

data class CapabilityItem(
    val id: String,
    val title: String,
    val state: CapabilityState,
    val detail: String,
    val androidApiNote: String
)

data class PermissionStatusItem(
    val permission: String,
    val displayName: String,
    val isGranted: Boolean,
    val isApplicableToDevice: Boolean,
    val rationale: String,
    val affectedModules: List<String>
)

data class DeviceCapabilitiesSummary(
    val androidVersion: String,
    val apiLevel: Int,
    val deviceModel: String,
    val abiList: List<String>,
    val hasInternetConnection: Boolean,
    val activeTransport: String,
    val isVpnActive: Boolean,
    val wifiEnabled: Boolean,
    val locationServicesEnabled: Boolean,
    val rootBinaryDetected: Boolean,
    val pythonRuntimeReady: Boolean,
    val pythonVersion: String,
    val capabilities: List<CapabilityItem>
)
