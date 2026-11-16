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
import com.sword.security.android.ui.components.SwordCard
import com.sword.security.android.ui.components.SwordCircuitBackdrop
import com.sword.security.android.ui.components.SwordLogoBadge
import com.sword.security.android.ui.theme.SwordDeepNavy
import com.sword.security.android.ui.theme.SwordPrimaryCyan
import com.sword.security.android.ui.theme.SwordTextPrimary
import com.sword.security.android.ui.theme.SwordTextSecondary

@Composable
fun OnboardingScreen(
    onGetStarted: () -> Unit
) {
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
                )
            )
            Text(
                text = "Android Security Toolkit",
                style = MaterialTheme.typography.titleMedium,
                color = SwordPrimaryCyan
            )
            Text(
                text = "Independent native Android diagnostic, OSINT, cryptography, and forensics toolkit built with Android-first security boundaries.",
                style = MaterialTheme.typography.bodyMedium,
                color = SwordTextSecondary
            )

            OnboardingPillarCard(
                icon = Icons.Default.Security,
                title = "Native Android Architecture",
                body = "No Termux wrapper or root requirement. Uses Android ConnectivityManager, WifiManager, SAF, ExifInterface, DoH, and TLS APIs."
            )
            OnboardingPillarCard(
                icon = Icons.Default.Terminal,
                title = "Bundled Python 3.10 Runtime",
                body = "Selective Sword logic (such as the WhatsMyName username OSINT engine) runs safely inside the APK via Chaquopy without extra installs."
            )
            OnboardingPillarCard(
                icon = Icons.Default.Shield,
                title = "Authorized Use Only",
                body = "Use Sword only on devices, domains, and networks you own or have explicit permission to assess. Unsupported raw-packet or offensive tools are excluded."
            )

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SwordPrimaryCyan,
                    contentColor = SwordDeepNavy
                )
            ) {
                Text(
                    text = "Acknowledge & Enter Toolkit",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = SwordDeepNavy,
                        fontWeight = FontWeight.Bold
                    )
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
    SwordCard {
        Row(verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SwordPrimaryCyan
            )
            Spacer(Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = SwordTextPrimary)
                Text(body, style = MaterialTheme.typography.bodyMedium, color = SwordTextSecondary)
            }
        }
    }
}
