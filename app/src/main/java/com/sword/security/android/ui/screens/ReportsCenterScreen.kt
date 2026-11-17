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
import androidx.compose.material.icons.filled.Description
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
import com.sword.security.android.core.i18n.SwordHelpContent
import com.sword.security.android.core.i18n.SwordStrings
import com.sword.security.android.data.local.db.ReportMetadataEntity
import com.sword.security.android.ui.components.ContextualHelpDialog
import com.sword.security.android.ui.components.HelpIconButton
import com.sword.security.android.ui.components.LocalSwordAnimationsEnabled
import com.sword.security.android.ui.components.SeverityBadge
import com.sword.security.android.ui.components.SwordCard
import com.sword.security.android.ui.components.SwordCircuitBackdrop
import com.sword.security.android.ui.components.SwordEmptyStateCard
import com.sword.security.android.ui.components.SwordTopBar
import com.sword.security.android.ui.theme.LocalSwordColors

@Composable
fun ReportsCenterScreen(
    reports: List<ReportMetadataEntity>,
    languageCode: String = "en",
    scrollToTopTrigger: Int = 0,
    onOpenReport: (ReportMetadataEntity) -> Unit,
    onDeleteReport: (ReportMetadataEntity) -> Unit,
    onDeleteAllReports: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val swordColors = LocalSwordColors.current
    val animationsEnabled = LocalSwordAnimationsEnabled.current
    val listState = rememberLazyListState()
    var showHelpDialog by remember { mutableStateOf(false) }
    var reportToDelete by remember { mutableStateOf<ReportMetadataEntity?>(null) }
    var showDeleteAllConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(scrollToTopTrigger) {
        if (scrollToTopTrigger > 0) {
            if (animationsEnabled) {
                listState.animateScrollToItem(0)
            } else {
                listState.scrollToItem(0)
            }
        }
    }

    if (showHelpDialog) {
        ContextualHelpDialog(
            topic = SwordHelpContent.forKey("reports_viewer", languageCode),
            languageCode = languageCode,
            onDismiss = { showHelpDialog = false }
        )
    }

    reportToDelete?.let { pending ->
        AlertDialog(
            onDismissRequest = { reportToDelete = null },
            containerColor = swordColors.cardSurface,
            title = { Text(SwordStrings.label("delete", languageCode), color = swordColors.textPrimary) },
            text = {
                Text(
                    "${pending.title}\n\n${SwordStrings.label("delete_report_confirm", languageCode)}",
                    color = swordColors.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteReport(pending)
                        reportToDelete = null
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
                    onClick = { reportToDelete = null },
                    border = BorderStroke(1.dp, swordColors.border)
                ) {
                    Text(SwordStrings.label("cancel", languageCode), color = swordColors.textSecondary)
                }
            }
        )
    }

    if (showDeleteAllConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteAllConfirm = false },
            containerColor = swordColors.cardSurface,
            title = { Text(SwordStrings.label("delete_all_reports", languageCode), color = swordColors.textPrimary) },
            text = { Text(SwordStrings.label("delete_all_reports_confirm", languageCode), color = swordColors.textSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAllConfirm = false
                        onDeleteAllReports()
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
                    onClick = { showDeleteAllConfirm = false },
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
                title = SwordStrings.label("reports_center", languageCode),
                subtitle = "${reports.size} JSON + HTML",
                onBack = onBack,
                actions = {
                    HelpIconButton(
                        onClick = { showHelpDialog = true },
                        contentDescription = SwordStrings.label("help", languageCode)
                    )
                    if (reports.isNotEmpty()) {
                        IconButton(
                            onClick = { showDeleteAllConfirm = true },
                            modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                        ) {
                            Icon(
                                Icons.Default.DeleteSweep,
                                contentDescription = SwordStrings.label("delete_all_reports", languageCode),
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
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (reports.isEmpty()) {
                    item {
                        SwordEmptyStateCard(
                            icon = Icons.Default.Description,
                            title = SwordStrings.label("reports_center", languageCode),
                            message = SwordStrings.label("no_reports", languageCode)
                        )
                    }
                } else {
                    items(reports, key = { it.id }) { report ->
                        SwordCard(onClick = { onOpenReport(report) }) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = report.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = swordColors.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = "${formatEpoch(report.createdEpochMs)} • ${report.findingsCount} ${SwordStrings.label("findings", languageCode)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = swordColors.accent
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                SeverityBadge(report.highestSeverity)
                                IconButton(
                                    onClick = { reportToDelete = report },
                                    modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = SwordStrings.label("delete", languageCode),
                                        tint = swordColors.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
