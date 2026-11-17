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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sword.security.android.core.i18n.SwordHelpContent
import com.sword.security.android.core.i18n.SwordStrings
import com.sword.security.android.core.model.CapabilityState
import com.sword.security.android.core.model.ToolDescriptor
import com.sword.security.android.ui.components.CapabilityBadge
import com.sword.security.android.ui.components.ContextualHelpDialog
import com.sword.security.android.ui.components.ExpandableHelpPanel
import com.sword.security.android.ui.components.HelpIconButton
import com.sword.security.android.ui.components.SwordCard
import com.sword.security.android.ui.components.SwordCircuitBackdrop
import com.sword.security.android.ui.components.SwordTopBar
import com.sword.security.android.ui.theme.LocalSwordColors

@Composable
fun ModuleDetailScreen(
    tool: ToolDescriptor,
    capabilityState: CapabilityState,
    languageCode: String = "en",
    onBack: () -> Unit,
    onConfigureAndRun: () -> Unit,
    onOpenPermissions: () -> Unit
) {
    val swordColors = LocalSwordColors.current
    var showHelpDialog by remember { mutableStateOf(false) }

    if (showHelpDialog) {
        ContextualHelpDialog(
            topic = SwordHelpContent.forTool(tool, languageCode),
            languageCode = languageCode,
            onDismiss = { showHelpDialog = false }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            SwordTopBar(
                title = tool.name,
                subtitle = SwordStrings.categoryTitle(tool.category, languageCode),
                onBack = onBack,
                actions = {
                    HelpIconButton(
                        onClick = { showHelpDialog = true },
                        contentDescription = SwordStrings.label("help", languageCode)
                    )
                }
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
                SwordCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = SwordStrings.label("device_capability", languageCode),
                            style = MaterialTheme.typography.titleMedium,
                            color = swordColors.textPrimary
                        )
                        CapabilityBadge(capabilityState, languageCode = languageCode)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(capabilityState.description, style = MaterialTheme.typography.bodyMedium, color = swordColors.textSecondary)
                    if (!tool.staticLimitationReason.isNullOrBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = tool.staticLimitationReason,
                            style = MaterialTheme.typography.bodyMedium,
                            color = swordColors.warning
                        )
                    }
                }

                SwordCard {
                    Text(
                        text = SwordStrings.label("module_overview", languageCode),
                        style = MaterialTheme.typography.titleMedium,
                        color = swordColors.accent
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(tool.description, style = MaterialTheme.typography.bodyLarge, color = swordColors.textPrimary)
                }

                if (tool.parameters.isNotEmpty()) {
                    SwordCard {
                        Text(
                            text = SwordStrings.label("required_inputs", languageCode),
                            style = MaterialTheme.typography.titleMedium,
                            color = swordColors.accent
                        )
                        Spacer(Modifier.height(6.dp))
                        tool.parameters.forEach { param ->
                            Text(
                                text = "• ${param.label}${if (param.required) " *" else ""}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = swordColors.textPrimary
                            )
                        }
                    }
                }

                ExpandableHelpPanel(
                    title = SwordStrings.label("technical_notes", languageCode),
                    body = "${tool.compatibility.name.replace('_', ' ')}: ${tool.compatibilityNotes}",
                    initiallyExpanded = false
                )

                Spacer(Modifier.height(4.dp))
                if (capabilityState == CapabilityState.REQUIRES_PERMISSION) {
                    OutlinedButton(
                        onClick = onOpenPermissions,
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp),
                        border = BorderStroke(1.dp, swordColors.accent)
                    ) {
                        Text(SwordStrings.label("open_permissions", languageCode), color = swordColors.accent)
                    }
                }

                val canConfigure = tool.isExecutable &&
                    capabilityState != CapabilityState.UNSUPPORTED &&
                    capabilityState != CapabilityState.NOT_IMPLEMENTED

                Button(
                    onClick = onConfigureAndRun,
                    enabled = canConfigure,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = swordColors.elevatedSurface,
                        disabledContentColor = swordColors.textSecondary
                    )
                ) {
                    Text(
                        text = SwordStrings.label(
                            if (canConfigure) "configure_and_run" else "unavailable_device",
                            languageCode
                        ),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
