package com.sword.security.android.core.i18n

import com.sword.security.android.core.model.CapabilityState
import com.sword.security.android.core.model.ModuleCategory

object SwordStrings {

    fun isPersian(languageCode: String): Boolean = languageCode == "fa"

    fun label(key: String, languageCode: String): String {
        val isFa = isPersian(languageCode)
        return when (key) {
            // Navigation & Common
            "dashboard" -> if (isFa) "خانه" else "Home"
            "modules" -> if (isFa) "ابزارها" else "Tools"
            "history" -> if (isFa) "تاریخچه" else "History"
            "reports" -> if (isFa) "گزارش‌ها" else "Reports"
            "settings" -> if (isFa) "تنظیمات" else "Settings"
            "back" -> if (isFa) "بازگشت" else "Back"
            "close" -> if (isFa) "بستن" else "Close"
            "cancel" -> if (isFa) "انصراف" else "Cancel"
            "confirm" -> if (isFa) "تأیید" else "Confirm"
            "delete" -> if (isFa) "حذف" else "Delete"
            "help" -> if (isFa) "راهنما" else "Help"
            "refresh" -> if (isFa) "به‌روزرسانی" else "Refresh"
            "all" -> if (isFa) "همه" else "All"
            "inspect" -> if (isFa) "بررسی" else "Inspect"
            "save" -> if (isFa) "ذخیره" else "Save"
            "copy" -> if (isFa) "کپی" else "Copy"
            "share" -> if (isFa) "اشتراک‌گذاری" else "Share"
            "export" -> if (isFa) "ذخیره فایل" else "Export"
            "view" -> if (isFa) "مشاهده" else "View"

            // Home / Dashboard
            "app_name" -> "Sword"
            "app_subtitle" -> if (isFa) "دستیار امنیت و بررسی دستگاه اندروید" else "Android Security & Safety Toolkit"
            "home_hero_desc" -> if (isFa) {
                "وضعیت شبکه، وب‌سایت‌ها، وای‌فای و فایل‌های خود را به‌صورت امن و بدون نیاز به روت بررسی کنید."
            } else {
                "Check your network, websites, Wi-Fi, and files safely—no root or technical setup required."
            }
            "explore_all_tools" -> if (isFa) "مشاهده همه ابزارها" else "Explore All Tools"
            "quick_actions" -> if (isFa) "شروع سریع" else "Quick Checks"
            "quick_website_safety" -> if (isFa) "بررسی امنیت سایت" else "Check Website"
            "quick_tls_check" -> if (isFa) "بررسی گواهی SSL" else "Check Certificate"
            "quick_dns_lookup" -> if (isFa) "استعلام دامنه (DNS)" else "Domain Lookup"
            "quick_hash_gen" -> if (isFa) "ساخت اثر انگشت فایل/متن" else "Create Hash"
            "quick_file_check" -> if (isFa) "بررسی فایل" else "Inspect File"
            "device_readiness" -> if (isFa) "آمادگی دستگاه" else "Device Readiness"
            "network_connected" -> if (isFa) "متصل به شبکه" else "Network Connected"
            "network_offline" -> if (isFa) "بدون اتصال اینترنت" else "Offline Mode"
            "permissions_summary" -> if (isFa) "مجوزها" else "Permissions"
            "capabilities" -> if (isFa) "قابلیت‌های دستگاه" else "Capabilities"
            "permissions" -> if (isFa) "مجوزها" else "Permissions"
            "security_categories" -> if (isFa) "دسته‌بندی ابزارها" else "Tool Categories"
            "recent_reports" -> if (isFa) "آخرین گزارش‌ها" else "Recent Reports"
            "all_reports" -> if (isFa) "همه گزارش‌ها" else "All Reports"
            "no_reports_home" -> if (isFa) {
                "هنوز گزارشی ساخته نشده است. با انتخاب یکی از بررسی‌های سریع بالا شروع کنید."
            } else {
                "No reports yet. Tap any quick check above to run your first security check."
            }
            "about_sword" -> if (isFa) "درباره Sword" else "About Sword"
            "user_manual" -> if (isFa) "راهنمای کامل کاربر" else "User Manual"
            "user_manual_sub" -> if (isFa) "آموزش گام‌به‌گام ابزارها و تنظیمات به زبان ساده" else "Step-by-step guide in plain language"

            // Modules Explorer & Detail
            "modules_title" -> if (isFa) "ابزارهای امنیتی" else "Security Tools"
            "modules_search_hint" -> if (isFa) "جستجوی ابزار، وب‌سایت، وای‌فای، رمزنگاری..." else "Search tools, websites, Wi-Fi, files..."
            "no_matching_tools" -> if (isFa) "ابزاری با این عبارت پیدا نشد." else "No tools match your search."
            "clear_filters" -> if (isFa) "پاک کردن فیلترها" else "Clear Filters"
            "device_capability" -> if (isFa) "سازگاری با دستگاه" else "Device Compatibility"
            "module_overview" -> if (isFa) "این ابزار چه کاری انجام می‌دهد؟" else "What This Tool Does"
            "required_inputs" -> if (isFa) "اطلاعات مورد نیاز" else "Inputs Needed"
            "technical_notes" -> if (isFa) "جزئیات فنی و سازگاری" else "Technical Compatibility Notes"
            "configure_and_run" -> if (isFa) "تنظیم و اجرا" else "Configure & Run"
            "unavailable_device" -> if (isFa) "در این دستگاه در دسترس نیست" else "Unavailable on This Device"
            "open_permissions" -> if (isFa) "مدیریت مجوزها" else "Open Permissions Manager"

            // Scan Config & Execution
            "scan_config_title" -> if (isFa) "تنظیم اجرا" else "Configure Check"
            "no_input_needed" -> if (isFa) {
                "این بررسی بدون نیاز به ورودی دستی، مستقیماً وضعیت دستگاه شما را تحلیل می‌کند."
            } else {
                "This check needs no manual input and runs directly on your device."
            }
            "pick_file_saf" -> if (isFa) "انتخاب فایل از حافظه دستگاه" else "Choose File"
            "no_file_selected" -> if (isFa) "هنوز فایلی انتخاب نشده است" else "No file selected"
            "start_execution" -> if (isFa) "شروع بررسی" else "Run Check"
            "auth_dialog_title" -> if (isFa) "تأیید مالکیت یا مجوز بررسی" else "Confirm Authorization"
            "auth_dialog_body" -> if (isFa) {
                "تأیید کنید که مالک این هدف هستید یا مجوز صریح برای بررسی آن دارید:"
            } else {
                "Confirm that you own or have explicit permission to inspect:"
            }
            "auth_dialog_confirm" -> if (isFa) "بله، مجاز هستم — اجرا" else "I Am Authorized — Run"
            "live_execution" -> if (isFa) "در حال اجرا" else "Running Check"
            "cancel_task" -> if (isFa) "توقف اجرا" else "Cancel"
            "execution_summary" -> if (isFa) "خلاصه نتیجه" else "Result Summary"
            "findings" -> if (isFa) "یافته‌ها" else "Findings"
            "recommendation" -> if (isFa) "توصیه:" else "Recommendation:"
            "detailed_output" -> if (isFa) "خروجی کامل" else "Detailed Output"
            "open_reports_center" -> if (isFa) "مشاهده در بخش گزارش‌ها" else "Open Reports Center"

            // History
            "scan_history" -> if (isFa) "تاریخچه بررسی‌ها" else "Scan History"
            "execution_timeline" -> if (isFa) "فهرست اجراهای قبلی" else "Recent Checks"
            "clear_history" -> if (isFa) "پاک کردن تاریخچه" else "Clear History"
            "clear_history_confirm" -> if (isFa) "همه تاریخچه اجراها و مدارک ثبت‌شده پاک شود؟" else "Delete all scan history and evidence records?"
            "no_history" -> if (isFa) {
                "هنوز هیچ بررسی‌ای اجرا نشده است. پس از اجرای هر ابزار، نتیجه و زمان آن اینجا ثبت می‌شود."
            } else {
                "No checks have run yet. Completed checks and timestamps appear here automatically."
            }
            "evidence_records" -> if (isFa) "سوابق بررسی فایل‌ها" else "File Integrity Records"

            // Reports & Viewers
            "reports_center" -> if (isFa) "گزارش‌ها" else "Reports Center"
            "no_reports" -> if (isFa) {
                "هنوز گزارشی ذخیره نشده است. با اجرای هر ابزار، گزارش کامل به دو فرمت JSON و HTML ساخته می‌شود."
            } else {
                "No reports saved yet. Run any tool to generate structured JSON and HTML reports."
            }
            "delete_all_reports" -> if (isFa) "حذف همه گزارش‌ها" else "Delete All Reports"
            "delete_all_reports_confirm" -> if (isFa) "آیا از حذف دائمی همه گزارش‌ها مطمئن هستید؟" else "Permanently delete all saved reports?"
            "delete_report_confirm" -> if (isFa) "این گزارش و فایل‌های آن حذف شود؟" else "Delete this report and its local JSON/HTML files?"
            "report_viewer" -> if (isFa) "نمایشگر گزارش" else "Report Viewer"
            "tab_summary" -> if (isFa) "خلاصه" else "Summary"
            "tab_json" -> "JSON"
            "tab_html" -> "HTML"
            "copy_json" -> if (isFa) "کپی JSON" else "Copy JSON"
            "json_copied" -> if (isFa) "متن JSON در حافظه کپی شد." else "JSON copied to clipboard."
            "export_json" -> if (isFa) "ذخیره JSON" else "Save JSON"
            "export_html" -> if (isFa) "ذخیره HTML" else "Save HTML"
            "share_html" -> if (isFa) "اشتراک HTML" else "Share HTML"
            "share_json" -> if (isFa) "اشتراک JSON" else "Share JSON"
            "json_error_title" -> if (isFa) "خطا در خواندن فایل JSON" else "Unable to Parse JSON Report"
            "html_error_title" -> if (isFa) "فایل HTML گزارش در دسترس نیست" else "HTML Report Unavailable"
            "report_not_found" -> if (isFa) "گزارش انتخاب‌شده پیدا نشد." else "The selected report could not be found."

            // Settings
            "settings_title" -> if (isFa) "تنظیمات" else "Settings"
            "settings_subtitle" -> if (isFa) "شخصی‌سازی، زبان، ظاهر و راهنمای کاربر" else "Appearance, language, safety & user manual"
            "appearance_language" -> if (isFa) "ظاهر و زبان" else "Appearance & Language"
            "theme_mode" -> if (isFa) "حالت نمایش (تم)" else "Theme"
            "theme_dark" -> if (isFa) "تیره" else "Dark"
            "theme_light" -> if (isFa) "روشن" else "Light"
            "theme_system" -> if (isFa) "مطابق سیستم" else "System"
            "language_pref" -> if (isFa) "زبان برنامه" else "Language"
            "safety_execution" -> if (isFa) "ایمنی و اجرای ابزارها" else "Safety & Execution"
            "auth_confirm_title" -> if (isFa) "پرسش پیش از بررسی شبکه/وب" else "Confirm Before Active Checks"
            "auth_confirm_desc" -> if (isFa) {
                "پیش از بررسی پورت‌ها، شبکه محلی یا هدر وب‌سایت، پیام تأیید مجوز نمایش داده شود."
            } else {
                "Ask for confirmation before port probes, LAN scans, or website header audits."
            }
            "animations_title" -> if (isFa) "انیمیشن‌های رابط کاربری" else "Smooth Transitions"
            "animations_desc" -> if (isFa) "حرکت‌های نرم هنگام جابه‌جایی بین صفحه‌ها و باز کردن راهنما." else "Subtle screen transitions and expandable help panels."
            "network_timeout" -> if (isFa) "مهلت انتظار شبکه" else "Network Timeout"
            "seconds_unit" -> if (isFa) "ثانیه" else "sec"
            "parallel_limit" -> if (isFa) "حداکثر درخواست هم‌زمان" else "Parallel Check Limit"
            "doh_resolver" -> if (isFa) "سرویس‌دهنده DNS امن (DoH)" else "Encrypted DNS Provider (DoH)"
            "proxy_title" -> if (isFa) "پراکسی اختیاری شبکه (HTTP / SOCKS5)" else "Optional Network Proxy"
            "proxy_desc" -> if (isFa) {
                "در صورت نیاز آدرس پراکسی را وارد کنید (مثلاً http://127.0.0.1:8080). برای غیرفعال‌سازی خالی بگذارید."
            } else {
                "Optional proxy for network requests (e.g. http://127.0.0.1:8080). Leave empty to connect directly."
            }
            "save_proxy" -> if (isFa) "ذخیره پراکسی" else "Save Proxy"
            "system_help_section" -> if (isFa) "راهنما و وضعیت دستگاه" else "Manual & Device Status"
            "data_cleanup" -> if (isFa) "مدیریت داده‌ها" else "Data & Reset"
            "clear_all_data" -> if (isFa) "پاک کردن داده‌ها" else "Clear All Data"
            "reset_defaults" -> if (isFa) "بازنشانی تنظیمات" else "Reset Settings"

            // Help modal labels
            "help_what_it_does" -> if (isFa) "چه کاری انجام می‌دهد" else "What it does"
            "help_when_to_use" -> if (isFa) "چه زمانی استفاده کنید" else "When to use it"
            "help_prerequisites" -> if (isFa) "پیش‌نیازها" else "Requirements"
            "help_security_note" -> if (isFa) "نکته ایمنی و مسئولانه" else "Important note"
            "manual_search_hint" -> if (isFa) "جستجو در راهنمای کاربر..." else "Search user manual..."

            else -> key
        }
    }

    fun categoryTitle(cat: ModuleCategory, languageCode: String): String {
        val isFa = isPersian(languageCode)
        return when (cat) {
            ModuleCategory.NETWORK -> if (isFa) "شبکه" else "Network"
            ModuleCategory.WEB_SECURITY -> if (isFa) "امنیت وب" else "Web Safety"
            ModuleCategory.OSINT -> if (isFa) "اطلاعات عمومی (OSINT)" else "Public Intel"
            ModuleCategory.WIRELESS -> if (isFa) "وای‌فای" else "Wi-Fi Info"
            ModuleCategory.CRYPTOGRAPHY -> if (isFa) "رمزنگاری و هش" else "Cryptography"
            ModuleCategory.FORENSICS -> if (isFa) "بررسی فایل (جرم‌شناسی)" else "File Forensics"
            ModuleCategory.REPORTS -> if (isFa) "گزارش‌ها" else "Reports"
            ModuleCategory.CUSTOM_TOOLS -> if (isFa) "بررسی‌های ترکیبی" else "Preset Audits"
            ModuleCategory.SETTINGS -> if (isFa) "تنظیمات" else "Settings"
        }
    }

    fun categorySubtitle(cat: ModuleCategory, languageCode: String): String {
        val isFa = isPersian(languageCode)
        return when (cat) {
            ModuleCategory.NETWORK -> if (isFa) "وضعیت اتصال، DNS و بررسی شبکه محلی" else "Connection info, DNS & network checks"
            ModuleCategory.WEB_SECURITY -> if (isFa) "بررسی امنیت هدرها، گواهی HTTPS و پیکربندی سایت" else "HTTPS certificates, headers & site config"
            ModuleCategory.OSINT -> if (isFa) "اطلاعات دامنه، IP عمومی و بررسی نام کاربری" else "Domain WHOIS, IP lookup & username checks"
            ModuleCategory.WIRELESS -> if (isFa) "مشخصات وای‌فای متصل و شبکه‌های اطراف" else "Active Wi-Fi & nearby network scan"
            ModuleCategory.CRYPTOGRAPHY -> if (isFa) "محاسبه و تأیید هش، تبدیل متن و توکن امن" else "Hashes, checksums, encodings & tokens"
            ModuleCategory.FORENSICS -> if (isFa) "تشخیص نوع فایل، اطلاعات تصویر (EXIF) و متن داخل فایل" else "File type, photo EXIF & readable strings"
            ModuleCategory.REPORTS -> if (isFa) "مشاهده، اشتراک و ذخیره گزارش‌های JSON و HTML" else "View, share & export JSON and HTML reports"
            ModuleCategory.CUSTOM_TOOLS -> if (isFa) "بررسی چندمرحله‌ای وب‌سایت، دامنه و موتور پایتون" else "Multi-step site audits & Python helper"
            ModuleCategory.SETTINGS -> if (isFa) "ظاهر، زبان، مجوزها و راهنمای کامل" else "Theme, language, safety & user manual"
        }
    }

    fun capabilityLabel(state: CapabilityState, languageCode: String): String {
        val isFa = isPersian(languageCode)
        return when (state) {
            CapabilityState.AVAILABLE -> if (isFa) "آماده اجرا" else "Ready"
            CapabilityState.AVAILABLE_WITH_LIMITATIONS -> if (isFa) "با محدودیت" else "Limited"
            CapabilityState.REQUIRES_PERMISSION -> if (isFa) "نیازمند مجوز" else "Needs Permission"
            CapabilityState.UNSUPPORTED -> if (isFa) "پشتیبانی نمی‌شود" else "Unsupported"
            CapabilityState.NOT_IMPLEMENTED -> if (isFa) "غیرفعال" else "Unavailable"
        }
    }
}
