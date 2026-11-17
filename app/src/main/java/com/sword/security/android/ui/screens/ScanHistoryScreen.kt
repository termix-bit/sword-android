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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sword.security.android.core.i18n.SwordStrings
import com.sword.security.android.data.local.db.EvidenceRecordEntity
import com.sword.security.android.data.local.db.ScanRecordEntity
import com.sword.security.android.ui.components.LocalSwordAnimationsEnabled
import com.sword.security.android.ui.components.SectionHeader
import com.sword.security.android.ui.components.SeverityBadge
import com.sword.security.android.ui.components.SwordCard
import com.sword.security.android.ui.components.SwordCircuitBackdrop
import com.sword.security.android.ui.components.SwordEmptyStateCard
import com.sword.security.android.ui.components.SwordTopBar
import com.sword.security.android.ui.theme.LocalSwordColors

@Composable
fun ScanHistoryScreen(
    scans: List<ScanRecordEntity>,
    evidenceRecords: List<EvidenceRecordEntity>,
    languageCode: String = "en",
    scrollToTopTrigger: Int = 0,
    onDeleteScan: (Long) -> Unit,
    onClearHistory: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val swordColors = LocalSwordColors.current
    val animationsEnabled = LocalSwordAnimationsEnabled.current
    val listState = rememberLazyListState()
    var showClearConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(scrollToTopTrigger) {
        if (scrollToTopTrigger > 0) {
            if (animationsEnabled) {
                listState.animateScrollToItem(0)
            } else {
                listState.scrollToItem(0)
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            containerColor = swordColors.cardSurface,
            title = { Text(SwordStrings.label("clear_history", languageCode), color = swordColors.textPrimary) },
            text = { Text(SwordStrings.label("clear_history_confirm", languageCode), color = swordColors.textSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirm = false
                        onClearHistory()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = swordColors.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(SwordStrings.label("delete", languageCode))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showClearConfirm = false },
                    border = BorderStroke(1.dp, swordColors.border)
                ) {
                    Text(SwordStrings.label("cancel", languageCode), color = swordColors.textSecondary)
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            SwordTopBar(
                title = SwordStrings.label("scan_history", languageCode),
                subtitle = "${scans.size}",
                onBack = onBack,
                actions = {
                    if (scans.isNotEmpty() || evidenceRecords.isNotEmpty()) {
                        IconButton(
                            onClick = { showClearConfirm = true },
                            modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                        ) {
                            Icon(
                                Icons.Default.DeleteSweep,
                                contentDescription = SwordStrings.label("clear_history", languageCode),
                                tint = swordColors.error
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        SwordCircuitBackdrop(modifier = Modifier.padding(innerPadding)) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    SectionHeader(title = SwordStrings.label("execution_timeline", languageCode))
                }

                if (scans.isEmpty()) {
                    item {
                        SwordEmptyStateCard(
                            icon = Icons.Default.History,
                            title = SwordStrings.label("scan_history", languageCode),
                            message = SwordStrings.label("no_history", languageCode)
                        )
                    }
                } else {
                    items(scans, key = { it.id }) { scan ->
                        SwordCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = scan.toolName,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = swordColors.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${scan.targetSummary} • ${scan.status} • ${scan.durationMs} ms",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = swordColors.accent,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                SeverityBadge(scan.highestSeverity)
                                IconButton(
                                    onClick = { onDeleteScan(scan.id) },
                                    modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = SwordStrings.label("delete", languageCode),
                                        tint = swordColors.textSecondary
                                    )
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = formatEpoch(scan.timestampEpochMs),
                                style = MaterialTheme.typography.labelSmall,
                                color = swordColors.textSecondary
                            )
                        }
                    }
                }

                if (evidenceRecords.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(6.dp))
                        SectionHeader(title = "${SwordStrings.label("evidence_records", languageCode)} (${evidenceRecords.size})")
                    }
                    items(evidenceRecords, key = { it.id }) { ev ->
                        SwordCard {
                            Text(ev.fileName, style = MaterialTheme.typography.titleMedium, color = swordColors.accent)
                            Text(
                                text = "${ev.detectedSignature} • ${ev.sizeBytes} bytes • ${formatEpoch(ev.recordedEpochMs)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = swordColors.textSecondary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text("SHA-256: ${ev.sha256}", style = MaterialTheme.typography.labelSmall, color = swordColors.textPrimary)
                        }
                    }
                }
            }
        }
    }
}
