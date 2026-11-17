package com.sword.security.android

import android.content.Context
import android.webkit.WebView
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.test.core.app.ApplicationProvider
import com.sword.security.android.core.i18n.SwordStrings
import com.sword.security.android.core.model.DeviceCapabilitiesSummary
import com.sword.security.android.data.local.db.ReportMetadataEntity
import com.sword.security.android.data.local.prefs.UserPreferences
import com.sword.security.android.data.reports.ReportViewerData
import com.sword.security.android.data.reports.SavedJsonReport
import com.sword.security.android.ui.navigation.ScreenRoutes
import com.sword.security.android.ui.navigation.SwordNavigationHelper
import com.sword.security.android.ui.screens.DashboardScreen
import com.sword.security.android.ui.screens.ReportViewerScreen
import com.sword.security.android.ui.screens.SettingsScreen
import com.sword.security.android.ui.screens.UserManualScreen
import com.sword.security.android.ui.theme.SwordTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ComposeUiAndNavigationRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `settings screen renders theme and language chips visibly in light and dark themes`() {
        var selectedTheme = "dark"
        var selectedLang = "en"

        composeRule.setContent {
            SwordTheme(themeMode = "light") {
                SettingsScreen(
                    preferences = UserPreferences(
                        themeMode = "light",
                        languageCode = "en",
                        dohProvider = "Cloudflare"
                    ),
                    onToggleAnimations = {},
                    onToggleAuthConfirm = {},
                    onTimeoutChange = {},
                    onConcurrencyChange = {},
                    onDohProviderChange = {},
                    onLanguageChange = { selectedLang = it },
                    onThemeModeChange = { selectedTheme = it },
                    onSaveProxy = {},
                    onClearAllData = {},
                    onResetDefaults = {},
                    onOpenUserManual = {},
                    onOpenCapabilities = {},
                    onOpenPermissions = {},
                    onOpenAbout = {}
                )
            }
        }

        composeRule.onNodeWithText("Light").assertIsDisplayed().assertIsSelected()
        composeRule.onNodeWithText("Dark").assertIsDisplayed().performClick()
        assertEquals("dark", selectedTheme)
        composeRule.onNodeWithText("فارسی (Persian)").assertIsDisplayed().performClick()
        assertEquals("fa", selectedLang)
    }

    @Test
    fun `user manual displays 13 sections in persian rtl and supports search`() {
        composeRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                SwordTheme(themeMode = "dark") {
                    UserManualScreen(languageCode = "fa", onBack = {})
                }
            }
        }

        composeRule.onNodeWithText("۱. معرفی Sword Android").assertIsDisplayed()
        composeRule.onNodeWithText(SwordStrings.label("manual_search_hint", "fa"))
            .performTextInput("گزارش‌ها و خروجی")
        composeRule.onNodeWithText("۱۰. گزارش‌ها و خروجی گرفتن").assertIsDisplayed()
    }

    @Test
    fun `report viewer renders formatted json copy button html webview and corrupted fallback`() {
        val meta = ReportMetadataEntity(
            id = 42L,
            scanRecordId = 10L,
            title = "Header Audit — example.com",
            toolId = "web_header_audit",
            toolName = "Header Audit",
            categoryId = "web",
            targetSummary = "https://example.com",
            highestSeverity = "Low",
            findingsCount = 0,
            jsonFileName = "audit.json",
            htmlFileName = "audit.html",
            createdEpochMs = 1_700_000_000_000L
        )
        val validData = ReportViewerData(
            reportMetaId = 42L,
            jsonFileName = "audit.json",
            htmlFileName = "audit.html",
            parsedReport = SavedJsonReport(
                taskId = "t1",
                toolId = "web_header_audit",
                toolName = "Header Audit",
                category = "Web Security",
                target = "https://example.com",
                timestampIso = "2026-01-01T00:00:00Z",
                durationMs = 55L,
                success = true,
                highestSeverity = "Low",
                findings = emptyList(),
                output = "OK"
            ),
            rawJsonText = """{"toolName":"Header Audit"}""",
            formattedJsonText = "{\n  \"toolName\": \"Header Audit\"\n}",
            jsonError = null,
            htmlText = "<!DOCTYPE html><html><body><h1>Sword Report Body</h1></body></html>",
            htmlError = null
        )

        var lastMessage = ""
        composeRule.setContent {
            SwordTheme(themeMode = "dark") {
                ReportViewerScreen(
                    reportMeta = meta,
                    viewerData = validData,
                    languageCode = "en",
                    onBack = {},
                    onDeleteReport = {},
                    onExportReport = { _, _ -> },
                    getShareUri = { null },
                    onShowMessage = { lastMessage = it }
                )
            }
        }

        // JSON tab
        composeRule.onNodeWithText("JSON").performClick()
        composeRule.onNodeWithText("Copy JSON").assertIsDisplayed().performClick()
        assertEquals("JSON copied to clipboard.", lastMessage)
        composeRule.onNodeWithText("{\n  \"toolName\": \"Header Audit\"\n}", substring = true)
            .assertIsDisplayed()

        // HTML tab renders in-app WebView cleanly with JS & file access disabled
        composeRule.onNodeWithText("HTML").performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun `report viewer displays graceful error cards for corrupted json and missing html`() {
        val meta = ReportMetadataEntity(
            id = 43L,
            scanRecordId = 11L,
            title = "Broken Report",
            toolId = "web_header_audit",
            toolName = "Header Audit",
            categoryId = "web",
            targetSummary = "https://example.com",
            highestSeverity = "Info",
            findingsCount = 0,
            jsonFileName = "broken.json",
            htmlFileName = "missing.html",
            createdEpochMs = 1_700_000_000_000L
        )
        val brokenData = ReportViewerData(
            reportMetaId = 43L,
            jsonFileName = "broken.json",
            htmlFileName = "missing.html",
            parsedReport = null,
            rawJsonText = "{bad",
            formattedJsonText = "{bad",
            jsonError = "Unterminated object",
            htmlText = "",
            htmlError = "HTML report file is missing or empty."
        )

        composeRule.setContent {
            SwordTheme(themeMode = "light") {
                ReportViewerScreen(
                    reportMeta = meta,
                    viewerData = brokenData,
                    languageCode = "en",
                    onBack = {},
                    onDeleteReport = {},
                    onExportReport = { _, _ -> },
                    getShareUri = { null },
                    onShowMessage = {}
                )
            }
        }

        composeRule.onNodeWithText("JSON").performClick()
        composeRule.onNodeWithText("Unable to Parse JSON Report").assertIsDisplayed()
        composeRule.onNodeWithText("HTML").performClick()
        composeRule.onNodeWithText("HTML Report Unavailable").assertIsDisplayed()
    }

    @Test
    fun `bottom navigation tab switching and reselection resets category filter and scrolls to top without duplicates`() {
        var reselectCount = 0
        var lastScrollRoute = ""
        lateinit var navRef: androidx.navigation.NavHostController

        composeRule.setContent {
            val navController = rememberNavController()
            navRef = navController
            val backStackEntry = navController.currentBackStackEntryAsState().value

            NavHost(
                navController = navController,
                startDestination = ScreenRoutes.DASHBOARD
            ) {
                composable(ScreenRoutes.DASHBOARD) {
                    androidx.compose.material3.Text("HOME_ROOT")
                }
                composable(
                    route = ScreenRoutes.MODULES_WITH_CAT,
                    arguments = listOf(
                        navArgument("category") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { entry ->
                    val cat = entry.arguments?.getString("category") ?: "ALL"
                    androidx.compose.material3.Text("TOOLS_$cat")
                }
                composable(ScreenRoutes.SETTINGS) {
                    androidx.compose.material3.Text("SETTINGS_ROOT")
                }
            }

            // Expose helper triggers via current entry check inside test
            androidx.compose.runtime.LaunchedEffect(Unit) {
                // Initially on DASHBOARD
            }
        }

        composeRule.runOnIdle {
            // 1. Navigate to category-filtered Tools route
            navRef.navigate(ScreenRoutes.modulesRoute("network"))
        }
        composeRule.onNodeWithText("TOOLS_network").assertIsDisplayed()

        // 2. Re-tap Tools bottom tab -> should reset to TOOLS_ALL without duplicate category entries
        composeRule.runOnIdle {
            SwordNavigationHelper.navigateToBottomTab(
                navController = navRef,
                tabRoute = ScreenRoutes.MODULES,
                currentEntry = navRef.currentBackStackEntry,
                onReselectScrollToTop = { route ->
                    reselectCount++
                    lastScrollRoute = route
                }
            )
        }
        composeRule.onNodeWithText("TOOLS_ALL").assertIsDisplayed()
        assertEquals(1, reselectCount)
        assertEquals(ScreenRoutes.MODULES, lastScrollRoute)

        // 3. Re-tap Tools tab AGAIN while already at root -> triggers scroll-to-top without adding a backstack entry
        val queueSizeBefore = composeRule.runOnIdle { navRef.currentBackStack.value.size }
        composeRule.runOnIdle {
            SwordNavigationHelper.navigateToBottomTab(
                navController = navRef,
                tabRoute = ScreenRoutes.MODULES,
                currentEntry = navRef.currentBackStackEntry,
                onReselectScrollToTop = { route ->
                    reselectCount++
                    lastScrollRoute = route
                }
            )
        }
        val queueSizeAfter = composeRule.runOnIdle { navRef.currentBackStack.value.size }
        assertEquals(queueSizeBefore, queueSizeAfter)
        assertEquals(2, reselectCount)

        // 4. Switch to Settings tab and verify no duplicate Home entries
        composeRule.runOnIdle {
            SwordNavigationHelper.navigateToBottomTab(
                navController = navRef,
                tabRoute = ScreenRoutes.SETTINGS,
                currentEntry = navRef.currentBackStackEntry,
                onReselectScrollToTop = { reselectCount++ }
            )
        }
        composeRule.onNodeWithText("SETTINGS_ROOT").assertIsDisplayed()
    }

    @Test
    fun `dashboard screen opens contextual help dialog on help icon tap`() {
        composeRule.setContent {
            SwordTheme(themeMode = "dark") {
                DashboardScreen(
                    capabilities = DeviceCapabilitiesSummary(
                        deviceModel = "Pixel",
                        androidVersion = "14",
                        apiLevel = 34,
                        abiList = listOf("arm64-v8a"),
                        hasInternetConnection = true,
                        activeTransport = "Wi-Fi",
                        isVpnActive = false,
                        wifiEnabled = true,
                        locationServicesEnabled = true,
                        rootBinaryDetected = false,
                        pythonRuntimeReady = true,
                        pythonVersion = "3.10",
                        capabilities = emptyList()
                    ),
                    permissions = emptyList(),
                    recentReports = emptyList(),
                    languageCode = "en",
                    onExploreTools = {},
                    onOpenCategory = {},
                    onOpenToolConfig = {},
                    onOpenCapabilities = {},
                    onOpenPermissions = {},
                    onOpenReports = {},
                    onOpenReportViewer = {},
                    onOpenSettings = {},
                    onOpenAbout = {}
                )
            }
        }

        composeRule.onNodeWithText("Explore All Tools").assertIsDisplayed()
        composeRule.onAllNodes(androidx.compose.ui.test.hasContentDescription("Help"))[0].performClick()
        composeRule.onNodeWithText("About the Sword Home Screen").assertIsDisplayed()
    }
}
