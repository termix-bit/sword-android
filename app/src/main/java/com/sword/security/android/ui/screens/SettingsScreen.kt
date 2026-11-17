package com.sword.security.android.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.sword.security.android.core.i18n.HelpTopic
import com.sword.security.android.core.i18n.SwordHelpContent
import com.sword.security.android.core.i18n.SwordStrings
import com.sword.security.android.data.local.prefs.UserPreferences
import com.sword.security.android.ui.components.ContextualHelpDialog
import com.sword.security.android.ui.components.HelpIconButton
import com.sword.security.android.ui.components.LocalSwordAnimationsEnabled
import com.sword.security.android.ui.components.SectionHeader
import com.sword.security.android.ui.components.SwordCard
import com.sword.security.android.ui.components.SwordCircuitBackdrop
import com.sword.security.android.ui.components.SwordFilterChip
import com.sword.security.android.ui.components.SwordTopBar
import com.sword.security.android.ui.components.swordOutlinedTextFieldColors
import com.sword.security.android.ui.components.swordSliderColors
import com.sword.security.android.ui.components.swordSwitchColors
import com.sword.security.android.ui.theme.LocalSwordColors
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    preferences: UserPreferences,
    scrollToTopTrigger: Int = 0,
    onToggleAnimations: (Boolean) -> Unit,
    onToggleAuthConfirm: (Boolean) -> Unit,
    onTimeoutChange: (Int) -> Unit,
    onConcurrencyChange: (Int) -> Unit,
    onDohProviderChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onThemeModeChange: (String) -> Unit,
    onSaveProxy: (String) -> Unit,
    onClearAllData: () -> Unit,
    onResetDefaults: () -> Unit,
    onOpenUserManual: () -> Unit,
    onOpenCapabilities: () -> Unit,
    onOpenPermissions: () -> Unit,
    onOpenAbout: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val swordColors = LocalSwordColors.current
    val animationsEnabled = LocalSwordAnimationsEnabled.current
    val lang = preferences.languageCode
    val listState = rememberLazyListState()

    var proxyInput by remember(preferences.proxyUrl) { mutableStateOf(preferences.proxyUrl) }
    var activeHelpTopic by remember { mutableStateOf<HelpTopic?>(null) }
    var showClearDataConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(scrollToTopTrigger) {
        if (scrollToTopTrigger > 0) {
            if (animationsEnabled) {
                listState.animateScrollToItem(0)
            } else {
                listState.scrollToItem(0)
            }
        }
    }

    activeHelpTopic?.let { topic ->
        ContextualHelpDialog(
            topic = topic,
            languageCode = lang,
            onDismiss = { activeHelpTopic = null }
        )
    }

    if (showClearDataConfirm) {
        AlertDialog(
            onDismissRequest = { showClearDataConfirm = false },
            containerColor = swordColors.cardSurface,
            title = { Text(SwordStrings.label("clear_all_data", lang), color = swordColors.textPrimary) },
            text = { Text(SwordStrings.label("delete_all_reports_confirm", lang), color = swordColors.textSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        showClearDataConfirm = false
                        onClearAllData()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = swordColors.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(SwordStrings.label("confirm", lang))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showClearDataConfirm = false },
                    border = BorderStroke(1.dp, swordColors.border)
                ) {
                    Text(SwordStrings.label("cancel", lang), color = swordColors.textSecondary)
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            SwordTopBar(
                title = SwordStrings.label("settings_title", lang),
                subtitle = SwordStrings.label("settings_subtitle", lang),
                onBack = onBack
            )
        }
    ) { innerPadding ->
        SwordCircuitBackdrop(modifier = Modifier.padding(innerPadding)) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // User Manual prominent card at top of Settings
                item {
                    SwordCard(
                        borderColor = swordColors.accent.copy(alpha = 0.55f),
                        onClick = onOpenUserManual
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = swordColors.accent
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = SwordStrings.label("user_manual", lang),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = swordColors.accent
                                )
                                Text(
                                    text = SwordStrings.label("user_manual_sub", lang),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = swordColors.textSecondary
                                )
                            }
                        }
                    }
                }

                // Appearance & Language
                item {
                    SectionHeader(title = SwordStrings.label("appearance_language", lang))
                }

                item {
                    SwordCard {
                        Text(
                            text = SwordStrings.label("theme_mode", lang),
                            style = MaterialTheme.typography.titleMedium,
                            color = swordColors.accent
                        )
                        Spacer(Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "dark" to SwordStrings.label("theme_dark", lang),
                                "light" to SwordStrings.label("theme_light", lang),
                                "system" to SwordStrings.label("theme_system", lang)
                            ).forEach { (mode, label) ->
                                SwordFilterChip(
                                    selected = preferences.themeMode == mode,
                                    onClick = { onThemeModeChange(mode) },
                                    label = label
                                )
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = SwordStrings.label("language_pref", lang),
                            style = MaterialTheme.typography.titleMedium,
                            color = swordColors.accent
                        )
                        Spacer(Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("en" to "English", "fa" to "فارسی (Persian)").forEach { (code, label) ->
                                SwordFilterChip(
                                    selected = preferences.languageCode == code,
                                    onClick = { onLanguageChange(code) },
                                    label = label
                                )
                            }
                        }
                    }
                }

                // Safety & Execution
                item {
                    SectionHeader(title = SwordStrings.label("safety_execution", lang))
                }

                item {
                    SwordCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = SwordStrings.label("auth_confirm_title", lang),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = swordColors.textPrimary,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    HelpIconButton(
                                        onClick = {
                                            activeHelpTopic = SwordHelpContent.forKey("auth_confirmation", lang)
                                        },
                                        contentDescription = SwordStrings.label("help", lang)
                                    )
                                }
                                Text(
                                    text = SwordStrings.label("auth_confirm_desc", lang),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = swordColors.textSecondary
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Switch(
                                checked = preferences.requireAuthorizationConfirmation,
                                onCheckedChange = onToggleAuthConfirm,
                                colors = swordSwitchColors()
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
                                Text(
                                    text = SwordStrings.label("animations_title", lang),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = swordColors.textPrimary
                                )
                                Text(
                                    text = SwordStrings.label("animations_desc", lang),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = swordColors.textSecondary
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Switch(
                                checked = preferences.animationsEnabled,
                                onCheckedChange = onToggleAnimations,
                                colors = swordSwitchColors()
                            )
                        }
                    }
                }

                item {
                    SwordCard {
                        Text(
                            text = "${SwordStrings.label("network_timeout", lang)}: ${preferences.networkTimeoutSeconds} ${SwordStrings.label("seconds_unit", lang)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = swordColors.textPrimary
                        )
                        Slider(
                            value = preferences.networkTimeoutSeconds.toFloat(),
                            onValueChange = { onTimeoutChange(it.roundToInt()) },
                            valueRange = 3f..30f,
                            colors = swordSliderColors()
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${SwordStrings.label("parallel_limit", lang)}: ${preferences.maxConcurrency}",
                                style = MaterialTheme.typography.titleMedium,
                                color = swordColors.textPrimary,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            HelpIconButton(
                                onClick = {
                                    activeHelpTopic = SwordHelpContent.forKey("parallel_concurrency", lang)
                                },
                                contentDescription = SwordStrings.label("help", lang)
                            )
                        }
                        Slider(
                            value = preferences.maxConcurrency.toFloat(),
                            onValueChange = { onConcurrencyChange(it.roundToInt()) },
                            valueRange = 1f..8f,
                            steps = 6,
                            colors = swordSliderColors()
                        )
                    }
                }

                item {
                    SwordCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = SwordStrings.label("doh_resolver", lang),
                                style = MaterialTheme.typography.titleMedium,
                                color = swordColors.accent,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            HelpIconButton(
                                onClick = {
                                    activeHelpTopic = SwordHelpContent.forKey("doh_resolver", lang)
                                },
                                contentDescription = SwordStrings.label("help", lang)
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Cloudflare", "Google", "Quad9").forEach { provider ->
                                SwordFilterChip(
                                    selected = preferences.dohProvider == provider,
                                    onClick = { onDohProviderChange(provider) },
                                    label = provider
                                )
                            }
                        }
                    }
                }

                item {
                    SwordCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = SwordStrings.label("proxy_title", lang),
                                style = MaterialTheme.typography.titleMedium,
                                color = swordColors.accent,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            HelpIconButton(
                                onClick = {
                                    activeHelpTopic = SwordHelpContent.forKey("proxy_config", lang)
                                },
                                contentDescription = SwordStrings.label("help", lang)
                            )
                        }
                        Text(
                            text = SwordStrings.label("proxy_desc", lang),
                            style = MaterialTheme.typography.bodyMedium,
                            color = swordColors.textSecondary
                        )
                        Spacer(Modifier.height(8.dp))
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            OutlinedTextField(
                                value = proxyInput,
                                onValueChange = { proxyInput = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("http://127.0.0.1:8080") },
                                singleLine = true,
                                colors = swordOutlinedTextFieldColors()
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { onSaveProxy(proxyInput) },
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(SwordStrings.label("save_proxy", lang))
                        }
                    }
                }

                // System & Info
                item {
                    SectionHeader(title = SwordStrings.label("system_help_section", lang))
                }

                item {
                    SwordCard(onClick = onOpenCapabilities) {
                        Text(
                            text = SwordStrings.label("capabilities", lang),
                            style = MaterialTheme.typography.titleMedium,
                            color = swordColors.textPrimary
                        )
                        Text(
                            text = if (SwordStrings.isPersian(lang)) {
                                "بررسی وضعیت اینترنت، نسخه اندروید و موتور پایتون ۳.۱۰"
                            } else {
                                "Inspect connectivity, Android API readiness, and Python 3.10 runtime."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = swordColors.textSecondary
                        )
                    }
                }

                item {
                    SwordCard(onClick = onOpenPermissions) {
                        Text(
                            text = SwordStrings.label("open_permissions", lang),
                            style = MaterialTheme.typography.titleMedium,
                            color = swordColors.textPrimary
                        )
                        Text(
                            text = if (SwordStrings.isPersian(lang)) {
                                "مدیریت و بررسی مجوزهای وای‌فای و مکان"
                            } else {
                                "Review and grant optional Wi-Fi and Location permissions."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = swordColors.textSecondary
                        )
                    }
                }

                item {
                    SwordCard(onClick = onOpenAbout) {
                        Text(
                            text = SwordStrings.label("about_sword", lang),
                            style = MaterialTheme.typography.titleMedium,
                            color = swordColors.textPrimary
                        )
                        Text(
                            text = if (SwordStrings.isPersian(lang)) {
                                "معرفی، معماری امنیتی، مجوزها و نسخه برنامه"
                            } else {
                                "Architecture, security policy, third-party notices, and version."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = swordColors.textSecondary
                        )
                    }
                }

                // Data Cleanup
                item {
                    SectionHeader(title = SwordStrings.label("data_cleanup", lang))
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showClearDataConfirm = true },
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 48.dp),
                            border = BorderStroke(1.dp, swordColors.error.copy(alpha = 0.6f))
                        ) {
                            Text(SwordStrings.label("clear_all_data", lang), color = swordColors.error)
                        }
                        OutlinedButton(
                            onClick = onResetDefaults,
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 48.dp),
                            border = BorderStroke(1.dp, swordColors.border)
                        ) {
                            Text(SwordStrings.label("reset_defaults", lang), color = swordColors.accent)
                        }
                    }
                }
            }
        }
    }
}
