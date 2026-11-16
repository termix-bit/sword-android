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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sword.security.android.core.model.DeviceCapabilitiesSummary
import com.sword.security.android.core.model.ModuleCategory
import com.sword.security.android.core.model.PermissionStatusItem
import com.sword.security.android.data.local.db.ActivityLogEntity
import com.sword.security.android.data.local.db.ReportMetadataEntity
import com.sword.security.android.ui.components.SectionHeader
import com.sword.security.android.ui.components.SeverityBadge
import com.sword.security.android.ui.components.SwordCard
import com.sword.security.android.ui.components.SwordCircuitBackdrop
import com.sword.security.android.ui.components.SwordLogoBadge
import com.sword.security.android.ui.theme.SwordPrimaryCyan
import com.sword.security.android.ui.theme.SwordSecondaryTurquoise
import com.sword.security.android.ui.theme.SwordSuccess
import com.sword.security.android.ui.theme.SwordTextPrimary
import com.sword.security.android.ui.theme.SwordTextSecondary
import com.sword.security.android.ui.theme.SwordWarning
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    capabilities: DeviceCapabilitiesSummary?,
    permissions: List<PermissionStatusItem>,
    recentActivity: List<ActivityLogEntity>,
    recentReports: List<ReportMetadataEntity>,
    onOpenCategory: (ModuleCategory) -> Unit,
    onOpenToolConfig: (String) -> Unit,
    onOpenCapabilities: () -> Unit,
    onOpenPermissions: () -> Unit,
    onOpenReports: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit
) {
    val grantedCount = permissions.count { it.isGranted }
    val totalPerms = permissions.size.coerceAtLeast(1)

    SwordCircuitBackdrop {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Branding Header
            item {
                SwordCard(borderColor = SwordPrimaryCyan.copy(alpha = 0.4f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SwordLogoBadge(size = 52.dp)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "SWORD",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                )
                                Text(
                                    text = "Android Security Toolkit",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = SwordPrimaryCyan
                                )
                            }
                        }
                        Row {
                            IconButton(onClick = onOpenAbout) {
                                Icon(Icons.Default.Info, contentDescription = "About Sword", tint = SwordPrimaryCyan)
                            }
                            IconButton(onClick = onOpenSettings) {
                                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = SwordTextSecondary)
                            }
                        }
                    }
                }
            }

            // Quick Actions
            item {
                SectionHeader(title = "Quick Actions")
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        QuickActionButton("DNS Lookup", Icons.Default.Dns) {
                            onOpenToolConfig("net_dns_lookup")
                        }
                    }
                    item {
                        QuickActionButton("TLS Check", Icons.Default.Language) {
                            onOpenToolConfig("web_tls_inspector")
                        }
                    }
                    item {
                        QuickActionButton("HTTP Headers", Icons.Default.Public) {
                            onOpenToolConfig("web_header_audit")
                        }
                    }
                    item {
                        QuickActionButton("SHA-256 Hash", Icons.Default.Key) {
                            onOpenToolConfig("crypto_hash_gen")
                        }
                    }
                    item {
                        QuickActionButton("File Forensics", Icons.Default.Fingerprint) {
                            onOpenToolConfig("forensics_file_inspect")
                        }
                    }
                }
            }

            // Device Capability & Permission & App Health Status
            item {
                SectionHeader(title = "Toolkit & Device Health", actionText = "Inspect", onAction = onOpenCapabilities)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SwordCard(
                        modifier = Modifier.weight(1f),
                        onClick = onOpenCapabilities
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Memory, contentDescription = null, tint = SwordPrimaryCyan, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Capabilities", style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = capabilities?.activeTransport ?: "Checking...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (capabilities?.hasInternetConnection == true) SwordSuccess else SwordWarning,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (capabilities?.pythonRuntimeReady == true) {
                                "Python ${capabilities.pythonVersion} Ready"
                            } else {
                                "Native Engine Active"
                            },
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    SwordCard(
                        modifier = Modifier.weight(1f),
                        onClick = onOpenPermissions
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = SwordSecondaryTurquoise, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Permissions", style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "$grantedCount / $totalPerms Granted",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (grantedCount == totalPerms) SwordSuccess else SwordPrimaryCyan
                        )
                        Text(
                            text = "API ${capabilities?.apiLevel ?: "-"} • ${if (capabilities?.rootBinaryDetected == true) "su present" else "Non-Root"}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            // Module Categories
            item {
                SectionHeader(title = "Module Categories")
                Spacer(Modifier.height(8.dp))
                val categories = ModuleCategory.entries
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    categories.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            pair.forEach { cat ->
                                SwordCard(
                                    modifier = Modifier.weight(1f),
                                    onClick = { onOpenCategory(cat) }
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = categoryIcon(cat),
                                            contentDescription = null,
                                            tint = SwordPrimaryCyan,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = cat.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = cat.subtitle,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            if (pair.size == 1) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Recent Reports
            item {
                SectionHeader(title = "Recent Reports", actionText = "All Reports", onAction = onOpenReports)
                Spacer(Modifier.height(8.dp))
                if (recentReports.isEmpty()) {
                    SwordCard {
                        Text(
                            text = "No reports generated yet. Run any diagnostic module to produce a JSON & HTML report.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        recentReports.take(3).forEach { report ->
                            SwordCard(onClick = onOpenReports) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = report.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    SeverityBadge(report.highestSeverity)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "${formatEpoch(report.createdEpochMs)} • ${report.findingsCount} finding(s)",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }

            // Recent Activities
            item {
                SectionHeader(title = "Recent Activity", actionText = "History", onAction = onOpenHistory)
                Spacer(Modifier.height(8.dp))
                if (recentActivity.isEmpty()) {
                    SwordCard {
                        Text(
                            text = "No execution activity recorded on this device yet.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        recentActivity.take(5).forEach { act ->
                            SwordCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(act.title, style = MaterialTheme.typography.titleMedium, color = SwordTextPrimary)
                                    Text(formatEpoch(act.timestampEpochMs), style = MaterialTheme.typography.labelSmall)
                                }
                                Spacer(Modifier.height(2.dp))
                                Text(act.detail, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionButton(label: String, icon: ImageVector, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick) {
        Icon(icon, contentDescription = null, tint = SwordPrimaryCyan, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, color = SwordTextPrimary)
    }
}

fun categoryIcon(category: ModuleCategory): ImageVector = when (category) {
    ModuleCategory.NETWORK -> Icons.Default.Dns
    ModuleCategory.WEB_SECURITY -> Icons.Default.Language
    ModuleCategory.OSINT -> Icons.Default.Public
    ModuleCategory.WIRELESS -> Icons.Default.Wifi
    ModuleCategory.CRYPTOGRAPHY -> Icons.Default.Key
    ModuleCategory.FORENSICS -> Icons.Default.FolderOpen
    ModuleCategory.REPORTS -> Icons.Default.Description
    ModuleCategory.CUSTOM_TOOLS -> Icons.Default.Terminal
    ModuleCategory.SETTINGS -> Icons.Default.Settings
}

fun formatEpoch(epochMs: Long): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date(epochMs))
}
