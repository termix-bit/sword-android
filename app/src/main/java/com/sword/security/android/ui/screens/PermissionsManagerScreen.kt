package com.sword.security.android.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sword.security.android.core.i18n.SwordHelpContent
import com.sword.security.android.core.i18n.SwordStrings
import com.sword.security.android.core.model.PermissionStatusItem
import com.sword.security.android.ui.components.ContextualHelpDialog
import com.sword.security.android.ui.components.HelpIconButton
import com.sword.security.android.ui.components.SwordCard
import com.sword.security.android.ui.components.SwordCircuitBackdrop
import com.sword.security.android.ui.components.SwordTopBar
import com.sword.security.android.ui.theme.LocalSwordColors

@Composable
fun PermissionsManagerScreen(
    permissions: List<PermissionStatusItem>,
    wifiPermissionsToRequest: Array<String>,
    languageCode: String = "en",
    onPermissionsUpdated: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val swordColors = LocalSwordColors.current
    val isFa = SwordStrings.isPersian(languageCode)
    var showHelpDialog by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        onPermissionsUpdated()
    }

    if (showHelpDialog) {
        ContextualHelpDialog(
            topic = SwordHelpContent.forKey("permissions_overview", languageCode),
            languageCode = languageCode,
            onDismiss = { showHelpDialog = false }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            SwordTopBar(
                title = SwordStrings.label("open_permissions", languageCode),
                subtitle = if (isFa) "دسترسی‌های اختیاری اندروید" else "Android Optional Access",
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
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    SwordCard {
                        Text(
                            text = if (isFa) {
                                "Sword فقط حداقل مجوزهای لازم را می‌خواهد. بررسی فایل مستقیماً از انتخابگر اندروید استفاده می‌کند و به کل حافظه دسترسی ندارد."
                            } else {
                                "Sword requests only minimal permissions required for diagnostics. File inspection uses Android's document picker without broad storage access."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = swordColors.textPrimary
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { launcher.launch(wifiPermissionsToRequest) },
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text(
                                    text = if (isFa) "درخواست مجوز وای‌فای" else "Request Wi-Fi Perms",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", context.packageName, null)
                                    )
                                    context.startActivity(intent)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp),
                                border = BorderStroke(1.dp, swordColors.border)
                            ) {
                                Text(
                                    text = if (isFa) "تنظیمات سیستم" else "System Settings",
                                    color = swordColors.accent
                                )
                            }
                        }
                    }
                }

                items(permissions, key = { it.permission }) { item ->
                    SwordCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                color = swordColors.textPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = when {
                                    !item.isApplicableToDevice -> if (isFa) "غیرمرتبط" else "N/A (API)"
                                    item.isGranted -> if (isFa) "فعال" else "GRANTED"
                                    else -> if (isFa) "غیرفعال" else "NOT GRANTED"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (item.isGranted) swordColors.success else swordColors.warning
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(item.rationale, style = MaterialTheme.typography.bodyMedium, color = swordColors.textPrimary)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "${if (isFa) "ابزارهای مرتبط:" else "Used by:"} ${item.affectedModules.joinToString(", ")}",
                            style = MaterialTheme.typography.labelSmall,
                            color = swordColors.textSecondary
                        )
                    }
                }
            }
        }
    }
}
