package com.sword.security.android.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sword.security.android.core.model.ExecutionProgress
import com.sword.security.android.core.model.ExecutionStatus
import com.sword.security.android.ui.components.SectionHeader
import com.sword.security.android.ui.components.SeverityBadge
import com.sword.security.android.ui.components.SwordCard
import com.sword.security.android.ui.components.SwordCircuitBackdrop
import com.sword.security.android.ui.components.SwordTopBar
import com.sword.security.android.ui.components.TerminalOutputBox
import com.sword.security.android.ui.theme.SwordBackground
import com.sword.security.android.ui.theme.SwordBorder
import com.sword.security.android.ui.theme.SwordDeepNavy
import com.sword.security.android.ui.theme.SwordError
import com.sword.security.android.ui.theme.SwordPrimaryCyan
import com.sword.security.android.ui.theme.SwordSuccess
import com.sword.security.android.ui.theme.SwordTextPrimary
import com.sword.security.android.ui.theme.SwordTextSecondary
import com.sword.security.android.ui.theme.SwordWarning

@Composable
fun LiveExecutionScreen(
    progress: ExecutionProgress,
    onBack: () -> Unit,
    onCancelTask: (String) -> Unit,
    onOpenReports: () -> Unit
) {
    val statusColor = when (progress.status) {
        ExecutionStatus.RUNNING -> SwordPrimaryCyan
        ExecutionStatus.COMPLETED -> SwordSuccess
        ExecutionStatus.CANCELLED -> SwordWarning
        ExecutionStatus.FAILED -> SwordError
        ExecutionStatus.IDLE -> SwordTextSecondary
    }

    Scaffold(
        containerColor = SwordBackground,
        topBar = {
            SwordTopBar(
                title = "Live Execution",
                subtitle = progress.toolName.ifBlank { "Execution Engine" },
                onBack = onBack
            )
        }
    ) { innerPadding ->
        SwordCircuitBackdrop(modifier = Modifier.padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SwordCard(borderColor = statusColor.copy(alpha = 0.5f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Status: ${progress.status.name}",
                            style = MaterialTheme.typography.titleMedium,
                            color = statusColor
                        )
                        if (progress.taskId.isNotBlank()) {
                            Text(
                                text = "Task #${progress.taskId}",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress.progressFraction.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth(),
                        color = statusColor,
                        trackColor = SwordBorder
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = progress.stageMessage.ifBlank { "Ready" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = SwordTextPrimary
                    )

                    if (progress.status == ExecutionStatus.RUNNING && progress.taskId.isNotBlank()) {
                        Spacer(Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = { onCancelTask(progress.taskId) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cancel Running Task", color = SwordError)
                        }
                    }
                }

                if (progress.liveLogs.isNotEmpty()) {
                    SectionHeader(title = "Execution Log Stream")
                    TerminalOutputBox(text = progress.liveLogs.joinToString("\n"))
                }

                val result = progress.result
                if (result != null) {
                    SwordCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Execution Summary", style = MaterialTheme.typography.titleMedium, color = SwordPrimaryCyan)
                            SeverityBadge(result.highestSeverity.label)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text("Target: ${result.targetSummary}", style = MaterialTheme.typography.bodyMedium, color = SwordTextPrimary)
                        Text("Duration: ${result.durationMs} ms", style = MaterialTheme.typography.labelSmall)
                    }

                    if (result.findings.isNotEmpty()) {
                        SectionHeader(title = "Findings (${result.findings.size})")
                        result.findings.forEach { finding ->
                            SwordCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = finding.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    SeverityBadge(finding.severity)
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(finding.detail, style = MaterialTheme.typography.bodyMedium, color = SwordTextPrimary)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Recommendation: ${finding.recommendation}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SwordPrimaryCyan
                                )
                            }
                        }
                    }

                    SectionHeader(title = "Structured Output")
                    TerminalOutputBox(text = result.rawOutput)

                    if (result.reportId != null) {
                        Button(
                            onClick = onOpenReports,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SwordPrimaryCyan,
                                contentColor = SwordDeepNavy
                            )
                        ) {
                            Text(
                                text = "View Saved Report in Reports Center",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = SwordDeepNavy,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
