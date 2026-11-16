package com.sword.security.android.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sword.security.android.core.model.ParameterType
import com.sword.security.android.core.model.ToolDescriptor
import com.sword.security.android.ui.components.AuthorizationConfirmDialog
import com.sword.security.android.ui.components.SwordCard
import com.sword.security.android.ui.components.SwordCircuitBackdrop
import com.sword.security.android.ui.components.SwordTopBar
import com.sword.security.android.ui.theme.SwordBackground
import com.sword.security.android.ui.theme.SwordBorder
import com.sword.security.android.ui.theme.SwordCardSurface
import com.sword.security.android.ui.theme.SwordDeepNavy
import com.sword.security.android.ui.theme.SwordError
import com.sword.security.android.ui.theme.SwordPrimaryCyan
import com.sword.security.android.ui.theme.SwordTextPrimary
import com.sword.security.android.ui.theme.SwordTextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScanConfigScreen(
    tool: ToolDescriptor,
    requireAuthConfirmation: Boolean,
    onBack: () -> Unit,
    onExecute: (Map<String, String>) -> Unit
) {
    val paramValues = remember(tool.id) {
        mutableStateMapOf<String, String>().apply {
            tool.parameters.forEach { p -> put(p.key, p.defaultValue) }
        }
    }
    var validationError by remember { mutableStateOf<String?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    var activeFileParamKey by remember { mutableStateOf<String?>(null) }
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        val key = activeFileParamKey
        if (uri != null && key != null) {
            paramValues[key] = uri.toString()
            validationError = null
        }
    }

    fun tryLaunch() {
        val missing = tool.parameters.firstOrNull { p ->
            p.required && paramValues[p.key].orEmpty().isBlank()
        }
        if (missing != null) {
            validationError = "${missing.label} is required."
            return
        }
        validationError = null
        if (tool.requiresAuthorizationConfirmation && requireAuthConfirmation) {
            showConfirmDialog = true
        } else {
            onExecute(paramValues.toMap())
        }
    }

    if (showConfirmDialog) {
        val summary = paramValues.entries
            .filter { it.value.isNotBlank() }
            .joinToString(" • ") { "${it.key}=${it.value}" }
        AuthorizationConfirmDialog(
            toolName = tool.name,
            targetSummary = summary,
            onConfirm = {
                showConfirmDialog = false
                onExecute(paramValues.toMap())
            },
            onDismiss = { showConfirmDialog = false }
        )
    }

    Scaffold(
        containerColor = SwordBackground,
        topBar = {
            SwordTopBar(
                title = "Scan Configuration",
                subtitle = tool.name,
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
                    Text(tool.summary, style = MaterialTheme.typography.bodyLarge)
                }

                if (tool.parameters.isEmpty()) {
                    SwordCard {
                        Text(
                            text = "This diagnostic requires no manual target input and runs directly against local Android system state.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    tool.parameters.forEach { param ->
                        SwordCard {
                            Text(
                                text = param.label + if (param.required) " *" else "",
                                style = MaterialTheme.typography.titleMedium,
                                color = SwordPrimaryCyan
                            )
                            Spacer(Modifier.height(8.dp))
                            when (param.type) {
                                ParameterType.SELECT -> {
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        param.options.forEach { option ->
                                            val selected = paramValues[param.key] == option
                                            FilterChip(
                                                selected = selected,
                                                onClick = { paramValues[param.key] = option },
                                                label = { Text(option) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = SwordPrimaryCyan,
                                                    selectedLabelColor = SwordDeepNavy,
                                                    labelColor = SwordTextSecondary
                                                )
                                            )
                                        }
                                    }
                                }
                                ParameterType.FILE_URI -> {
                                    val currentUri = paramValues[param.key].orEmpty()
                                    Text(
                                        text = if (currentUri.isBlank()) "No file selected" else currentUri,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (currentUri.isBlank()) SwordTextSecondary else SwordTextPrimary
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    OutlinedButton(
                                        onClick = {
                                            activeFileParamKey = param.key
                                            val mime = if (tool.id == "forensics_exif_audit") arrayOf("image/*") else arrayOf("*/*")
                                            filePicker.launch(mime)
                                        }
                                    ) {
                                        Text("Pick File via Android SAF", color = SwordPrimaryCyan)
                                    }
                                }
                                else -> {
                                    OutlinedTextField(
                                        value = paramValues[param.key].orEmpty(),
                                        onValueChange = {
                                            paramValues[param.key] = it
                                            validationError = null
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = { Text(param.hint) },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = SwordPrimaryCyan,
                                            unfocusedBorderColor = SwordBorder,
                                            focusedTextColor = SwordTextPrimary,
                                            unfocusedTextColor = SwordTextPrimary,
                                            focusedContainerColor = SwordCardSurface,
                                            unfocusedContainerColor = SwordCardSurface
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                if (!validationError.isNullOrBlank()) {
                    Text(
                        text = validationError!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SwordError
                    )
                }

                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = ::tryLaunch,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SwordPrimaryCyan,
                        contentColor = SwordDeepNavy
                    )
                ) {
                    Text(
                        text = "Start Execution",
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
