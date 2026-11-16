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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sword.security.android.data.local.db.EvidenceRecordEntity
import com.sword.security.android.data.local.db.ScanRecordEntity
import com.sword.security.android.ui.components.SectionHeader
import com.sword.security.android.ui.components.SeverityBadge
import com.sword.security.android.ui.components.SwordCard
import com.sword.security.android.ui.components.SwordCircuitBackdrop
import com.sword.security.android.ui.components.SwordTopBar
import com.sword.security.android.ui.theme.SwordBackground
import com.sword.security.android.ui.theme.SwordError
import com.sword.security.android.ui.theme.SwordPrimaryCyan
import com.sword.security.android.ui.theme.SwordTextPrimary
import com.sword.security.android.ui.theme.SwordTextSecondary

@Composable
fun ScanHistoryScreen(
    scans: List<ScanRecordEntity>,
    evidenceRecords: List<EvidenceRecordEntity>,
    onDeleteScan: (Long) -> Unit,
    onClearHistory: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    Scaffold(
        containerColor = SwordBackground,
        topBar = {
            SwordTopBar(
                title = "Scan History",
                subtitle = "${scans.size} Execution(s)",
                onBack = onBack,
                actions = {
                    if (scans.isNotEmpty() || evidenceRecords.isNotEmpty()) {
                        IconButton(onClick = onClearHistory) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear History", tint = SwordError)
                        }
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
                item {
                    SectionHeader(title = "Execution Timeline")
                }

                if (scans.isEmpty()) {
                    item {
                        SwordCard {
                            Text(
                                text = "No scans have been executed yet. All task runs, durations, and statuses are persisted locally in Room.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
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
                                    Text(scan.toolName, style = MaterialTheme.typography.titleMedium, color = SwordTextPrimary)
                                    Text(
                                        text = "${scan.targetSummary} • ${scan.status} • ${scan.durationMs} ms",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SwordPrimaryCyan
                                    )
                                }
                                SeverityBadge(scan.highestSeverity)
                                IconButton(onClick = { onDeleteScan(scan.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Scan", tint = SwordTextSecondary)
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(formatEpoch(scan.timestampEpochMs), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                if (evidenceRecords.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(6.dp))
                        SectionHeader(title = "Forensics Evidence Integrity Records (${evidenceRecords.size})")
                    }
                    items(evidenceRecords, key = { it.id }) { ev ->
                        SwordCard {
                            Text(ev.fileName, style = MaterialTheme.typography.titleMedium, color = SwordPrimaryCyan)
                            Text(
                                text = "${ev.detectedSignature} • ${ev.sizeBytes} bytes • ${formatEpoch(ev.recordedEpochMs)}",
                                style = MaterialTheme.typography.labelSmall
                            )
                            Spacer(Modifier.height(4.dp))
                            Text("SHA-256: ${ev.sha256}", style = MaterialTheme.typography.labelSmall, color = SwordTextPrimary)
                        }
                    }
                }
            }
        }
    }
}
