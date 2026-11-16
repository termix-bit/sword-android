package com.sword.security.android.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sword.security.android.core.model.ModuleCategory
import com.sword.security.android.ui.screens.AboutSwordScreen
import com.sword.security.android.ui.screens.DashboardScreen
import com.sword.security.android.ui.screens.DeviceCapabilitiesScreen
import com.sword.security.android.ui.screens.LiveExecutionScreen
import com.sword.security.android.ui.screens.ModuleDetailScreen
import com.sword.security.android.ui.screens.ModulesExplorerScreen
import com.sword.security.android.ui.screens.OnboardingScreen
import com.sword.security.android.ui.screens.PermissionsManagerScreen
import com.sword.security.android.ui.screens.ReportsCenterScreen
import com.sword.security.android.ui.screens.ScanConfigScreen
import com.sword.security.android.ui.screens.ScanHistoryScreen
import com.sword.security.android.ui.screens.SettingsScreen
import com.sword.security.android.ui.screens.SplashScreen
import com.sword.security.android.ui.theme.SwordBackground
import com.sword.security.android.ui.theme.SwordCardSurface
import com.sword.security.android.ui.theme.SwordDeepNavy
import com.sword.security.android.ui.theme.SwordPrimaryCyan
import com.sword.security.android.ui.theme.SwordTextSecondary
import com.sword.security.android.ui.viewmodel.SwordViewModel

private data class BottomNavTab(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun SwordNavHost(viewModel: SwordViewModel) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val capabilities by viewModel.capabilitiesSummary.collectAsStateWithLifecycle()
    val permissions by viewModel.permissions.collectAsStateWithLifecycle()
    val recentScans by viewModel.recentScans.collectAsStateWithLifecycle()
    val allReports by viewModel.allReports.collectAsStateWithLifecycle()
    val recentActivity by viewModel.recentActivity.collectAsStateWithLifecycle()
    val evidenceRecords by viewModel.evidenceRecords.collectAsStateWithLifecycle()
    val executionProgress by viewModel.executionProgress.collectAsStateWithLifecycle()
    val selectedReportDetail by viewModel.selectedReportContent.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()

    LaunchedEffect(statusBanner) {
        val msg = statusBanner
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.consumeStatusBanner()
        }
    }

    val bottomTabs = remember(prefs.languageCode) {
        listOf(
            BottomNavTab(ScreenRoutes.DASHBOARD, com.sword.security.android.core.i18n.SwordStrings.label("dashboard", prefs.languageCode), Icons.Default.Dashboard),
            BottomNavTab(ScreenRoutes.MODULES, com.sword.security.android.core.i18n.SwordStrings.label("modules", prefs.languageCode), Icons.Default.Apps),
            BottomNavTab(ScreenRoutes.SCAN_HISTORY, com.sword.security.android.core.i18n.SwordStrings.label("history", prefs.languageCode), Icons.Default.History),
            BottomNavTab(ScreenRoutes.REPORTS_CENTER, com.sword.security.android.core.i18n.SwordStrings.label("reports", prefs.languageCode), Icons.Default.Description),
            BottomNavTab(ScreenRoutes.SETTINGS, com.sword.security.android.core.i18n.SwordStrings.label("settings", prefs.languageCode), Icons.Default.Settings)
        )
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route
    val showBottomBar = currentRoute in listOf(
        ScreenRoutes.DASHBOARD,
        ScreenRoutes.MODULES,
        ScreenRoutes.MODULES_WITH_CAT,
        ScreenRoutes.SCAN_HISTORY,
        ScreenRoutes.REPORTS_CENTER,
        ScreenRoutes.SETTINGS
    )

    Scaffold(
        containerColor = SwordBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = SwordDeepNavy) {
                    bottomTabs.forEach { tab ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route == tab.route ||
                                (tab.route == ScreenRoutes.MODULES && it.route == ScreenRoutes.MODULES_WITH_CAT)
                        } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(ScreenRoutes.DASHBOARD) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SwordPrimaryCyan,
                                selectedTextColor = SwordPrimaryCyan,
                                indicatorColor = SwordCardSurface,
                                unselectedIconColor = SwordTextSecondary,
                                unselectedTextColor = SwordTextSecondary
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = ScreenRoutes.SPLASH
            ) {
                composable(ScreenRoutes.SPLASH) {
                    SplashScreen(
                        animationsEnabled = prefs.animationsEnabled,
                        onboardingCompleted = prefs.onboardingCompleted,
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
                        recentActivity = recentActivity,
                        recentReports = allReports,
                        onOpenCategory = { cat ->
                            when (cat) {
                                ModuleCategory.REPORTS -> navController.navigate(ScreenRoutes.REPORTS_CENTER)
                                ModuleCategory.SETTINGS -> navController.navigate(ScreenRoutes.SETTINGS)
                                else -> navController.navigate(ScreenRoutes.modulesRoute(cat.id))
                            }
                        },
                        onOpenToolConfig = { toolId ->
                            navController.navigate(ScreenRoutes.scanConfigRoute(toolId))
                        },
                        onOpenCapabilities = { navController.navigate(ScreenRoutes.DEVICE_CAPABILITIES) },
                        onOpenPermissions = { navController.navigate(ScreenRoutes.PERMISSIONS_MANAGER) },
                        onOpenReports = { navController.navigate(ScreenRoutes.REPORTS_CENTER) },
                        onOpenHistory = { navController.navigate(ScreenRoutes.SCAN_HISTORY) },
                        onOpenSettings = { navController.navigate(ScreenRoutes.SETTINGS) },
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
                        onBack = { navController.popBackStack() },
                        onCancelTask = { viewModel.cancelExecution(it) },
                        onOpenReports = {
                            navController.navigate(ScreenRoutes.REPORTS_CENTER) {
                                popUpTo(navController.graph.findStartDestination().id)
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable(ScreenRoutes.SCAN_HISTORY) {
                    ScanHistoryScreen(
                        scans = recentScans,
                        evidenceRecords = evidenceRecords,
                        onDeleteScan = viewModel::deleteSingleScan,
                        onClearHistory = viewModel::clearAllHistory
                    )
                }

                composable(ScreenRoutes.REPORTS_CENTER) {
                    ReportsCenterScreen(
                        reports = allReports,
                        selectedReportDetail = selectedReportDetail,
                        onOpenReport = viewModel::loadReportDetail,
                        onDismissReportDetail = viewModel::clearSelectedReport,
                        onDeleteReport = viewModel::deleteSingleReport,
                        onDeleteAllReports = viewModel::deleteAllReports,
                        onExportReport = viewModel::exportReportToUri,
                        getShareUri = viewModel::getReportShareUri
                    )
                }

                composable(ScreenRoutes.DEVICE_CAPABILITIES) {
                    DeviceCapabilitiesScreen(
                        summary = capabilities,
                        onRefresh = viewModel::refreshCapabilitiesAndPermissions,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(ScreenRoutes.PERMISSIONS_MANAGER) {
                    PermissionsManagerScreen(
                        permissions = permissions,
                        wifiPermissionsToRequest = viewModel.getWifiPermissionsToRequest(),
                        onPermissionsUpdated = viewModel::refreshCapabilitiesAndPermissions,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(ScreenRoutes.SETTINGS) {
                    SettingsScreen(
                        preferences = prefs,
                        onToggleAnimations = viewModel::setAnimationsEnabled,
                        onToggleAuthConfirm = viewModel::setRequireAuthorizationConfirmation,
                        onTimeoutChange = viewModel::setNetworkTimeout,
                        onConcurrencyChange = viewModel::setMaxConcurrency,
                        onDohProviderChange = viewModel::setDohProvider,
                        onLanguageChange = viewModel::setLanguageCode,
                        onSaveProxy = viewModel::saveProxyUrl,
                        onClearAllData = viewModel::clearAllAppData,
                        onResetDefaults = viewModel::resetSettings,
                        onOpenCapabilities = { navController.navigate(ScreenRoutes.DEVICE_CAPABILITIES) },
                        onOpenPermissions = { navController.navigate(ScreenRoutes.PERMISSIONS_MANAGER) },
                        onOpenAbout = { navController.navigate(ScreenRoutes.ABOUT) }
                    )
                }

                composable(ScreenRoutes.ABOUT) {
                    AboutSwordScreen(
                        pythonVersion = capabilities?.pythonVersion ?: "3.10",
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
