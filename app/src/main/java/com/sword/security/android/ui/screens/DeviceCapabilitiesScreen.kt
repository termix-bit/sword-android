package com.sword.security.android.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.sword.security.android.core.i18n.SwordStrings
import com.sword.security.android.core.model.DeviceCapabilitiesSummary
import com.sword.security.android.ui.components.CapabilityBadge
import com.sword.security.android.ui.components.SectionHeader
import com.sword.security.android.ui.components.SwordCard
import com.sword.security.android.ui.components.SwordCircuitBackdrop
import com.sword.security.android.ui.components.SwordTopBar
import com.sword.security.android.ui.theme.LocalSwordColors

@Composable
fun DeviceCapabilitiesScreen(
    summary: DeviceCapabilitiesSummary?,
    languageCode: String = "en",
    onRefresh: () -> Unit,
    onBack: () -> Unit
) {
    val swordColors = LocalSwordColors.current
    val isFa = SwordStrings.isPersian(languageCode)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            SwordTopBar(
                title = SwordStrings.label("capabilities", languageCode),
                subtitle = if (isFa) "ممیزی سخت‌افزار و API اندروید" else "Android Hardware & API Audit",
                onBack = onBack,
                actions = {
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = SwordStrings.label("refresh", languageCode),
                            tint = swordColors.accent
                        )
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
                        SwordCard(borderColor = swordColors.accent.copy(alpha = 0.45f)) {
                            Text(
                                text = if (isFa) "مشخصات دستگاه" else "Device Profile",
                                style = MaterialTheme.typography.titleMedium,
                                color = swordColors.accent
                            )
                            Spacer(Modifier.height(6.dp))
                            InfoLine(if (isFa) "مدل" else "Model", summary.deviceModel)
                            InfoLine(if (isFa) "سیستم‌عامل" else "Android OS", "Android ${summary.androidVersion} (API ${summary.apiLevel})")
                            InfoLine(if (isFa) "معماری‌ها" else "Supported ABIs", summary.abiList.joinToString(", "))
                            InfoLine(if (isFa) "اتصال فعال" else "Active Transport", summary.activeTransport)
                            InfoLine("VPN", if (summary.isVpnActive) (if (isFa) "فعال" else "Yes") else (if (isFa) "غیرفعال" else "No"))
                            InfoLine(if (isFa) "وای‌فای" else "Wi-Fi Radio", if (summary.wifiEnabled) (if (isFa) "روشن" else "Enabled") else (if (isFa) "خاموش" else "Disabled"))
                            InfoLine(if (isFa) "سرویس مکان" else "Location Services", if (summary.locationServicesEnabled) (if (isFa) "روشن" else "Enabled") else (if (isFa) "خاموش" else "Disabled"))
                            InfoLine(if (isFa) "موتور پایتون" else "Embedded Python", if (summary.pythonRuntimeReady) "Python ${summary.pythonVersion}" else "Unavailable")
                        }
                    }

                    item {
                        SectionHeader(
                            title = if (isFa) "ماتریس قابلیت‌ها (${summary.capabilities.size})" else "Capability Matrix (${summary.capabilities.size})"
                        )
                    }

                    items(summary.capabilities, key = { it.id }) { cap ->
                        SwordCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = cap.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = swordColors.textPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(Modifier.width(8.dp))
                                CapabilityBadge(cap.state, languageCode = languageCode)
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(cap.detail, style = MaterialTheme.typography.bodyMedium, color = swordColors.textPrimary)
                            Spacer(Modifier.height(4.dp))
                            Text(cap.androidApiNote, style = MaterialTheme.typography.labelSmall, color = swordColors.textSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    val swordColors = LocalSwordColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = swordColors.textSecondary)
        Spacer(Modifier.width(12.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = swordColors.textPrimary)
    }
}
