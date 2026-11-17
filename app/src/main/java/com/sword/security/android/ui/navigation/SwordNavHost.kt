package com.sword.security.android.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sword.security.android.core.i18n.SwordStrings
import com.sword.security.android.core.model.ModuleCategory
import com.sword.security.android.ui.components.LocalSwordAnimationsEnabled
import com.sword.security.android.ui.components.rememberEffectiveAnimationsEnabled
import com.sword.security.android.ui.screens.AboutSwordScreen
import com.sword.security.android.ui.screens.DashboardScreen
import com.sword.security.android.ui.screens.DeviceCapabilitiesScreen
import com.sword.security.android.ui.screens.LiveExecutionScreen
import com.sword.security.android.ui.screens.ModuleDetailScreen
import com.sword.security.android.ui.screens.ModulesExplorerScreen
import com.sword.security.android.ui.screens.OnboardingScreen
import com.sword.security.android.ui.screens.PermissionsManagerScreen
import com.sword.security.android.ui.screens.ReportViewerScreen
import com.sword.security.android.ui.screens.ReportsCenterScreen
import com.sword.security.android.ui.screens.ScanConfigScreen
import com.sword.security.android.ui.screens.ScanHistoryScreen
import com.sword.security.android.ui.screens.SettingsScreen
import com.sword.security.android.ui.screens.SplashScreen
import com.sword.security.android.ui.screens.UserManualScreen
import com.sword.security.android.ui.theme.LocalSwordColors
import com.sword.security.android.ui.viewmodel.SwordViewModel

data class BottomNavTab(
    val route: String,
    val label: String,
    val icon: ImageVector
)

object SwordNavigationHelper {

    fun isTabSelected(currentEntry: NavBackStackEntry?, tabRoute: String): Boolean {
        val dest = currentEntry?.destination ?: return false
        return dest.hierarchy.any {
            it.route == tabRoute ||
                (tabRoute == ScreenRoutes.MODULES && it.route == ScreenRoutes.MODULES_WITH_CAT)
        }
    }

    fun navigateToBottomTab(
        navController: NavController,
        tabRoute: String,
        currentEntry: NavBackStackEntry?,
        onReselectScrollToTop: (String) -> Unit
    ) {
        val alreadySelected = isTabSelected(currentEntry, tabRoute)
        if (!alreadySelected) {
            navController.navigate(tabRoute) {
                popUpTo(ScreenRoutes.DASHBOARD) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
            return
        }

        // Re-tapping an already-selected tab:
        // 1. If the Tools tab currently has a category argument (opened from Dashboard category),
        //    reset back to the unfiltered Tools root cleanly without duplicating entries.
        val currentRoute = currentEntry?.destination?.route
        val currentCategory = currentEntry?.arguments?.getString("category")
        if (tabRoute == ScreenRoutes.MODULES &&
            currentRoute == ScreenRoutes.MODULES_WITH_CAT &&
            !currentCategory.isNullOrBlank()
        ) {
            navController.navigate(ScreenRoutes.MODULES) {
                popUpTo(ScreenRoutes.MODULES_WITH_CAT) { inclusive = true }
                launchSingleTop = true
            }
            onReselectScrollToTop(tabRoute)
            return
        }

        // 2. If deeper on a backstack (shouldn't normally show bottom bar, but defensive), pop to tab root.
        val popped = if (currentRoute != tabRoute &&
            !(tabRoute == ScreenRoutes.MODULES && currentRoute == ScreenRoutes.MODULES_WITH_CAT)
        ) {
            navController.popBackStack(tabRoute, inclusive = false)
        } else {
            false
        }

        // 3. Always scroll active tab list/content smoothly to top on re-tap without losing running tasks.
        if (!popped) {
            onReselectScrollToTop(tabRoute)
        }
    }
}

@Composable
fun SwordNavHost(viewModel: SwordViewModel) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val capabilities by viewModel.capabilitiesSummary.collectAsStateWithLifecycle()
    val permissions by viewModel.permissions.collectAsStateWithLifecycle()
    val recentScans by viewModel.recentScans.collectAsStateWithLifecycle()
    val allReports by viewModel.allReports.collectAsStateWithLifecycle()
    val evidenceRecords by viewModel.evidenceRecords.collectAsStateWithLifecycle()
    val executionProgress by viewModel.executionProgress.collectAsStateWithLifecycle()
    val viewerReportMeta by viewModel.viewerReportMeta.collectAsStateWithLifecycle()
    val viewerData by viewModel.selectedReportViewerData.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()

    val effectiveAnimations = rememberEffectiveAnimationsEnabled(prefs.animationsEnabled)
    val layoutDirection = if (SwordStrings.isPersian(prefs.languageCode)) {
        LayoutDirection.Rtl
    } else {
        LayoutDirection.Ltr
    }
    val swordColors = LocalSwordColors.current

    val reselectTicks = remember {
        mutableStateMapOf(
            ScreenRoutes.DASHBOARD to 0,
            ScreenRoutes.MODULES to 0,
            ScreenRoutes.SCAN_HISTORY to 0,
            ScreenRoutes.REPORTS_CENTER to 0,
            ScreenRoutes.SETTINGS to 0
        )
    }
    val modulesResetTick = remember { mutableIntStateOf(0) }

    LaunchedEffect(statusBanner) {
        val msg = statusBanner
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.consumeStatusBanner()
        }
    }

    val bottomTabs = remember(prefs.languageCode) {
        listOf(
            BottomNavTab(ScreenRoutes.DASHBOARD, SwordStrings.label("dashboard", prefs.languageCode), Icons.Default.Dashboard),
            BottomNavTab(ScreenRoutes.MODULES, SwordStrings.label("modules", prefs.languageCode), Icons.Default.Apps),
            BottomNavTab(ScreenRoutes.SCAN_HISTORY, SwordStrings.label("history", prefs.languageCode), Icons.Default.History),
            BottomNavTab(ScreenRoutes.REPORTS_CENTER, SwordStrings.label("reports", prefs.languageCode), Icons.Default.Description),
            BottomNavTab(ScreenRoutes.SETTINGS, SwordStrings.label("settings", prefs.languageCode), Icons.Default.Settings)
        )
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in listOf(
        ScreenRoutes.DASHBOARD,
        ScreenRoutes.MODULES,
        ScreenRoutes.MODULES_WITH_CAT,
        ScreenRoutes.SCAN_HISTORY,
        ScreenRoutes.REPORTS_CENTER,
        ScreenRoutes.SETTINGS
    )

    CompositionLocalProvider(
        LocalLayoutDirection provides layoutDirection,
        LocalSwordAnimationsEnabled provides effectiveAnimations
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(containerColor = swordColors.topBarBackground) {
                        bottomTabs.forEach { tab ->
                            val selected = SwordNavigationHelper.isTabSelected(navBackStackEntry, tab.route)
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    SwordNavigationHelper.navigateToBottomTab(
                                        navController = navController,
                                        tabRoute = tab.route,
                                        currentEntry = navBackStackEntry,
                                        onReselectScrollToTop = { route ->
                                            reselectTicks[route] = (reselectTicks[route] ?: 0) + 1
                                            if (route == ScreenRoutes.MODULES) {
                                                modulesResetTick.intValue++
                                            }
                                        }
                                    )
                                },
                                icon = { Icon(tab.icon, contentDescription = tab.label) },
                                label = {
                                    Text(
                                        text = tab.label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedIconColor = swordColors.textSecondary,
                                    unselectedTextColor = swordColors.textSecondary
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                val animMs = if (effectiveAnimations) 180 else 0
                NavHost(
                    navController = navController,
                    startDestination = ScreenRoutes.SPLASH,
                    enterTransition = { fadeIn(animationSpec = tween(animMs)) },
                    exitTransition = { fadeOut(animationSpec = tween(animMs)) },
                    popEnterTransition = { fadeIn(animationSpec = tween(animMs)) },
                    popExitTransition = { fadeOut(animationSpec = tween(animMs)) }
                ) {
                    composable(ScreenRoutes.SPLASH) {
                        SplashScreen(
                            animationsEnabled = effectiveAnimations,
                            onboardingCompleted = prefs.onboardingCompleted,
                            languageCode = prefs.languageCode,
                            onNavigateToOnboarding = {
                                navController.navigate(ScreenRoutes.ONBOARDING) {
                                    popUpTo(ScreenRoutes.SPLASH) { inclusive = true }
                                }
                            },
                            onNavigateToDashboard = {
                                navController.navigate(ScreenRoutes.DASHBOARD) {
                                    popUpTo(ScreenRoutes.SPLASH) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(ScreenRoutes.ONBOARDING) {
                        OnboardingScreen(
                            languageCode = prefs.languageCode,
                            onGetStarted = {
                                viewModel.completeOnboarding()
                                navController.navigate(ScreenRoutes.DASHBOARD) {
                                    popUpTo(ScreenRoutes.ONBOARDING) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(ScreenRoutes.DASHBOARD) {
                        DashboardScreen(
                            capabilities = capabilities,
                            permissions = permissions,
                            recentReports = allReports,
                            languageCode = prefs.languageCode,
                            scrollToTopTrigger = reselectTicks[ScreenRoutes.DASHBOARD] ?: 0,
                            onExploreTools = {
                                SwordNavigationHelper.navigateToBottomTab(
                                    navController = navController,
                                    tabRoute = ScreenRoutes.MODULES,
                                    currentEntry = navBackStackEntry,
                                    onReselectScrollToTop = {}
                                )
                            },
                            onOpenCategory = { cat ->
                                when (cat) {
                                    ModuleCategory.REPORTS -> SwordNavigationHelper.navigateToBottomTab(
                                        navController, ScreenRoutes.REPORTS_CENTER, navBackStackEntry
                                    ) {}
                                    ModuleCategory.SETTINGS -> SwordNavigationHelper.navigateToBottomTab(
                                        navController, ScreenRoutes.SETTINGS, navBackStackEntry
                                    ) {}
                                    else -> navController.navigate(ScreenRoutes.modulesRoute(cat.id)) {
                                        popUpTo(ScreenRoutes.DASHBOARD) { saveState = true }
                                        launchSingleTop = true
                                    }
                                }
                            },
                            onOpenToolConfig = { toolId ->
                                navController.navigate(ScreenRoutes.scanConfigRoute(toolId))
                            },
                            onOpenCapabilities = { navController.navigate(ScreenRoutes.DEVICE_CAPABILITIES) },
                            onOpenPermissions = { navController.navigate(ScreenRoutes.PERMISSIONS_MANAGER) },
                            onOpenReports = {
                                SwordNavigationHelper.navigateToBottomTab(
                                    navController, ScreenRoutes.REPORTS_CENTER, navBackStackEntry
                                ) {}
                            },
                            onOpenReportViewer = { report ->
                                viewModel.loadReportForViewer(report)
                                navController.navigate(ScreenRoutes.reportViewerRoute(report.id))
                            },
                            onOpenSettings = {
                                SwordNavigationHelper.navigateToBottomTab(
                                    navController, ScreenRoutes.SETTINGS, navBackStackEntry
                                ) {}
                            },
                            onOpenAbout = { navController.navigate(ScreenRoutes.ABOUT) }
                        )
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
                    ) { backStackEntry ->
                        val catId = backStackEntry.arguments?.getString("category")
                        val initialCat = ModuleCategory.entries.firstOrNull { it.id == catId }
                        ModulesExplorerScreen(
                            tools = viewModel.getTools(),
                            initialCategory = initialCat,
                            languageCode = prefs.languageCode,
                            scrollToTopTrigger = reselectTicks[ScreenRoutes.MODULES] ?: 0,
                            resetFilterTrigger = modulesResetTick.intValue,
                            resolveCapability = viewModel::getToolCapability,
                            onSelectTool = { tool ->
                                navController.navigate(ScreenRoutes.moduleDetailRoute(tool.id))
                            }
                        )
                    }

                    composable(
                        route = ScreenRoutes.MODULE_DETAIL,
                        arguments = listOf(navArgument("toolId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val toolId = backStackEntry.arguments?.getString("toolId").orEmpty()
                        val tool = viewModel.findTool(toolId)
                        if (tool != null) {
                            ModuleDetailScreen(
                                tool = tool,
                                capabilityState = viewModel.getToolCapability(tool),
                                languageCode = prefs.languageCode,
                                onBack = { navController.popBackStack() },
                                onConfigureAndRun = {
                                    navController.navigate(ScreenRoutes.scanConfigRoute(tool.id))
                                },
                                onOpenPermissions = {
                                    navController.navigate(ScreenRoutes.PERMISSIONS_MANAGER)
                                }
                            )
                        }
                    }

                    composable(
                        route = ScreenRoutes.SCAN_CONFIG,
                        arguments = listOf(navArgument("toolId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val toolId = backStackEntry.arguments?.getString("toolId").orEmpty()
                        val tool = viewModel.findTool(toolId)
                        if (tool != null) {
                            ScanConfigScreen(
                                tool = tool,
                                requireAuthConfirmation = prefs.requireAuthorizationConfirmation,
                                languageCode = prefs.languageCode,
                                onBack = { navController.popBackStack() },
                                onExecute = { params ->
                                    viewModel.runTool(tool, params)
                                    navController.navigate(ScreenRoutes.LIVE_EXECUTION)
                                }
                            )
                        }
                    }

                    composable(ScreenRoutes.LIVE_EXECUTION) {
                        LiveExecutionScreen(
                            progress = executionProgress,
                            languageCode = prefs.languageCode,
                            onBack = { navController.popBackStack() },
                            onCancelTask = { viewModel.cancelExecution(it) },
                            onOpenReports = {
                                val reportId = executionProgress.result?.reportId
                                if (reportId != null) {
                                    viewModel.loadReportForViewerById(reportId)
                                    navController.navigate(ScreenRoutes.reportViewerRoute(reportId)) {
                                        popUpTo(ScreenRoutes.DASHBOARD)
                                    }
                                } else {
                                    navController.navigate(ScreenRoutes.REPORTS_CENTER) {
                                        popUpTo(ScreenRoutes.DASHBOARD) { saveState = true }
                                        launchSingleTop = true
                                    }
                                }
                            }
                        )
                    }

                    composable(ScreenRoutes.SCAN_HISTORY) {
                        ScanHistoryScreen(
                            scans = recentScans,
                            evidenceRecords = evidenceRecords,
                            languageCode = prefs.languageCode,
                            scrollToTopTrigger = reselectTicks[ScreenRoutes.SCAN_HISTORY] ?: 0,
                            onDeleteScan = viewModel::deleteSingleScan,
                            onClearHistory = viewModel::clearAllHistory
                        )
                    }

                    composable(ScreenRoutes.REPORTS_CENTER) {
                        ReportsCenterScreen(
                            reports = allReports,
                            languageCode = prefs.languageCode,
                            scrollToTopTrigger = reselectTicks[ScreenRoutes.REPORTS_CENTER] ?: 0,
                            onOpenReport = { report ->
                                viewModel.loadReportForViewer(report)
                                navController.navigate(ScreenRoutes.reportViewerRoute(report.id))
                            },
                            onDeleteReport = viewModel::deleteSingleReport,
                            onDeleteAllReports = viewModel::deleteAllReports
                        )
                    }

                    composable(
                        route = ScreenRoutes.REPORT_VIEWER,
                        arguments = listOf(navArgument("reportId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val reportId = backStackEntry.arguments?.getLong("reportId") ?: -1L
                        LaunchedEffect(reportId) {
                            if (reportId > 0L && viewerReportMeta?.id != reportId) {
                                viewModel.loadReportForViewerById(reportId)
                            }
                        }
                        ReportViewerScreen(
                            reportMeta = viewerReportMeta,
                            viewerData = viewerData,
                            languageCode = prefs.languageCode,
                            onBack = {
                                viewModel.clearSelectedReport()
                                navController.popBackStack()
                            },
                            onDeleteReport = viewModel::deleteSingleReport,
                            onExportReport = viewModel::exportReportToUri,
                            getShareUri = viewModel::getReportShareUri,
                            onShowMessage = viewModel::showStatusMessage
                        )
                    }

                    composable(ScreenRoutes.DEVICE_CAPABILITIES) {
                        DeviceCapabilitiesScreen(
                            summary = capabilities,
                            languageCode = prefs.languageCode,
                            onRefresh = viewModel::refreshCapabilitiesAndPermissions,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(ScreenRoutes.PERMISSIONS_MANAGER) {
                        PermissionsManagerScreen(
                            permissions = permissions,
                            wifiPermissionsToRequest = viewModel.getWifiPermissionsToRequest(),
                            languageCode = prefs.languageCode,
                            onPermissionsUpdated = viewModel::refreshCapabilitiesAndPermissions,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(ScreenRoutes.SETTINGS) {
                        SettingsScreen(
                            preferences = prefs,
                            scrollToTopTrigger = reselectTicks[ScreenRoutes.SETTINGS] ?: 0,
                            onToggleAnimations = viewModel::setAnimationsEnabled,
                            onToggleAuthConfirm = viewModel::setRequireAuthorizationConfirmation,
                            onTimeoutChange = viewModel::setNetworkTimeout,
                            onConcurrencyChange = viewModel::setMaxConcurrency,
                            onDohProviderChange = viewModel::setDohProvider,
                            onLanguageChange = viewModel::setLanguageCode,
                            onThemeModeChange = viewModel::setThemeMode,
                            onSaveProxy = viewModel::saveProxyUrl,
                            onClearAllData = viewModel::clearAllAppData,
                            onResetDefaults = viewModel::resetSettings,
                            onOpenUserManual = { navController.navigate(ScreenRoutes.USER_MANUAL) },
                            onOpenCapabilities = { navController.navigate(ScreenRoutes.DEVICE_CAPABILITIES) },
                            onOpenPermissions = { navController.navigate(ScreenRoutes.PERMISSIONS_MANAGER) },
                            onOpenAbout = { navController.navigate(ScreenRoutes.ABOUT) }
                        )
                    }

                    composable(ScreenRoutes.USER_MANUAL) {
                        UserManualScreen(
                            languageCode = prefs.languageCode,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(ScreenRoutes.ABOUT) {
                        AboutSwordScreen(
                            pythonVersion = capabilities?.pythonVersion ?: "3.10",
                            languageCode = prefs.languageCode,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
