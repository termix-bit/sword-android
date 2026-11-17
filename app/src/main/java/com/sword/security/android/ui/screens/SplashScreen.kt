package com.sword.security.android.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sword.security.android.core.i18n.SwordStrings
import com.sword.security.android.ui.components.SwordCircuitBackdrop
import com.sword.security.android.ui.components.SwordLogoBadge
import com.sword.security.android.ui.theme.LocalSwordColors
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    animationsEnabled: Boolean,
    onboardingCompleted: Boolean,
    languageCode: String = "en",
    onNavigateToOnboarding: () -> Unit,
    onNavigateToDashboard: () -> Unit
) {
    val swordColors = LocalSwordColors.current
    val isFa = SwordStrings.isPersian(languageCode)

    LaunchedEffect(onboardingCompleted) {
        delay(if (animationsEnabled) 750L else 180L)
        if (onboardingCompleted) {
            onNavigateToDashboard()
        } else {
            onNavigateToOnboarding()
        }
    }

    SwordCircuitBackdrop {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SwordLogoBadge(size = 112.dp)
            Spacer(Modifier.height(24.dp))
            Text(
                text = "SWORD",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 6.sp,
                    color = swordColors.textPrimary
                )
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = SwordStrings.label("app_subtitle", languageCode),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = swordColors.accent,
                    letterSpacing = 1.sp
                )
            )
            Spacer(Modifier.height(32.dp))
            LinearProgressIndicator(
                modifier = Modifier.width(180.dp),
                color = swordColors.accent,
                trackColor = swordColors.border
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = if (isFa) "در حال آماده‌سازی ابزارها و بررسی قابلیت‌های دستگاه..." else "Initializing engine & device readiness checks...",
                style = MaterialTheme.typography.labelSmall,
                color = swordColors.textSecondary
            )
        }
    }
}
