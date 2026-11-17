package com.sword.security.android.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sword.security.android.core.i18n.SwordStrings
import com.sword.security.android.ui.components.SwordCard
import com.sword.security.android.ui.components.SwordCircuitBackdrop
import com.sword.security.android.ui.components.SwordLogoBadge
import com.sword.security.android.ui.theme.LocalSwordColors

@Composable
fun OnboardingScreen(
    languageCode: String = "en",
    onGetStarted: () -> Unit
) {
    val swordColors = LocalSwordColors.current
    val isFa = SwordStrings.isPersian(languageCode)

    SwordCircuitBackdrop {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            SwordLogoBadge(size = 88.dp)
            Text(
                text = "SWORD",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 5.sp
                ),
                color = swordColors.textPrimary
            )
            Text(
                text = SwordStrings.label("app_subtitle", languageCode),
                style = MaterialTheme.typography.titleMedium,
                color = swordColors.accent
            )
            Text(
                text = SwordStrings.label("home_hero_desc", languageCode),
                style = MaterialTheme.typography.bodyMedium,
                color = swordColors.textSecondary
            )

            OnboardingPillarCard(
                icon = Icons.Default.Security,
                title = if (isFa) "اجرای بومی بدون نیاز به روت" else "Native Android Architecture",
                body = if (isFa) {
                    "بدون نیاز به Termux یا دسترسی روت. بررسی شبکه، وب، وای‌فای و فایل‌ها با APIهای استاندارد اندروید."
                } else {
                    "No Termux wrapper or root requirement. Uses native Android Connectivity, Wifi, SAF, Exif, DoH, and TLS APIs."
                }
            )
            OnboardingPillarCard(
                icon = Icons.Default.Terminal,
                title = if (isFa) "موتور پایتون ۳.۱۰ داخل برنامه" else "Bundled Python 3.10 Runtime",
                body = if (isFa) {
                    "برخی تحلیل‌ها (مانند بررسی نام کاربری OSINT) به‌صورت ایمن و بدون نیاز به نصب اضافی با پایتون داخلی اجرا می‌شوند."
                } else {
                    "Selective Sword modules (such as username OSINT checks) run safely inside the APK via Chaquopy without extra installs."
                }
            )
            OnboardingPillarCard(
                icon = Icons.Default.Shield,
                title = if (isFa) "فقط برای اهداف مجاز" else "Authorized Use Only",
                body = if (isFa) {
                    "فقط دستگاه‌ها، دامنه‌ها و شبکه‌هایی را بررسی کنید که مالک آن هستید یا اجازه صریح دارید."
                } else {
                    "Use Sword only on devices, domains, and networks you own or have explicit permission to assess."
                }
            )

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = if (isFa) "تأیید و ورود به برنامه" else "Acknowledge & Enter Toolkit",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun OnboardingPillarCard(
    icon: ImageVector,
    title: String,
    body: String
) {
    val swordColors = LocalSwordColors.current
    SwordCard {
        Row(verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = swordColors.accent
            )
            Spacer(Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = swordColors.textPrimary)
                Text(body, style = MaterialTheme.typography.bodyMedium, color = swordColors.textSecondary)
            }
        }
    }
}
