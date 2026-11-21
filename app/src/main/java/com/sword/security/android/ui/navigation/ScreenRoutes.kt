package com.sword.security.android.ui.navigation

object ScreenRoutes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val ONBOARDING_WITH_REPLAY = "onboarding?replay={replay}"
    const val DASHBOARD = "dashboard"
    const val MODULES = "modules"
    const val MODULES_WITH_CAT = "modules?category={category}"
    const val MODULE_DETAIL = "module_detail/{toolId}"
    const val SCAN_CONFIG = "scan_config/{toolId}"
    const val LIVE_EXECUTION = "live_execution"
    const val SCAN_HISTORY = "scan_history"
    const val REPORTS_CENTER = "reports_center"
    const val REPORT_VIEWER = "report_viewer/{reportId}"
    const val DEVICE_CAPABILITIES = "device_capabilities"
    const val PERMISSIONS_MANAGER = "permissions_manager"
    const val SETTINGS = "settings"
    const val USER_MANUAL = "user_manual"
    const val ABOUT = "about"

    fun onboardingRoute(replay: Boolean = false): String =
        if (replay) "onboarding?replay=true" else "onboarding?replay=false"

    fun modulesRoute(categoryId: String? = null): String =
        if (categoryId.isNullOrBlank()) MODULES else "modules?category=$categoryId"

    fun moduleDetailRoute(toolId: String): String = "module_detail/$toolId"
    fun scanConfigRoute(toolId: String): String = "scan_config/$toolId"
    fun reportViewerRoute(reportId: Long): String = "report_viewer/$reportId"
}
