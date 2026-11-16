package com.sword.security.android.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sword.security.android.data.local.db.ReportMetadataEntity
import com.sword.security.android.data.reports.SavedJsonReport
import com.sword.security.android.ui.components.SeverityBadge
import com.sword.security.android.ui.components.SwordCard
import com.sword.security.android.ui.components.SwordCircuitBackdrop
import com.sword.security.android.ui.components.SwordTopBar
import com.sword.security.android.ui.components.TerminalOutputBox
import com.sword.security.android.ui.theme.SwordBackground
import com.sword.security.android.ui.theme.SwordCardSurface
import com.sword.security.android.ui.theme.SwordError
import com.sword.security.android.ui.theme.SwordPrimaryCyan
import com.sword.security.android.ui.theme.SwordTextPrimary
import com.sword.security.android.ui.theme.SwordTextSecondary

@Composable
fun ReportsCenterScreen(
    reports: List<ReportMetadataEntity>,
    selectedReportDetail: SavedJsonReport?,
    onOpenReport: (ReportMetadataEntity) -> Unit,
    onDismissReportDetail: () -> Unit,
    onDeleteReport: (ReportMetadataEntity) -> Unit,
    onDeleteAllReports: () -> Unit,
    onExportReport: (String, android.net.Uri) -> Unit,
    getShareUri: (String) -> android.net.Uri?,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var activeReportForDialog by remember { mutableStateOf<ReportMetadataEntity?>(null) }
    var pendingExportFileName by remember { mutableStateOf<String?>(null) }

    val jsonExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        val fileName = pendingExportFileName
        if (uri != null && fileName != null) {
            onExportReport(fileName, uri)
        }
    }

    val htmlExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/html")
    ) { uri ->
        val fileName = pendingExportFileName
        if (uri != null && fileName != null) {
            onExportReport(fileName, uri)
        }
    }

    val viewingMeta = activeReportForDialog
    if (viewingMeta != null && selectedReportDetail != null) {
        AlertDialog(
            onDismissRequest = {
                activeReportForDialog = null
                onDismissReportDetail()
            },
            containerColor = SwordCardSurface,
            title = {
                Column {
                    Text(selectedReportDetail.toolName, style = MaterialTheme.typography.titleLarge, color = SwordTextPrimary)
                    Text(selectedReportDetail.target, style = MaterialTheme.typography.labelSmall, color = SwordPrimaryCyan)
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Timestamp: ${selectedReportDetail.timestampIso} (${selectedReportDetail.durationMs} ms)",
                        style = MaterialTheme.typography.labelSmall
                    )
                    if (selectedReportDetail.findings.isNotEmpty()) {
                        selectedReportDetail.findings.forEach { f ->
                            SwordCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(f.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                    SeverityBadge(f.severity)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(f.detail, style = MaterialTheme.typography.bodyMedium)
                                Text("Recommendation: ${f.recommendation}", style = MaterialTheme.typography.bodyMedium, color = SwordPrimaryCyan)
                            }
                        }
                    }
                    TerminalOutputBox(text = selectedReportDetail.output)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                pendingExportFileName = viewingMeta.jsonFileName
                                jsonExportLauncher.launch(viewingMeta.jsonFileName)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = SwordPrimaryCyan)
                            Spacer(Modifier.width(4.dp))
                            Text("JSON", color = SwordPrimaryCyan)
                        }
                        OutlinedButton(
                            onClick = {
                                pendingExportFileName = viewingMeta.htmlFileName
                                htmlExportLauncher.launch(viewingMeta.htmlFileName)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = SwordPrimaryCyan)
                            Spacer(Modifier.width(4.dp))
                            Text("HTML", color = SwordPrimaryCyan)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val uri = getShareUri(viewingMeta.htmlFileName)
                        if (uri != null) {
                            val share = Intent(Intent.ACTION_SEND).apply {
                                type = "text/html"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(share, "Share Sword HTML Report"))
                        }
                    }
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = SwordPrimaryCyan)
                    Spacer(Modifier.width(4.dp))
                    Text("Share HTML", color = SwordPrimaryCyan)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        activeReportForDialog = null
                        onDismissReportDetail()
                    }
                ) {
                    Text("Close", color = SwordTextSecondary)
                }
            }
        )
    }

    Scaffold(
        containerColor = SwordBackground,
        topBar = {
            SwordTopBar(
                title = "Reports Center",
                subtitle = "${reports.size} JSON + HTML Report(s)",
                onBack = onBack,
                actions = {
                    if (reports.isNotEmpty()) {
                        IconButton(onClick = onDeleteAllReports) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Delete All Reports", tint = SwordError)
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
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (reports.isEmpty()) {
                    item {
                        SwordCard {
                            Text(
                                text = "No security reports saved yet. Execute any Sword module to automatically generate structured JSON and styled HTML reports.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    items(reports, key = { it.id }) { report ->
                        SwordCard(
                            onClick = {
                                activeReportForDialog = report
                                onOpenReport(report)
                            }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(report.title, style = MaterialTheme.typography.titleMedium, color = SwordTextPrimary)
                                    Text(
                                        text = "${formatEpoch(report.createdEpochMs)} • ${report.findingsCount} finding(s)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SwordPrimaryCyan
                                    )
                                }
                                SeverityBadge(report.highestSeverity)
                                IconButton(onClick = { onDeleteReport(report) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Report", tint = SwordTextSecondary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
