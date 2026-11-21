package com.sword.security.android.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sword.security.android.core.i18n.SwordStrings
import com.sword.security.android.core.model.ExecutionProgress
import com.sword.security.android.core.model.ExecutionStatus
import com.sword.security.android.ui.components.ScanExplanationPanel
import com.sword.security.android.ui.components.SectionHeader
import com.sword.security.android.ui.components.SeverityBadge
import com.sword.security.android.ui.components.SwordCard
import com.sword.security.android.ui.components.SwordCircuitBackdrop
import com.sword.security.android.ui.components.SwordTopBar
import com.sword.security.android.ui.components.TerminalOutputBox
import com.sword.security.android.ui.theme.LocalSwordColors

@Composable
fun LiveExecutionScreen(
    progress: ExecutionProgress,
    languageCode: String = "en",
    onBack: () -> Unit,
    onCancelTask: (String) -> Unit,
    onOpenReports: () -> Unit
) {
    val swordColors = LocalSwordColors.current
    val statusColor = when (progress.status) {
        ExecutionStatus.RUNNING -> swordColors.accent
        ExecutionStatus.COMPLETED -> swordColors.success
        ExecutionStatus.CANCELLED -> swordColors.warning
        ExecutionStatus.FAILED -> swordColors.error
        ExecutionStatus.IDLE -> swordColors.textSecondary
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            SwordTopBar(
                title = SwordStrings.label("live_execution", languageCode),
                subtitle = progress.toolName.ifBlank { "Sword" },
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
                            text = progress.status.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = statusColor
                        )
                        if (progress.taskId.isNotBlank()) {
                            Text(
                                text = "#${progress.taskId}",
                                style = MaterialTheme.typography.labelSmall,
                                color = swordColors.textSecondary
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress.progressFraction.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth(),
                        color = statusColor,
                        trackColor = swordColors.border
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = progress.stageMessage.ifBlank { "..." },
                        style = MaterialTheme.typography.bodyMedium,
                        color = swordColors.textPrimary
                    )

                    if (progress.status == ExecutionStatus.RUNNING && progress.taskId.isNotBlank()) {
                        Spacer(Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = { onCancelTask(progress.taskId) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp),
                            border = BorderStroke(1.dp, swordColors.error.copy(alpha = 0.6f))
                        ) {
                            Text(SwordStrings.label("cancel_task", languageCode), color = swordColors.error)
                        }
                    }
                }

                if (progress.liveLogs.isNotEmpty()) {
                    SectionHeader(
                        title = if (SwordStrings.isPersian(languageCode)) "گزارش زنده مراحل" else "Progress Log"
                    )
                    TerminalOutputBox(text = progress.liveLogs.joinToString("\n"))
                }

                val result = progress.result
                if (result != null) {
                    var showExplanation by rememberSaveable(result.taskId, result.timestampEpochMs) {
                        mutableStateOf(false)
                    }

                    SwordCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = SwordStrings.label("execution_summary", languageCode),
                                style = MaterialTheme.typography.titleMedium,
                                color = swordColors.accent
                            )
                            SeverityBadge(result.highestSeverity.label)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(result.targetSummary, style = MaterialTheme.typography.bodyMedium, color = swordColors.textPrimary)
                        Text("${result.durationMs} ms", style = MaterialTheme.typography.labelSmall, color = swordColors.textSecondary)
                        Spacer(Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = { showExplanation = !showExplanation },
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp),
                            border = BorderStroke(1.dp, swordColors.accent.copy(alpha = 0.6f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = null,
                                tint = swordColors.accent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = SwordStrings.label(
                                    if (showExplanation) "hide_explanation" else "explain_results",
                                    languageCode
                                ),
                                color = swordColors.accent,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }

                    if (showExplanation) {
                        ScanExplanationPanel(
                            toolName = result.toolName,
                            targetSummary = result.targetSummary,
                            findings = result.findings,
                            defaultLanguageCode = languageCode,
                            rawOutputAvailable = result.rawOutput.isNotBlank()
                        )
                    }

                    if (result.findings.isNotEmpty()) {
                        SectionHeader(title = "${SwordStrings.label("findings", languageCode)} (${result.findings.size})")
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
                                        color = swordColors.textPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    SeverityBadge(finding.severity)
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(finding.detail, style = MaterialTheme.typography.bodyMedium, color = swordColors.textPrimary)
                                if (finding.recommendation.isNotBlank()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "${SwordStrings.label("recommendation", languageCode)} ${finding.recommendation}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = swordColors.accent
                                    )
                                }
                            }
                        }
                    }

                    SectionHeader(title = SwordStrings.label("detailed_output", languageCode))
                    TerminalOutputBox(text = result.rawOutput)

                    if (result.reportId != null) {
                        Button(
                            onClick = onOpenReports,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(
                                text = SwordStrings.label("open_reports_center", languageCode),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}
