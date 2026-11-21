package com.sword.security.android.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.sword.security.android.core.i18n.ScanResultTranslator
import com.sword.security.android.core.i18n.SwordStrings
import com.sword.security.android.core.i18n.TranslatedFindingExplanation
import com.sword.security.android.core.model.SecurityFinding
import com.sword.security.android.ui.theme.LocalSwordColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScanExplanationPanel(
    toolName: String,
    targetSummary: String,
    findings: List<SecurityFinding>,
    defaultLanguageCode: String,
    rawOutputAvailable: Boolean = true,
    modifier: Modifier = Modifier
) {
    val swordColors = LocalSwordColors.current
    var explanationLang by rememberSaveable(defaultLanguageCode) {
        mutableStateOf(if (defaultLanguageCode == "fa") "fa" else "en")
    }

    val translationResult = remember(
        toolName,
        targetSummary,
        findings,
        explanationLang,
        rawOutputAvailable
    ) {
        runCatching {
            ScanResultTranslator.translate(
                toolName = toolName,
                targetSummary = targetSummary,
                findings = findings,
                languageCode = explanationLang,
                rawOutputAvailable = rawOutputAvailable
            )
        }
    }
    val summary = translationResult.getOrNull()
    val errorMsg = translationResult.exceptionOrNull()?.let { err ->
        err.message ?: SwordStrings.label("explain_error", explanationLang)
    }

    val panelDirection = if (explanationLang == "fa") LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides panelDirection) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Language switch & offline privacy guarantee banner
            SwordCard(borderColor = swordColors.accent.copy(alpha = 0.45f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = null,
                        tint = swordColors.accent,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = SwordStrings.label("explain_lang_label", explanationLang),
                        style = MaterialTheme.typography.titleMedium,
                        color = swordColors.textPrimary
                    )
                }
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("en" to "English", "fa" to "فارسی (Persian)").forEach { (code, label) ->
                        SwordFilterChip(
                            selected = explanationLang == code,
                            onClick = { explanationLang = code },
                            label = label
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Surface(
                    color = swordColors.elevatedSurface,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, swordColors.success.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = swordColors.success,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = SwordStrings.label("explain_privacy_note", explanationLang),
                            style = MaterialTheme.typography.bodyMedium,
                            color = swordColors.textSecondary
                        )
                    }
                }
            }

            if (errorMsg != null) {
                SwordErrorCard(
                    title = SwordStrings.label("explain_error", explanationLang),
                    message = errorMsg.orEmpty()
                )
            } else {
                val data = summary
                if (data != null) {
                    SwordCard {
                        Text(
                            text = data.overallPlainSummary,
                            style = MaterialTheme.typography.bodyLarge,
                            color = swordColors.textPrimary
                        )
                        if (!data.limitationNotice.isNullOrBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Surface(
                                color = swordColors.elevatedSurface,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, swordColors.warning.copy(alpha = 0.45f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = swordColors.warning,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = data.limitationNotice,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = swordColors.textPrimary
                                    )
                                }
                            }
                        }
                    }

                    if (data.findings.isEmpty()) {
                        SwordEmptyStateCard(
                            icon = Icons.Default.Translate,
                            title = SwordStrings.label("explain_results", explanationLang),
                            message = SwordStrings.label("explain_no_findings", explanationLang)
                        )
                    } else {
                        data.findings.forEach { item ->
                            TranslatedFindingCard(item = item, languageCode = explanationLang)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TranslatedFindingCard(
    item: TranslatedFindingExplanation,
    languageCode: String
) {
    val swordColors = LocalSwordColors.current
    SwordCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.simpleTitle,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = swordColors.accent,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            SeverityBadge(item.originalSeverity)
        }

        Spacer(Modifier.height(6.dp))
        Text(
            text = SwordStrings.label("explain_original_title", languageCode),
            style = MaterialTheme.typography.labelSmall,
            color = swordColors.textSecondary
        )
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Text(
                text = item.originalTitle,
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                color = swordColors.textPrimary
            )
        }

        Spacer(Modifier.height(10.dp))
        ExplanationSubBlock(
            label = SwordStrings.label("explain_what_it_means", languageCode),
            body = item.whatItMeans
        )
        Spacer(Modifier.height(8.dp))
        ExplanationSubBlock(
            label = SwordStrings.label("explain_why_it_matters", languageCode),
            body = item.whyItMatters
        )
        Spacer(Modifier.height(8.dp))
        ExplanationSubBlock(
            label = SwordStrings.label("explain_impact", languageCode),
            body = item.potentialImpact
        )
        Spacer(Modifier.height(8.dp))
        ExplanationSubBlock(
            label = SwordStrings.label("explain_next_steps", languageCode),
            body = item.recommendedNextSteps
        )

        if (item.preservedIdentifiers.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = SwordStrings.label("explain_identifiers", languageCode),
                style = MaterialTheme.typography.labelSmall,
                color = swordColors.textSecondary
            )
            Spacer(Modifier.height(4.dp))
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item.preservedIdentifiers.forEach { token ->
                        Surface(
                            color = swordColors.codeSurface,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, swordColors.border)
                        ) {
                            Text(
                                text = token,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                color = swordColors.codeText
                            )
                        }
                    }
                }
            }
        }

        if (!item.limitationNote.isNullOrBlank()) {
            Spacer(Modifier.height(10.dp))
            Surface(
                color = swordColors.elevatedSurface,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, swordColors.warning.copy(alpha = 0.45f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = SwordStrings.label("explain_limitation_title", languageCode),
                        style = MaterialTheme.typography.labelSmall,
                        color = swordColors.warning
                    )
                    Text(
                        text = item.limitationNote,
                        style = MaterialTheme.typography.bodyMedium,
                        color = swordColors.textPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun ExplanationSubBlock(label: String, body: String) {
    val swordColors = LocalSwordColors.current
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = swordColors.accent
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = swordColors.textPrimary
        )
    }
}
