package com.sword.security.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SwordDarkColorScheme = darkColorScheme(
    primary = SwordPrimaryCyan,
    onPrimary = SwordDeepNavy,
    primaryContainer = SwordDeepNavy,
    onPrimaryContainer = SwordPrimaryCyan,
    secondary = SwordSecondaryTurquoise,
    onSecondary = SwordDeepNavy,
    secondaryContainer = SwordSurfaceElevated,
    onSecondaryContainer = SwordTextPrimary,
    tertiary = SwordSeverityInfo,
    background = SwordBackground,
    onBackground = SwordTextPrimary,
    surface = SwordCardSurface,
    onSurface = SwordTextPrimary,
    surfaceVariant = SwordSurfaceElevated,
    onSurfaceVariant = SwordTextSecondary,
    outline = SwordBorder,
    outlineVariant = SwordBorder,
    error = SwordError,
    onError = SwordDeepNavy
)

@Composable
fun SwordTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SwordDarkColorScheme,
        typography = SwordTypography,
        content = content
    )
}
