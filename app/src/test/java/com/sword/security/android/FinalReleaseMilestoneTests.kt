package com.sword.security.android

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.core.app.ApplicationProvider
import com.sword.security.android.core.i18n.ScanResultTranslator
import com.sword.security.android.core.i18n.SwordStrings
import com.sword.security.android.core.model.ExecutionProgress
import com.sword.security.android.core.model.ExecutionResult
import com.sword.security.android.core.model.ExecutionStatus
import com.sword.security.android.core.model.ModuleCategory
import com.sword.security.android.core.model.SecurityFinding
import com.sword.security.android.core.model.SeverityLevel
import com.sword.security.android.data.local.db.ReportMetadataEntity
import com.sword.security.android.data.local.prefs.UserPreferences
import com.sword.security.android.data.local.prefs.UserPreferencesRepository
import com.sword.security.android.data.reports.ReportViewerData
import com.sword.security.android.data.reports.SavedJsonReport
import com.sword.security.android.ui.screens.LiveExecutionScreen
import com.sword.security.android.ui.screens.OnboardingScreen
import com.sword.security.android.ui.screens.ReportViewerScreen
import com.sword.security.android.ui.screens.SettingsScreen
import com.sword.security.android.ui.theme.SwordTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FinalReleaseMilestoneTests {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `translator generates beginner explanations in english and persian while preserving identifiers and severity`() {
        val originalFindings = listOf(
            SecurityFinding(
                title = "Missing Strict-Transport-Security (HSTS)",
                severity = "Medium",
                detail = "Site https://api.example.org (192.168.1.10) returned HTTP 200 without HSTS; legacy TLSv1.0 or CVE-2024-1234 notes.",
                recommendation = "Add Strict-Transport-Security header on TCP/443."
            ),
            SecurityFinding(
                title = "Unknown Custom Probe",
                severity = "High",
                detail = "",
                recommendation = ""
            )
        )

        val enSummary = ScanResultTranslator.translate(
            toolName = "Header Audit",
            targetSummary = "https://api.example.org",
            findings = originalFindings,
            languageCode = "en"
        )
        assertEquals("en", enSummary.explanationLanguage)
        assertEquals(2, enSummary.findings.size)
        assertNotNull(enSummary.limitationNotice)

        val firstEn = enSummary.findings[0]
        assertEquals("Missing Strict-Transport-Security (HSTS)", firstEn.originalTitle)
        assertEquals("Medium", firstEn.originalSeverity)
        assertTrue(firstEn.hasSufficientContext)
        assertTrue(firstEn.whatItMeans.contains("Strict-Transport-Security"))
        assertTrue(firstEn.whyItMatters.isNotBlank())
        assertTrue(firstEn.potentialImpact.isNotBlank())
        assertTrue(firstEn.recommendedNextSteps.contains("TCP/443"))
        assertTrue(firstEn.preservedIdentifiers.contains("CVE-2024-1234"))
        assertTrue(firstEn.preservedIdentifiers.contains("192.168.1.10"))
        assertTrue(firstEn.preservedIdentifiers.contains("TLSv1.0"))
        assertTrue(firstEn.preservedIdentifiers.contains("HTTP 200"))
        assertTrue(firstEn.preservedIdentifiers.any { it.contains("api.example.org") })

        val unknownEn = enSummary.findings[1]
        assertFalse(unknownEn.hasSufficientContext)
        assertEquals("High", unknownEn.originalSeverity)
        assertNotNull(unknownEn.limitationNote)

        val faSummary = ScanResultTranslator.translate(
            toolName = "Header Audit",
            targetSummary = "https://api.example.org",
            findings = originalFindings,
            languageCode = "fa"
        )
        assertEquals("fa", faSummary.explanationLanguage)
        assertTrue(faSummary.findings[0].simpleTitle.contains("HSTS"))
        assertTrue(faSummary.findings[0].whatItMeans.contains("سربرگ"))
        assertTrue(faSummary.findings[0].preservedIdentifiers.contains("CVE-2024-1234"))
    }

    @Test
    fun `translator handles empty findings list without inventing issues`() {
        val summary = ScanResultTranslator.translate(
            toolName = "DNS Lookup",
            targetSummary = "example.com",
            findings = emptyList(),
            languageCode = "en",
            rawOutputAvailable = true
        )
        assertTrue(summary.findings.isEmpty())
        assertNotNull(summary.limitationNotice)
        assertTrue(summary.overallPlainSummary.contains("DNS Lookup"))
    }

    @Test
    fun `onboarding shows language selection first switches to rtl live and requires finish or skip to complete`() {
        var currentLang by mutableStateOf("en")
        var completedCount = 0
        var skippedCount = 0

        composeRule.setContent {
            SwordTheme(themeMode = "dark") {
                OnboardingScreen(
                    languageCode = currentLang,
                    isReplay = false,
                    onSelectLanguage = { currentLang = it },
                    onFinishOnboarding = { completedCount++ },
                    onSkipOnboarding = { skippedCount++ }
                )
            }
        }

        // Step 0 displays Language Selection before tutorial in LTR
        val enNode = composeRule.onNodeWithText("Choose Your Language").performScrollTo().assertIsDisplayed().fetchSemanticsNode()
        assertEquals(LayoutDirection.Ltr, enNode.layoutInfo.layoutDirection)
        composeRule.onNodeWithText("English").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("فارسی (Persian)").performScrollTo().assertIsDisplayed()
        assertEquals(0, completedCount)
        assertEquals(0, skippedCount)

        // Switching language to Persian applies immediately with RTL direction
        composeRule.onNodeWithText("فارسی (Persian)").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertEquals("fa", currentLang)
        val faNode = composeRule.onNodeWithText("زبان برنامه را انتخاب کنید").performScrollTo().assertIsDisplayed().fetchSemanticsNode()
        assertEquals(LayoutDirection.Rtl, faNode.layoutInfo.layoutDirection)
        composeRule.onNodeWithText("بعدی").assertIsDisplayed()

        // Switch back to English and step through tutorial (Next & Back) without prematurely completing
        composeRule.onNodeWithText("English").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Next").performClick()
        composeRule.onNodeWithText("Step 1 of 6").assertIsDisplayed()
        assertEquals(0, completedCount)

        composeRule.onNodeWithText("Next").performClick()
        composeRule.onNodeWithText("Step 2 of 6").assertIsDisplayed()
        composeRule.onNodeWithText("Back").performClick()
        composeRule.onNodeWithText("Step 1 of 6").assertIsDisplayed()

        // Navigate to Step 5 (User Manual & Settings location) and Step 6 (Finish)
        repeat(4) { composeRule.onNodeWithText("Next").performClick() }
        composeRule.onNodeWithText("Step 5 of 6").assertIsDisplayed()
        composeRule.onNodeWithText("Find Settings & the User Manual").performScrollTo().assertIsDisplayed()
        assertEquals(0, completedCount)

        composeRule.onNodeWithText("Next").performClick()
        composeRule.onNodeWithText("Step 6 of 6").assertIsDisplayed()
        composeRule.onNodeWithText("Finish").performClick()
        assertEquals(1, completedCount)
        assertEquals(0, skippedCount)
    }

    @Test
    fun `onboarding skip triggers completion and replay mode closes via close button`() {
        var skipped = 0
        composeRule.setContent {
            SwordTheme(themeMode = "dark") {
                OnboardingScreen(
                    languageCode = "en",
                    isReplay = true,
                    onSelectLanguage = {},
                    onFinishOnboarding = {},
                    onSkipOnboarding = { skipped++ }
                )
            }
        }
        composeRule.onNodeWithText("Close").assertIsDisplayed()
        composeRule.onNodeWithText("Skip").performClick()
        assertEquals(1, skipped)
    }

    @Test
    fun `settings screen shows replay tutorial card and invokes callback`() {
        var replayClicked = 0
        composeRule.setContent {
            SwordTheme(themeMode = "dark") {
                SettingsScreen(
                    preferences = UserPreferences(languageCode = "en"),
                    onToggleAnimations = {},
                    onToggleAuthConfirm = {},
                    onTimeoutChange = {},
                    onConcurrencyChange = {},
                    onDohProviderChange = {},
                    onLanguageChange = {},
                    onThemeModeChange = {},
                    onSaveProxy = {},
                    onClearAllData = {},
                    onResetDefaults = {},
                    onOpenUserManual = {},
                    onReplayTutorial = { replayClicked++ },
                    onOpenCapabilities = {},
                    onOpenPermissions = {},
                    onOpenAbout = {}
                )
            }
        }
        composeRule.onNodeWithText("User Manual").assertIsDisplayed()
        composeRule.onNode(hasScrollAction()).performScrollToNode(hasText("Replay Tutorial"))
        composeRule.onNodeWithText("Replay Tutorial").assertIsDisplayed().performClick()
        assertEquals(1, replayClicked)
    }

    @Test
    fun `user preferences repository persists language and onboarding completion safely`() = runTest {
        val context: Context = ApplicationProvider.getApplicationContext()
        val repo = UserPreferencesRepository(context)
        repo.setOnboardingCompleted(false)
        repo.setLanguageCode("fa")
        val afterLangOnly = repo.preferencesFlow.first()
        assertEquals("fa", afterLangOnly.languageCode)
        assertFalse(afterLangOnly.onboardingCompleted)

        repo.setOnboardingCompleted(true)
        val afterFinish = repo.preferencesFlow.first()
        assertTrue(afterFinish.onboardingCompleted)
        assertEquals("fa", afterFinish.languageCode)
    }

    @Test
    fun `report viewer explain tab translates findings switches inline to persian rtl and preserves json`() {
        val meta = ReportMetadataEntity(
            id = 88L,
            scanRecordId = 88L,
            title = "TLS Inspector — example.org",
            toolId = "web_tls_inspector",
            toolName = "TLS Certificate Inspector",
            categoryId = "web",
            targetSummary = "example.org",
            highestSeverity = "High",
            findingsCount = 1,
            jsonFileName = "tls.json",
            htmlFileName = "tls.html",
            createdEpochMs = 1_700_000_000_000L
        )
        val viewerData = ReportViewerData(
            reportMetaId = 88L,
            jsonFileName = "tls.json",
            htmlFileName = "tls.html",
            parsedReport = SavedJsonReport(
                taskId = "t88",
                toolId = "web_tls_inspector",
                toolName = "TLS Certificate Inspector",
                category = "Web Safety",
                target = "example.org",
                timestampIso = "2026-01-01T00:00:00Z",
                durationMs = 45L,
                success = true,
                highestSeverity = "High",
                findings = listOf(
                    SecurityFinding(
                        title = "Legacy TLS Protocol Negotiated",
                        severity = "High",
                        detail = "Negotiated TLSv1.0 on example.org.",
                        recommendation = "Disable TLSv1.0 and require TLSv1.2 or TLSv1.3."
                    )
                ),
                output = "Negotiated TLSv1.0"
            ),
            rawJsonText = """{"toolId":"web_tls_inspector"}""",
            formattedJsonText = "{\n  \"toolId\": \"web_tls_inspector\"\n}",
            jsonError = null,
            htmlText = "<!DOCTYPE html><html><body>TLSv1.0</body></html>",
            htmlError = null
        )

        composeRule.setContent {
            SwordTheme(themeMode = "dark") {
                ReportViewerScreen(
                    reportMeta = meta,
                    viewerData = viewerData,
                    languageCode = "en",
                    onBack = {},
                    onDeleteReport = {},
                    onExportReport = { _, _ -> },
                    getShareUri = { null },
                    onShowMessage = {}
                )
            }
        }

        // Trigger via Summary quick button into Explain tab
        composeRule.onNodeWithText("Explain Results").performScrollTo().performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Server uses an outdated TLS encryption version")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithText("TLSv1.0").performScrollTo().assertIsDisplayed()

        // Inline switch inside Explain tab to Persian and verify RTL
        composeRule.onNodeWithText("فارسی (Persian)").performScrollTo().performClick()
        composeRule.waitForIdle()
        val faTitleNode = composeRule.onNodeWithText("سرور از نسخه قدیمی رمزنگاری TLS استفاده می‌کند")
            .performScrollTo()
            .assertIsDisplayed()
            .fetchSemanticsNode()
        assertEquals(LayoutDirection.Rtl, faTitleNode.layoutInfo.layoutDirection)

        // Original JSON tab remains untouched
        composeRule.onNodeWithText("JSON").performClick()
        composeRule.onNodeWithText("web_tls_inspector", substring = true).assertIsDisplayed()
    }

    @Test
    fun `live execution screen supports toggling offline explanation without losing raw output`() {
        val result = ExecutionResult(
            taskId = "t77",
            toolId = "crypto_hash_gen",
            toolName = "Hash Generator",
            category = ModuleCategory.CRYPTOGRAPHY,
            targetSummary = "12 bytes",
            success = true,
            durationMs = 15L,
            timestampEpochMs = 1_700_000_000_000L,
            rawOutput = "SHA-256 : abc123",
            findings = listOf(
                SecurityFinding(
                    title = "SHA-256 & SHA-512 Digests Computed",
                    severity = "Info",
                    detail = "Generated collision-resistant SHA-256 checksum.",
                    recommendation = "Use SHA-256 or SHA-512."
                )
            ),
            highestSeverity = SeverityLevel.INFO,
            reportId = 5L
        )

        composeRule.setContent {
            SwordTheme(themeMode = "dark") {
                LiveExecutionScreen(
                    progress = ExecutionProgress(
                        taskId = "t77",
                        toolId = "crypto_hash_gen",
                        toolName = "Hash Generator",
                        status = ExecutionStatus.COMPLETED,
                        progressFraction = 1f,
                        stageMessage = "Done",
                        result = result
                    ),
                    languageCode = "en",
                    onBack = {},
                    onCancelTask = {},
                    onOpenReports = {}
                )
            }
        }

        composeRule.onNodeWithText("Explain Results").performScrollTo().assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Offline cryptography utility completed")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithText(SwordStrings.label("hide_explanation", "en"))
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithText("SHA-256 : abc123", substring = true)
            .performScrollTo()
            .assertIsDisplayed()
    }
}
