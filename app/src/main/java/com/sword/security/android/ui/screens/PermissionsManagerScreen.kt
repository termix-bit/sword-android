package com.sword.security.android.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sword.security.android.core.model.PermissionStatusItem
import com.sword.security.android.ui.components.SwordCard
import com.sword.security.android.ui.components.SwordCircuitBackdrop
import com.sword.security.android.ui.components.SwordTopBar
import com.sword.security.android.ui.theme.SwordBackground
import com.sword.security.android.ui.theme.SwordDeepNavy
import com.sword.security.android.ui.theme.SwordPrimaryCyan
import com.sword.security.android.ui.theme.SwordSuccess
import com.sword.security.android.ui.theme.SwordTextPrimary
import com.sword.security.android.ui.theme.SwordTextSecondary
import com.sword.security.android.ui.theme.SwordWarning

@Composable
fun PermissionsManagerScreen(
    permissions: List<PermissionStatusItem>,
    wifiPermissionsToRequest: Array<String>,
    onPermissionsUpdated: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        onPermissionsUpdated()
    }

    Scaffold(
        containerColor = SwordBackground,
        topBar = {
            SwordTopBar(
                title = "Permissions Manager",
                subtitle = "Android Runtime & Manifest Access",
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
                    SwordCard {
                        Text(
                            text = "Sword requests only minimal permissions required for diagnostics. Storage Forensics uses Android SAF without broad storage access.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { launcher.launch(wifiPermissionsToRequest) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SwordPrimaryCyan,
                                    contentColor = SwordDeepNavy
                                )
                            ) {
                                Text("Request Wi-Fi Perms", fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", context.packageName, null)
                                    )
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("System Settings", color = SwordPrimaryCyan)
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
                                color = SwordTextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = when {
                                    !item.isApplicableToDevice -> "N/A (API)"
                                    item.isGranted -> "GRANTED"
                                    else -> "NOT GRANTED"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (item.isGranted) SwordSuccess else SwordWarning
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(item.rationale, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Used by: ${item.affectedModules.joinToString(", ")}",
                            style = MaterialTheme.typography.labelSmall,
                            color = SwordTextSecondary
                        )
                    }
                }
            }
        }
    }
}
