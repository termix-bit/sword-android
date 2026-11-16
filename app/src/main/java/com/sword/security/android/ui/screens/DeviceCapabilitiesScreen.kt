package com.sword.security.android.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sword.security.android.core.model.DeviceCapabilitiesSummary
import com.sword.security.android.ui.components.CapabilityBadge
import com.sword.security.android.ui.components.SectionHeader
import com.sword.security.android.ui.components.SwordCard
import com.sword.security.android.ui.components.SwordCircuitBackdrop
import com.sword.security.android.ui.components.SwordTopBar
import com.sword.security.android.ui.theme.SwordBackground
import com.sword.security.android.ui.theme.SwordPrimaryCyan
import com.sword.security.android.ui.theme.SwordTextPrimary
import com.sword.security.android.ui.theme.SwordTextSecondary

@Composable
fun DeviceCapabilitiesScreen(
    summary: DeviceCapabilitiesSummary?,
    onRefresh: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = SwordBackground,
        topBar = {
            SwordTopBar(
                title = "Device Capabilities",
                subtitle = "Android Hardware & API Audit",
                onBack = onBack,
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Capabilities", tint = SwordPrimaryCyan)
                    }
                }
            )
        }
    ) { innerPadding ->
        SwordCircuitBackdrop(modifier = Modifier.padding(innerPadding)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (summary != null) {
                    item {
                        SwordCard(borderColor = SwordPrimaryCyan.copy(alpha = 0.4f)) {
                            Text("Device Profile", style = MaterialTheme.typography.titleMedium, color = SwordPrimaryCyan)
                            Spacer(Modifier.height(6.dp))
                            InfoLine("Model", summary.deviceModel)
                            InfoLine("Android OS", "Android ${summary.androidVersion} (API ${summary.apiLevel})")
                            InfoLine("Supported ABIs", summary.abiList.joinToString(", "))
                            InfoLine("Active Transport", summary.activeTransport)
                            InfoLine("VPN Active", if (summary.isVpnActive) "Yes" else "No")
                            InfoLine("Wi-Fi Radio", if (summary.wifiEnabled) "Enabled" else "Disabled")
                            InfoLine("Location Services", if (summary.locationServicesEnabled) "Enabled" else "Disabled")
                            InfoLine("Optional su Binary", if (summary.rootBinaryDetected) "Detected (Sword runs non-root)" else "Not detected (Standard Sandbox)")
                            InfoLine("Embedded Python", if (summary.pythonRuntimeReady) "Active (${summary.pythonVersion})" else "Unavailable")
                        }
                    }

                    item {
                        SectionHeader(title = "Capability Matrix (${summary.capabilities.size})")
                    }

                    items(summary.capabilities, key = { it.id }) { cap ->
                        SwordCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(cap.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                CapabilityBadge(cap.state)
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(cap.detail, style = MaterialTheme.typography.bodyMedium, color = SwordTextPrimary)
                            Spacer(Modifier.height(4.dp))
                            Text(cap.androidApiNote, style = MaterialTheme.typography.labelSmall, color = SwordTextSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = SwordTextSecondary)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = SwordTextPrimary)
    }
}
