package com.sword.security.android.core.i18n

object SwordStrings {
    fun label(key: String, languageCode: String): String {
        val isFa = languageCode == "fa"
        return when (key) {
            "dashboard" -> if (isFa) "داشبورد" else "Dashboard"
            "modules" -> if (isFa) "ماژول‌ها" else "Modules"
            "history" -> if (isFa) "تاریخچه" else "History"
            "reports" -> if (isFa) "گزارش‌ها" else "Reports"
            "settings" -> if (isFa) "تنظیمات" else "Settings"
            "quick_actions" -> if (isFa) "اقدامات سریع" else "Quick Actions"
            "capabilities" -> if (isFa) "قابلیت‌های دستگاه" else "Capabilities"
            "permissions" -> if (isFa) "دسترسی‌ها" else "Permissions"
            else -> key
        }
    }
}
