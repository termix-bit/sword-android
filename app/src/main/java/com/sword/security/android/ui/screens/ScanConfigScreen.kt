package com.sword.security.android.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.sword.security.android.core.i18n.SwordHelpContent
import com.sword.security.android.core.i18n.SwordStrings
import com.sword.security.android.core.model.ParameterType
import com.sword.security.android.core.model.ToolDescriptor
import com.sword.security.android.ui.components.AuthorizationConfirmDialog
import com.sword.security.android.ui.components.ContextualHelpDialog
import com.sword.security.android.ui.components.HelpIconButton
import com.sword.security.android.ui.components.SwordCard
import com.sword.security.android.ui.components.SwordCircuitBackdrop
import com.sword.security.android.ui.components.SwordFilterChip
import com.sword.security.android.ui.components.SwordTopBar
import com.sword.security.android.ui.components.swordOutlinedTextFieldColors
import com.sword.security.android.ui.theme.LocalSwordColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScanConfigScreen(
    tool: ToolDescriptor,
    requireAuthConfirmation: Boolean,
    languageCode: String = "en",
    onBack: () -> Unit,
    onExecute: (Map<String, String>) -> Unit
) {
    val swordColors = LocalSwordColors.current
    val paramValues = remember(tool.id) {
        mutableStateMapOf<String, String>().apply {
            tool.parameters.forEach { p -> put(p.key, p.defaultValue) }
        }
    }
    var validationError by remember { mutableStateOf<String?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

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

    if (showHelpDialog) {
        ContextualHelpDialog(
            topic = SwordHelpContent.forTool(tool, languageCode),
            languageCode = languageCode,
            onDismiss = { showHelpDialog = false }
        )
    }

    if (showConfirmDialog) {
        val summary = paramValues.entries
            .filter { it.value.isNotBlank() }
            .joinToString(" • ") { "${it.key}=${it.value}" }
        AuthorizationConfirmDialog(
            toolName = tool.name,
            targetSummary = summary,
            languageCode = languageCode,
            onConfirm = {
                showConfirmDialog = false
                onExecute(paramValues.toMap())
            },
            onDismiss = { showConfirmDialog = false }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            SwordTopBar(
                title = SwordStrings.label("scan_config_title", languageCode),
                subtitle = tool.name,
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
                    Text(tool.summary, style = MaterialTheme.typography.bodyLarge, color = swordColors.textPrimary)
                }

                if (tool.parameters.isEmpty()) {
                    SwordCard {
                        Text(
                            text = SwordStrings.label("no_input_needed", languageCode),
                            style = MaterialTheme.typography.bodyMedium,
                            color = swordColors.textSecondary
                        )
                    }
                } else {
                    tool.parameters.forEach { param ->
                        SwordCard {
                            Text(
                                text = param.label + if (param.required) " *" else "",
                                style = MaterialTheme.typography.titleMedium,
                                color = swordColors.accent
                            )
                            Spacer(Modifier.height(8.dp))
                            when (param.type) {
                                ParameterType.SELECT -> {
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        param.options.forEach { option ->
                                            SwordFilterChip(
                                                selected = paramValues[param.key] == option,
                                                onClick = { paramValues[param.key] = option },
                                                label = option
                                            )
                                        }
                                    }
                                }
                                ParameterType.FILE_URI -> {
                                    val currentUri = paramValues[param.key].orEmpty()
                                    Text(
                                        text = currentUri.ifBlank { SwordStrings.label("no_file_selected", languageCode) },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (currentUri.isBlank()) swordColors.textSecondary else swordColors.textPrimary
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    OutlinedButton(
                                        onClick = {
                                            activeFileParamKey = param.key
                                            val mime = if (tool.id == "forensics_exif_audit") arrayOf("image/*") else arrayOf("*/*")
                                            filePicker.launch(mime)
                                        },
                                        modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                                        border = BorderStroke(1.dp, swordColors.accent)
                                    ) {
                                        Text(SwordStrings.label("pick_file_saf", languageCode), color = swordColors.accent)
                                    }
                                }
                                else -> {
                                    val forceLtr = param.type in listOf(
                                        ParameterType.URL,
                                        ParameterType.DOMAIN,
                                        ParameterType.IP_ADDRESS,
                                        ParameterType.SUBNET,
                                        ParameterType.PORT_LIST,
                                        ParameterType.INTEGER
                                    )
                                    val fieldDir = if (forceLtr) LayoutDirection.Ltr else LocalLayoutDirection.current
                                    CompositionLocalProvider(LocalLayoutDirection provides fieldDir) {
                                        OutlinedTextField(
                                            value = paramValues[param.key].orEmpty(),
                                            onValueChange = {
                                                paramValues[param.key] = it
                                                validationError = null
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            placeholder = { Text(param.hint) },
                                            singleLine = true,
                                            colors = swordOutlinedTextFieldColors()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (!validationError.isNullOrBlank()) {
                    Text(
                        text = validationError!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = swordColors.error
                    )
                }

                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = ::tryLaunch,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = SwordStrings.label("start_execution", languageCode),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
