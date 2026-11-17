package com.sword.security.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class SwordSemanticColors(
    val isDark: Boolean,
    val topBarBackground: Color,
    val topBarContent: Color,
    val accent: Color,
    val onAccent: Color,
    val cardSurface: Color,
    val elevatedSurface: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
    val severityCritical: Color,
    val severityHigh: Color,
    val severityMedium: Color,
    val severityLow: Color,
    val severityInfo: Color,
    val codeSurface: Color,
    val codeText: Color,
    val jsonKey: Color,
    val jsonString: Color,
    val jsonNumber: Color,
    val jsonBoolean: Color,
    val jsonPunctuation: Color
)

private val DarkSemanticColors = SwordSemanticColors(
    isDark = true,
    topBarBackground = SwordDeepNavy,
    topBarContent = SwordTextPrimary,
    accent = SwordPrimaryCyan,
    onAccent = SwordDeepNavy,
    cardSurface = SwordCardSurface,
    elevatedSurface = SwordSurfaceElevated,
    border = SwordBorder,
    textPrimary = SwordTextPrimary,
    textSecondary = SwordTextSecondary,
    success = SwordSuccess,
    warning = SwordWarning,
    error = SwordError,
    severityCritical = SwordSeverityCritical,
    severityHigh = SwordSeverityHigh,
    severityMedium = SwordSeverityMedium,
    severityLow = SwordSeverityLow,
    severityInfo = SwordSeverityInfo,
    codeSurface = SwordDarkCodeSurface,
    codeText = SwordDarkCodeText,
    jsonKey = Color(0xFF4DD9E8),
    jsonString = Color(0xFF7BE8B3),
    jsonNumber = Color(0xFFFFCA6E),
    jsonBoolean = Color(0xFFFF8BA7),
    jsonPunctuation = Color(0xFF8FA1B3)
)

private val LightSemanticColors = SwordSemanticColors(
    isDark = false,
    topBarBackground = SwordLightTopBar,
    topBarContent = SwordLightTextPrimary,
    accent = SwordLightPrimary,
    onAccent = Color.White,
    cardSurface = SwordLightCardSurface,
    elevatedSurface = SwordLightSurfaceElevated,
    border = SwordLightBorder,
    textPrimary = SwordLightTextPrimary,
    textSecondary = SwordLightTextSecondary,
    success = SwordLightSuccess,
    warning = SwordLightWarning,
    error = SwordLightError,
    severityCritical = SwordLightSeverityCritical,
    severityHigh = SwordLightSeverityHigh,
    severityMedium = SwordLightSeverityMedium,
    severityLow = SwordLightSeverityLow,
    severityInfo = SwordLightSeverityInfo,
    codeSurface = SwordLightCodeSurface,
    codeText = SwordLightCodeText,
    jsonKey = Color(0xFF005B82),
    jsonString = Color(0xFF0E6B42),
    jsonNumber = Color(0xFF9C4700),
    jsonBoolean = Color(0xFF9E1C42),
    jsonPunctuation = Color(0xFF546678)
)

val LocalSwordColors = staticCompositionLocalOf { DarkSemanticColors }

val SwordDarkColorScheme = darkColorScheme(
    primary = SwordPrimaryCyan,
    onPrimary = SwordDeepNavy,
    primaryContainer = SwordDeepNavy,
    onPrimaryContainer = SwordPrimaryCyan,
    secondary = SwordSecondaryTurquoise,
    onSecondary = SwordDeepNavy,
    secondaryContainer = SwordSurfaceElevated,
    onSecondaryContainer = SwordTextPrimary,
    tertiary = SwordSeverityInfo,
    onTertiary = SwordDeepNavy,
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

val SwordLightColorScheme = lightColorScheme(
    primary = SwordLightPrimary,
    onPrimary = Color.White,
    primaryContainer = SwordLightPrimaryContainer,
    onPrimaryContainer = SwordLightOnPrimaryContainer,
    secondary = SwordLightSecondary,
    onSecondary = Color.White,
    secondaryContainer = SwordLightSecondaryContainer,
    onSecondaryContainer = SwordLightTextPrimary,
    tertiary = SwordLightSeverityInfo,
    onTertiary = Color.White,
    background = SwordLightBackground,
    onBackground = SwordLightTextPrimary,
    surface = SwordLightCardSurface,
    onSurface = SwordLightTextPrimary,
    surfaceVariant = SwordLightSurfaceElevated,
    onSurfaceVariant = SwordLightTextSecondary,
    outline = SwordLightBorder,
    outlineVariant = SwordLightBorder,
    error = SwordLightError,
    onError = Color.White
)

@Composable
fun SwordTheme(
    themeMode: String = "dark",
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val useDark = when (themeMode) {
        "light" -> false
        "system" -> systemDark
        else -> true
    }
    val colorScheme = if (useDark) SwordDarkColorScheme else SwordLightColorScheme
    val semanticColors = if (useDark) DarkSemanticColors else LightSemanticColors

    CompositionLocalProvider(LocalSwordColors provides semanticColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = SwordTypography,
            content = content
        )
    }
}
