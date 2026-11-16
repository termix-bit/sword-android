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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sword.security.android.core.model.CapabilityState
import com.sword.security.android.core.model.ToolDescriptor
import com.sword.security.android.ui.components.CapabilityBadge
import com.sword.security.android.ui.components.SwordCard
import com.sword.security.android.ui.components.SwordCircuitBackdrop
import com.sword.security.android.ui.components.SwordTopBar
import com.sword.security.android.ui.theme.SwordBackground
import com.sword.security.android.ui.theme.SwordDeepNavy
import com.sword.security.android.ui.theme.SwordPrimaryCyan
import com.sword.security.android.ui.theme.SwordTextPrimary
import com.sword.security.android.ui.theme.SwordTextSecondary
import com.sword.security.android.ui.theme.SwordWarning

@Composable
fun ModuleDetailScreen(
    tool: ToolDescriptor,
    capabilityState: CapabilityState,
    onBack: () -> Unit,
    onConfigureAndRun: () -> Unit,
    onOpenPermissions: () -> Unit
) {
    Scaffold(
        containerColor = SwordBackground,
        topBar = {
            SwordTopBar(
                title = tool.name,
                subtitle = tool.category.title,
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
                SwordCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Device Capability", style = MaterialTheme.typography.titleMedium)
                        CapabilityBadge(capabilityState)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(capabilityState.description, style = MaterialTheme.typography.bodyMedium)
                    if (!tool.staticLimitationReason.isNullOrBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Platform Note: ${tool.staticLimitationReason}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SwordWarning
                        )
                    }
                }

                SwordCard {
                    Text("Module Overview", style = MaterialTheme.typography.titleMedium, color = SwordPrimaryCyan)
                    Spacer(Modifier.height(6.dp))
                    Text(tool.description, style = MaterialTheme.typography.bodyLarge)
                }

                SwordCard {
                    Text("Sword Reference Compatibility", style = MaterialTheme.typography.titleMedium, color = SwordPrimaryCyan)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Classification: ${tool.compatibility.name.replace('_', ' ')}",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(tool.compatibilityNotes, style = MaterialTheme.typography.bodyMedium)
                }

                if (tool.parameters.isNotEmpty()) {
                    SwordCard {
                        Text("Required Inputs", style = MaterialTheme.typography.titleMedium, color = SwordPrimaryCyan)
                        Spacer(Modifier.height(6.dp))
                        tool.parameters.forEach { param ->
                            Text(
                                text = "• ${param.label}${if (param.required) " (required)" else " (optional)"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SwordTextPrimary
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                if (capabilityState == CapabilityState.REQUIRES_PERMISSION) {
                    OutlinedButton(
                        onClick = onOpenPermissions,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Open Permissions Manager", color = SwordPrimaryCyan)
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
                        containerColor = SwordPrimaryCyan,
                        contentColor = SwordDeepNavy
                    )
                ) {
                    Text(
                        text = if (canConfigure) "Configure & Execute" else "Unavailable on This Device",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = if (canConfigure) SwordDeepNavy else SwordTextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
