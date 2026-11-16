package com.sword.security.android.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sword.security.android.BuildConfig
import com.sword.security.android.ui.components.SwordCard
import com.sword.security.android.ui.components.SwordCircuitBackdrop
import com.sword.security.android.ui.components.SwordLogoBadge
import com.sword.security.android.ui.components.SwordTopBar
import com.sword.security.android.ui.theme.SwordBackground
import com.sword.security.android.ui.theme.SwordPrimaryCyan
import com.sword.security.android.ui.theme.SwordTextPrimary
import com.sword.security.android.ui.theme.SwordTextSecondary

@Composable
fun AboutSwordScreen(
    pythonVersion: String,
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = SwordBackground,
        topBar = {
            SwordTopBar(
                title = "About Sword",
                subtitle = "Android Security Toolkit",
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
                SwordCard(borderColor = SwordPrimaryCyan.copy(alpha = 0.4f)) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        SwordLogoBadge(size = 84.dp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "SWORD",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 4.sp
                            )
                        )
                        Text(
                            text = "Android Security Toolkit",
                            style = MaterialTheme.typography.titleMedium,
                            color = SwordPrimaryCyan
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                            style = MaterialTheme.typography.labelSmall,
                            color = SwordTextSecondary
                        )
                    }
                }

                SwordCard {
                    Text("Sword Product Family", style = MaterialTheme.typography.titleMedium, color = SwordPrimaryCyan)
                    Spacer(Modifier.height(6.dp))
                    Text("• Sword Termux — Terminal Security Framework", style = MaterialTheme.typography.bodyMedium, color = SwordTextPrimary)
                    Text("• Sword Linux — Desktop/Server Security Suite", style = MaterialTheme.typography.bodyMedium, color = SwordTextPrimary)
                    Text("• Sword Android — Native Android Security Toolkit", style = MaterialTheme.typography.bodyMedium, color = SwordPrimaryCyan)
                }

                SwordCard {
                    Text("Architecture & Embedded Runtime", style = MaterialTheme.typography.titleMedium, color = SwordPrimaryCyan)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Built natively with Kotlin, Jetpack Compose, Room, DataStore, OkHttp, AndroidX ExifInterface, and bundled Chaquopy Python ($pythonVersion) inside the APK.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                SwordCard {
                    Text("Authorized Testing & Security Policy", style = MaterialTheme.typography.titleMedium, color = SwordPrimaryCyan)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Sword Android is strictly intended for education, defensive diagnostics, and authorized security assessments. Offensive exploit tools, deauthentication, evil-twin attacks, and cleartext credential telemetry are excluded.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                SwordCard {
                    Text("Third-Party Notices", style = MaterialTheme.typography.titleMedium, color = SwordPrimaryCyan)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Username OSINT dataset: WebBreacher/WhatsMyName (wmn-data.json), licensed under Creative Commons Attribution-ShareAlike 4.0 International (CC BY-SA 4.0). Reference architecture inspired by termix-bit/sword (MIT).",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
