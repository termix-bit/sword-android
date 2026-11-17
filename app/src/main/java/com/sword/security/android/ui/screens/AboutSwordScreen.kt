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
import com.sword.security.android.core.i18n.SwordStrings
import com.sword.security.android.ui.components.SwordCard
import com.sword.security.android.ui.components.SwordCircuitBackdrop
import com.sword.security.android.ui.components.SwordLogoBadge
import com.sword.security.android.ui.components.SwordTopBar
import com.sword.security.android.ui.theme.LocalSwordColors

@Composable
fun AboutSwordScreen(
    pythonVersion: String,
    languageCode: String = "en",
    onBack: () -> Unit
) {
    val swordColors = LocalSwordColors.current
    val isFa = SwordStrings.isPersian(languageCode)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            SwordTopBar(
                title = SwordStrings.label("about_sword", languageCode),
                subtitle = SwordStrings.label("app_subtitle", languageCode),
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
                SwordCard(borderColor = swordColors.accent.copy(alpha = 0.45f)) {
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
                            ),
                            color = swordColors.textPrimary
                        )
                        Text(
                            text = SwordStrings.label("app_subtitle", languageCode),
                            style = MaterialTheme.typography.titleMedium,
                            color = swordColors.accent
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                            style = MaterialTheme.typography.labelSmall,
                            color = swordColors.textSecondary
                        )
                    }
                }

                SwordCard {
                    Text(
                        text = if (isFa) "خانواده محصولات Sword" else "Sword Product Family",
                        style = MaterialTheme.typography.titleMedium,
                        color = swordColors.accent
                    )
                    Spacer(Modifier.height(6.dp))
                    Text("• Sword Termux — Terminal Security Framework", style = MaterialTheme.typography.bodyMedium, color = swordColors.textPrimary)
                    Text("• Sword Linux — Desktop/Server Security Suite", style = MaterialTheme.typography.bodyMedium, color = swordColors.textPrimary)
                    Text("• Sword Android — Native Android Security Toolkit", style = MaterialTheme.typography.bodyMedium, color = swordColors.accent)
                }

                SwordCard {
                    Text(
                        text = if (isFa) "معماری و موتور داخلی" else "Architecture & Embedded Runtime",
                        style = MaterialTheme.typography.titleMedium,
                        color = swordColors.accent
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Built natively with Kotlin, Jetpack Compose, Room, DataStore, OkHttp, AndroidX ExifInterface, and bundled Chaquopy Python ($pythonVersion) inside the APK.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = swordColors.textPrimary
                    )
                }

                SwordCard {
                    Text(
                        text = if (isFa) "سیاست استفاده ایمن و مجاز" else "Authorized Testing & Security Policy",
                        style = MaterialTheme.typography.titleMedium,
                        color = swordColors.accent
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = if (isFa) {
                            "برنامه Sword Android صرفاً برای آموزش، عیب‌یابی دفاعی و آزمون‌های مجاز ساخته شده است. ابزارهای تهاجمی، قطع ارتباط وای‌فای و ترافیک غیررمزگذاری‌شده در برنامه وجود ندارند."
                        } else {
                            "Sword Android is strictly intended for education, defensive diagnostics, and authorized security assessments. Offensive exploit tools, deauthentication, evil-twin attacks, and cleartext credential telemetry are excluded."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = swordColors.textPrimary
                    )
                }

                SwordCard {
                    Text(
                        text = if (isFa) "اعلان‌های شخص ثالث" else "Third-Party Notices",
                        style = MaterialTheme.typography.titleMedium,
                        color = swordColors.accent
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Username OSINT dataset: WebBreacher/WhatsMyName (wmn-data.json), licensed under Creative Commons Attribution-ShareAlike 4.0 International (CC BY-SA 4.0). Reference architecture inspired by termix-bit/sword (MIT).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = swordColors.textPrimary
                    )
                }
            }
        }
    }
}
