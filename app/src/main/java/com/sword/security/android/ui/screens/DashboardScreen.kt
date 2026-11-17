package com.sword.security.android.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sword.security.android.core.i18n.HelpTopic
import com.sword.security.android.core.i18n.SwordHelpContent
import com.sword.security.android.core.i18n.SwordStrings
import com.sword.security.android.core.model.DeviceCapabilitiesSummary
import com.sword.security.android.core.model.ModuleCategory
import com.sword.security.android.core.model.PermissionStatusItem
import com.sword.security.android.data.local.db.ReportMetadataEntity
import com.sword.security.android.ui.components.ContextualHelpDialog
import com.sword.security.android.ui.components.HelpIconButton
import com.sword.security.android.ui.components.LocalSwordAnimationsEnabled
import com.sword.security.android.ui.components.SectionHeader
import com.sword.security.android.ui.components.SeverityBadge
import com.sword.security.android.ui.components.SwordCard
import com.sword.security.android.ui.components.SwordCircuitBackdrop
import com.sword.security.android.ui.components.SwordEmptyStateCard
import com.sword.security.android.ui.components.SwordLogoBadge
import com.sword.security.android.ui.theme.LocalSwordColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    capabilities: DeviceCapabilitiesSummary?,
    permissions: List<PermissionStatusItem>,
    recentReports: List<ReportMetadataEntity>,
    languageCode: String = "en",
    scrollToTopTrigger: Int = 0,
    onExploreTools: () -> Unit,
    onOpenCategory: (ModuleCategory) -> Unit,
    onOpenToolConfig: (String) -> Unit,
    onOpenCapabilities: () -> Unit,
    onOpenPermissions: () -> Unit,
    onOpenReports: () -> Unit,
    onOpenReportViewer: (ReportMetadataEntity) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit
) {
    val swordColors = LocalSwordColors.current
    val animationsEnabled = LocalSwordAnimationsEnabled.current
    val listState = rememberLazyListState()
    var activeHelpTopic by remember { mutableStateOf<HelpTopic?>(null) }

    LaunchedEffect(scrollToTopTrigger) {
        if (scrollToTopTrigger > 0) {
            if (animationsEnabled) {
                listState.animateScrollToItem(0)
            } else {
                listState.scrollToItem(0)
            }
        }
    }

    activeHelpTopic?.let { topic ->
        ContextualHelpDialog(
            topic = topic,
            languageCode = languageCode,
            onDismiss = { activeHelpTopic = null }
        )
    }

    val grantedCount = permissions.count { it.isGranted }
    val totalPerms = permissions.size.coerceAtLeast(1)
    val isOnline = capabilities?.hasInternetConnection == true
    val toolCategories = remember { ModuleCategory.entries.filter { it.isToolCategory } }

    SwordCircuitBackdrop {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Clean Hero Section — clearly communicates what Sword does and how to get started
            item {
                SwordCard(borderColor = swordColors.accent.copy(alpha = 0.45f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            SwordLogoBadge(size = 52.dp)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = SwordStrings.label("app_name", languageCode),
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    color = swordColors.textPrimary
                                )
                                Text(
                                    text = SwordStrings.label("app_subtitle", languageCode),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = swordColors.accent,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            HelpIconButton(
                                onClick = {
                                    activeHelpTopic = SwordHelpContent.forKey("home_overview", languageCode)
                                },
                                contentDescription = SwordStrings.label("help", languageCode)
                            )
                            IconButton(
                                onClick = onOpenAbout,
                                modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = SwordStrings.label("about_sword", languageCode),
                                    tint = swordColors.textSecondary
                                )
                            }
                            IconButton(
                                onClick = onOpenSettings,
                                modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = SwordStrings.label("settings", languageCode),
                                    tint = swordColors.textSecondary
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = SwordStrings.label("home_hero_desc", languageCode),
                        style = MaterialTheme.typography.bodyLarge,
                        color = swordColors.textSecondary
                    )
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = onExploreTools,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = SwordStrings.label("explore_all_tools", languageCode),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // 2. Quick Checks — one-tap plain-language shortcuts
            item {
                SectionHeader(
                    title = SwordStrings.label("quick_actions", languageCode),
                    onHelpClick = {
                        activeHelpTopic = SwordHelpContent.forKey("quick_checks", languageCode)
                    },
                    helpContentDescription = SwordStrings.label("help", languageCode)
                )
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        QuickCheckTile(
                            label = SwordStrings.label("quick_website_safety", languageCode),
                            icon = Icons.Default.Public,
                            onClick = { onOpenToolConfig("web_header_audit") }
                        )
                    }
                    item {
                        QuickCheckTile(
                            label = SwordStrings.label("quick_tls_check", languageCode),
                            icon = Icons.Default.Language,
                            onClick = { onOpenToolConfig("web_tls_inspector") }
                        )
                    }
                    item {
                        QuickCheckTile(
                            label = SwordStrings.label("quick_dns_lookup", languageCode),
                            icon = Icons.Default.Dns,
                            onClick = { onOpenToolConfig("net_dns_lookup") }
                        )
                    }
                    item {
                        QuickCheckTile(
                            label = SwordStrings.label("quick_hash_gen", languageCode),
                            icon = Icons.Default.Key,
                            onClick = { onOpenToolConfig("crypto_hash_gen") }
                        )
                    }
                    item {
                        QuickCheckTile(
                            label = SwordStrings.label("quick_file_check", languageCode),
                            icon = Icons.Default.Fingerprint,
                            onClick = { onOpenToolConfig("forensics_file_inspect") }
                        )
                    }
                }
            }

            // 3. Compact Device Readiness — non-technical summary with links to Capabilities / Permissions
            item {
                SectionHeader(
                    title = SwordStrings.label("device_readiness", languageCode),
                    onHelpClick = {
                        activeHelpTopic = SwordHelpContent.forKey("device_readiness", languageCode)
                    },
                    helpContentDescription = SwordStrings.label("help", languageCode)
                )
                Spacer(Modifier.height(8.dp))
                SwordCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isOnline) Icons.Default.CheckCircle else Icons.Default.WifiOff,
                                contentDescription = null,
                                tint = if (isOnline) swordColors.success else swordColors.warning,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = SwordStrings.label(
                                        if (isOnline) "network_connected" else "network_offline",
                                        languageCode
                                    ),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = swordColors.textPrimary
                                )
                                Text(
                                    text = "${SwordStrings.label("permissions_summary", languageCode)}: $grantedCount / $totalPerms",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = swordColors.textSecondary
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onOpenPermissions,
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 48.dp),
                            border = BorderStroke(1.dp, swordColors.border)
                        ) {
                            Icon(
                                Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = swordColors.accent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = SwordStrings.label("permissions", languageCode),
                                color = swordColors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        OutlinedButton(
                            onClick = onOpenCapabilities,
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 48.dp),
                            border = BorderStroke(1.dp, swordColors.border)
                        ) {
                            Icon(
                                Icons.Default.Memory,
                                contentDescription = null,
                                tint = swordColors.accent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = SwordStrings.label("capabilities", languageCode),
                                color = swordColors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // 4. Security Categories — clean balanced cards with contextual ? help
            item {
                SectionHeader(title = SwordStrings.label("security_categories", languageCode))
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    toolCategories.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            pair.forEach { cat ->
                                SwordCard(
                                    modifier = Modifier.weight(1f),
                                    onClick = { onOpenCategory(cat) }
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = categoryIcon(cat),
                                            contentDescription = null,
                                            tint = swordColors.accent,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        HelpIconButton(
                                            onClick = {
                                                activeHelpTopic = SwordHelpContent.forCategory(cat, languageCode)
                                            },
                                            contentDescription = "${SwordStrings.label("help", languageCode)}: ${SwordStrings.categoryTitle(cat, languageCode)}"
                                        )
                                    }
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = SwordStrings.categoryTitle(cat, languageCode),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = swordColors.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = SwordStrings.categorySubtitle(cat, languageCode),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = swordColors.textSecondary,
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

            // 5. Recent Reports — tapping opens ReportViewerScreen directly
            item {
                SectionHeader(
                    title = SwordStrings.label("recent_reports", languageCode),
                    actionText = SwordStrings.label("all_reports", languageCode),
                    onAction = onOpenReports
                )
                Spacer(Modifier.height(8.dp))
                if (recentReports.isEmpty()) {
                    SwordEmptyStateCard(
                        icon = Icons.Default.Description,
                        title = SwordStrings.label("recent_reports", languageCode),
                        message = SwordStrings.label("no_reports_home", languageCode)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        recentReports.take(3).forEach { report ->
                            SwordCard(onClick = { onOpenReportViewer(report) }) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = report.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = swordColors.textPrimary,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    SeverityBadge(report.highestSeverity)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "${formatEpoch(report.createdEpochMs)} • ${report.findingsCount} ${SwordStrings.label("findings", languageCode)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = swordColors.textSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickCheckTile(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val swordColors = LocalSwordColors.current
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = swordColors.cardSurface,
        border = BorderStroke(1.dp, swordColors.border),
        modifier = Modifier.defaultMinSize(minWidth = 112.dp, minHeight = 52.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = swordColors.accent, modifier = Modifier.size(18.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = swordColors.textPrimary,
                maxLines = 1
            )
        }
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
