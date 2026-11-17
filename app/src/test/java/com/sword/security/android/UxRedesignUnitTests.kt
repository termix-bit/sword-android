package com.sword.security.android

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.test.core.app.ApplicationProvider
import com.sword.security.android.core.i18n.SwordHelpContent
import com.sword.security.android.core.i18n.SwordManualContent
import com.sword.security.android.core.i18n.SwordStrings
import com.sword.security.android.core.model.ExecutionResult
import com.sword.security.android.core.model.ModuleCategory
import com.sword.security.android.core.model.SecurityFinding
import com.sword.security.android.core.model.SeverityLevel
import com.sword.security.android.data.reports.ReportStorageManager
import com.sword.security.android.ui.components.JsonHighlightPalette
import com.sword.security.android.ui.components.JsonSyntaxHighlighter
import com.sword.security.android.ui.theme.SwordDarkColorScheme
import com.sword.security.android.ui.theme.SwordLightColorScheme
import com.sword.security.android.ui.theme.SwordTypography
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import kotlin.math.max
import kotlin.math.min

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UxRedesignUnitTests {

    private lateinit var context: Context
    private lateinit var reportStorage: ReportStorageManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        reportStorage = ReportStorageManager(context)
    }

    private fun contrastRatio(fg: Color, bg: Color): Double {
        val l1 = fg.luminance().toDouble()
        val l2 = bg.luminance().toDouble()
        val lighter = max(l1, l2)
        val darker = min(l1, l2)
        return (lighter + 0.05) / (darker + 0.05)
    }

    @Test
    fun `typography text styles do not hardcode colors that break selection controls`() {
        val styles = listOf(
            SwordTypography.displaySmall,
            SwordTypography.headlineMedium,
            SwordTypography.titleLarge,
            SwordTypography.titleMedium,
            SwordTypography.bodyLarge,
            SwordTypography.bodyMedium,
            SwordTypography.labelLarge,
            SwordTypography.labelSmall
        )
        styles.forEach { style ->
            assertEquals(Color.Unspecified, style.color)
        }
    }

    @Test
    fun `dark and light color schemes satisfy readable wcag contrast ratios`() {
        listOf(SwordDarkColorScheme, SwordLightColorScheme).forEach { scheme ->
            assertTrue(contrastRatio(scheme.onBackground, scheme.background) >= 4.5)
            assertTrue(contrastRatio(scheme.onSurface, scheme.surface) >= 4.5)
            assertTrue(contrastRatio(scheme.onSurfaceVariant, scheme.surface) >= 4.5)
            assertTrue(contrastRatio(scheme.onPrimary, scheme.primary) >= 4.5)
            assertTrue(contrastRatio(scheme.onPrimaryContainer, scheme.primaryContainer) >= 4.5)
        }
    }

    @Test
    fun `bilingual strings help topics and 13 section user manual are complete in english and persian`() {
        listOf("en", "fa").forEach { lang ->
            assertTrue(SwordStrings.label("dashboard", lang).isNotBlank())
            assertTrue(SwordStrings.label("settings_title", lang).isNotBlank())
            assertTrue(SwordStrings.label("user_manual", lang).isNotBlank())

            ModuleCategory.entries.forEach { cat ->
                assertTrue(SwordStrings.categoryTitle(cat, lang).isNotBlank())
                assertTrue(SwordStrings.categorySubtitle(cat, lang).isNotBlank())
                if (cat.isToolCategory) {
                    val topic = SwordHelpContent.forCategory(cat, lang)
                    assertTrue(topic.whatItDoes.isNotBlank())
                    assertTrue(topic.whenToUse.isNotBlank())
                    assertTrue(topic.prerequisites.isNotBlank())
                    assertTrue(topic.securityNote.isNotBlank())
                }
            }

            val sections = SwordManualContent.sections(lang)
            assertEquals(13, sections.size)
            assertEquals((1..13).toList(), sections.map { it.number })
            sections.forEach { sec ->
                assertTrue(sec.title.isNotBlank())
                assertTrue(sec.summary.isNotBlank())
                assertTrue(sec.bullets.isNotEmpty())
            }
        }
    }

    @Test
    fun `json syntax highlighter styles keys strings numbers booleans and nulls accurately`() {
        val palette = JsonHighlightPalette(
            keyColor = Color.Cyan,
            stringColor = Color.Green,
            numberColor = Color.Yellow,
            booleanColor = Color.Magenta,
            punctuationColor = Color.Gray,
            fallbackColor = Color.White
        )
        val input = """{"tool":"Sword","count":42,"ok":true,"extra":null}"""
        val annotated = JsonSyntaxHighlighter.highlight(input, palette)
        assertEquals(input, annotated.text)
        assertTrue(annotated.spanStyles.any { it.item.color == Color.Cyan })
        assertTrue(annotated.spanStyles.any { it.item.color == Color.Green })
        assertTrue(annotated.spanStyles.any { it.item.color == Color.Yellow })
        assertTrue(annotated.spanStyles.any { it.item.color == Color.Magenta })
    }

    @Test
    fun `report viewer loader formats valid json html and handles corrupted or missing files safely`() = runTest {
        val result = ExecutionResult(
            taskId = "t999",
            toolId = "web_header_audit",
            toolName = "Header Audit",
            category = ModuleCategory.WEB_SECURITY,
            targetSummary = "https://example.org",
            success = true,
            durationMs = 88L,
            timestampEpochMs = 1_700_000_000_000L,
            rawOutput = "Status: 200 OK",
            findings = listOf(
                SecurityFinding("Sample", "Low", "Detail text", "Fix recommendation")
            ),
            highestSeverity = SeverityLevel.LOW
        )
        val (jsonName, htmlName) = reportStorage.saveReportFiles(result)
        val validData = reportStorage.loadReportViewerData(1L, jsonName, htmlName)
        assertNull(validData.jsonError)
        assertNull(validData.htmlError)
        assertNotNull(validData.parsedReport)
        assertTrue(validData.formattedJsonText.contains("\n  \"toolName\": \"Header Audit\""))
        assertTrue(validData.htmlText.contains("SWORD // ANDROID SECURITY TOOLKIT"))

        // Corrupt JSON file and verify graceful error without exception
        val reportsDir = File(context.filesDir, "reports")
        val corruptJson = File(reportsDir, "corrupt_test.json").apply {
            writeText("{not valid json:::")
        }
        val corruptData = reportStorage.loadReportViewerData(2L, corruptJson.name, "missing_report.html")
        assertNotNull(corruptData.jsonError)
        assertNull(corruptData.parsedReport)
        assertEquals("{not valid json:::", corruptData.rawJsonText)
        assertNotNull(corruptData.htmlError)
        assertEquals("", corruptData.htmlText)

        reportStorage.deleteReportFiles(jsonName, htmlName)
        corruptJson.delete()
    }
}
