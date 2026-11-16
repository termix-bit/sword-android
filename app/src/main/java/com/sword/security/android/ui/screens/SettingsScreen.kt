package com.sword.security.android.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sword.security.android.data.local.prefs.UserPreferences
import com.sword.security.android.ui.components.SectionHeader
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
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    preferences: UserPreferences,
    onToggleAnimations: (Boolean) -> Unit,
    onToggleAuthConfirm: (Boolean) -> Unit,
    onTimeoutChange: (Int) -> Unit,
    onConcurrencyChange: (Int) -> Unit,
    onDohProviderChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onSaveProxy: (String) -> Unit,
    onClearAllData: () -> Unit,
    onResetDefaults: () -> Unit,
    onOpenCapabilities: () -> Unit,
    onOpenPermissions: () -> Unit,
    onOpenAbout: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    var proxyInput by remember(preferences.proxyUrl) { mutableStateOf(preferences.proxyUrl) }

    Scaffold(
        containerColor = SwordBackground,
        topBar = {
            SwordTopBar(
                title = "Settings",
                subtitle = "DataStore Preferences & Security Controls",
                onBack = onBack
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
                    SectionHeader(title = "Execution & Security Controls")
                }

                item {
                    SwordCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Authorization Confirmation", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "Prompt before active port probes, LAN discovery, or HTTP audits.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Switch(
                                checked = preferences.requireAuthorizationConfirmation,
                                onCheckedChange = onToggleAuthConfirm,
                                colors = SwitchDefaults.colors(checkedThumbColor = SwordDeepNavy, checkedTrackColor = SwordPrimaryCyan)
                            )
                        }
                    }
                }

                item {
                    SwordCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Interface Animations", style = MaterialTheme.typography.titleMedium)
                                Text("Smooth startup and transition animations.", style = MaterialTheme.typography.bodyMedium)
                            }
                            Switch(
                                checked = preferences.animationsEnabled,
                                onCheckedChange = onToggleAnimations,
                                colors = SwitchDefaults.colors(checkedThumbColor = SwordDeepNavy, checkedTrackColor = SwordPrimaryCyan)
                            )
                        }
                    }
                }

                item {
                    SwordCard {
                        Text(
                            text = "Network Timeout: ${preferences.networkTimeoutSeconds} seconds",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Slider(
                            value = preferences.networkTimeoutSeconds.toFloat(),
                            onValueChange = { onTimeoutChange(it.roundToInt()) },
                            valueRange = 3f..30f,
                            colors = SliderDefaults.colors(thumbColor = SwordPrimaryCyan, activeTrackColor = SwordPrimaryCyan)
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Max Parallel Probe Concurrency: ${preferences.maxConcurrency}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Slider(
                            value = preferences.maxConcurrency.toFloat(),
                            onValueChange = { onConcurrencyChange(it.roundToInt()) },
                            valueRange = 1f..8f,
                            steps = 6,
                            colors = SliderDefaults.colors(thumbColor = SwordPrimaryCyan, activeTrackColor = SwordPrimaryCyan)
                        )
                    }
                }

                item {
                    SwordCard {
                        Text("DNS-over-HTTPS (DoH) Resolver", style = MaterialTheme.typography.titleMedium, color = SwordPrimaryCyan)
                        Spacer(Modifier.height(8.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Cloudflare", "Google", "Quad9").forEach { provider ->
                                FilterChip(
                                    selected = preferences.dohProvider == provider,
                                    onClick = { onDohProviderChange(provider) },
                                    label = { Text(provider) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SwordPrimaryCyan,
                                        selectedLabelColor = SwordDeepNavy,
                                        labelColor = SwordTextSecondary
                                    )
                                )
                            }
                        }
                    }
                }

                item {
                    SwordCard {
                        Text("Language Preference", style = MaterialTheme.typography.titleMedium, color = SwordPrimaryCyan)
                        Spacer(Modifier.height(8.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("en" to "English", "fa" to "Persian (فارسی)").forEach { (code, label) ->
                                FilterChip(
                                    selected = preferences.languageCode == code,
                                    onClick = { onLanguageChange(code) },
                                    label = { Text(label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SwordPrimaryCyan,
                                        selectedLabelColor = SwordDeepNavy,
                                        labelColor = SwordTextSecondary
                                    )
                                )
                            }
                        }
                    }
                }

                item {
                    SwordCard {
                        Text("Application HTTP/SOCKS Proxy", style = MaterialTheme.typography.titleMedium, color = SwordPrimaryCyan)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Optional proxy for OkHttp requests (e.g. http://127.0.0.1:8080 or socks5://127.0.0.1:1080). Leave blank to disable.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = proxyInput,
                            onValueChange = { proxyInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("http://127.0.0.1:8080") },
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
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { onSaveProxy(proxyInput) },
                            colors = ButtonDefaults.buttonColors(containerColor = SwordPrimaryCyan, contentColor = SwordDeepNavy)
                        ) {
                            Text("Save Proxy")
                        }
                    }
                }

                item {
                    SectionHeader(title = "System & Information")
                }

                item {
                    SwordCard(onClick = onOpenCapabilities) {
                        Text("Device Capabilities Audit", style = MaterialTheme.typography.titleMedium, color = SwordTextPrimary)
                        Text("Inspect Android API support, connectivity, and embedded Python status.", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                item {
                    SwordCard(onClick = onOpenPermissions) {
                        Text("Permissions Manager", style = MaterialTheme.typography.titleMedium, color = SwordTextPrimary)
                        Text("Review and manage runtime Location / Wi-Fi permissions.", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                item {
                    SwordCard(onClick = onOpenAbout) {
                        Text("About Sword Android", style = MaterialTheme.typography.titleMedium, color = SwordTextPrimary)
                        Text("Product family, licensing, third-party notices, and version info.", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                item {
                    SectionHeader(title = "Data Cleanup")
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = onClearAllData, modifier = Modifier.weight(1f)) {
                            Text("Clear Reports & History", color = SwordError)
                        }
                        OutlinedButton(onClick = onResetDefaults, modifier = Modifier.weight(1f)) {
                            Text("Reset Settings", color = SwordPrimaryCyan)
                        }
                    }
                }
            }
        }
    }
}
