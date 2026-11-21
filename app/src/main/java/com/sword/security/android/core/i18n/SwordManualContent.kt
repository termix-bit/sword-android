package com.sword.security.android.core.i18n

data class ManualSection(
    val number: Int,
    val id: String,
    val title: String,
    val summary: String,
    val bullets: List<String>,
    val note: String? = null
)

object SwordManualContent {

    fun sections(languageCode: String): List<ManualSection> {
        return if (languageCode == "fa") persianSections else englishSections
    }

    private val englishSections = listOf(
        ManualSection(
            number = 1,
            id = "intro",
            title = "1. Introduction to Sword",
            summary = "Sword Android is a native security diagnostic, web safety, OSINT, cryptography, and file inspection toolkit built specifically for Android without requiring root access or Termux.",
            bullets = listOf(
                "Checks your active network, DNS, websites, Wi-Fi environment, hashes, and files.",
                "Runs native Android APIs alongside a built-in Python 3.10 runtime (Chaquopy) packaged inside the app.",
                "Saves structured JSON and HTML reports privately on your device."
            ),
            note = "Sword is strictly for defensive checks, learning, and authorized testing—offensive attack tools are intentionally excluded."
        ),
        ManualSection(
            number = 2,
            id = "getting_started",
            title = "2. Getting Started",
            summary = "You can start using most Sword features right away without changing complex settings.",
            bullets = listOf(
                "Pick your preferred language (English or Persian) and walk through the brief welcome tutorial on first launch.",
                "Pick any Quick Check on the Home screen or open the Tools tab to browse all modules.",
                "Enter the website, domain, IP, text, or file requested and tap Run Check.",
                "Review findings on screen, tap Explain Results for beginner-friendly guidance, or open saved results in the Reports tab."
            )
        ),
        ManualSection(
            number = 3,
            id = "home_screen",
            title = "3. Home Screen",
            summary = "The Home screen is organized so non-technical users can act quickly without feeling overwhelmed.",
            bullets = listOf(
                "Quick Checks launch common tasks like Check Website, Check Certificate, Domain Lookup, Create Hash, and Inspect File in one tap.",
                "Device Readiness shows at a glance whether you are online and how many permissions are granted.",
                "Tool Categories take you directly to Network, Web Safety, Public Intel, Wi-Fi Info, Cryptography, File Forensics, or Preset Audits.",
                "Recent Reports let you tap a saved scan to open it directly in the in-app Report Viewer."
            )
        ),
        ManualSection(
            number = 4,
            id = "network_tools",
            title = "4. Network Tools",
            summary = "Understand how your phone connects to the internet and test authorized hosts or your local network.",
            bullets = listOf(
                "Network Interface Inspector: Shows active IP addresses, DNS servers, and MTU without root.",
                "DNS Record Lookup (DoH): Looks up A, AAAA, MX, TXT, NS, CNAME, and SOA records over encrypted HTTPS.",
                "Connectivity & Latency Diagnostics: Measures DNS lookup speed and TCP connection response time.",
                "Authorized TCP Port Probe: Checks up to 64 ports on a target you own or are permitted to test.",
                "Authorized Local LAN Discovery: Finds reachable devices on your active local /24 subnet."
            ),
            note = "Raw-packet SYN scans (hping3 / nmap -sS) require root privileges blocked by Android and are marked Unsupported."
        ),
        ManualSection(
            number = 5,
            id = "web_tools",
            title = "5. Web Tools",
            summary = "Verify whether a website protects visitors with HTTPS and modern browser security headers.",
            bullets = listOf(
                "HTTP Security Header Analyzer: Inspects HSTS, Content-Security-Policy, clickjacking headers, X-Content-Type-Options, and cookie flags.",
                "TLS / X.509 Certificate Inspector: Checks TLS protocol version, cipher suite, certificate issuer, domain SANs, and days until expiration.",
                "Authorized Site Config Check: Inspects public /.well-known/security.txt and /robots.txt files over HTTPS."
            )
        ),
        ManualSection(
            number = 6,
            id = "osint_tools",
            title = "6. OSINT Tools (Public Intelligence)",
            summary = "Gather strictly public domain, IP, email DNS, and username availability data over encrypted HTTPS.",
            bullets = listOf(
                "Domain RDAP / WHOIS Lookup: Shows registrar, nameservers, and registration/expiry dates via HTTPS RDAP.",
                "Public IP & ASN Intelligence: Identifies ISP, ASN, organization, and country for a public IP.",
                "Domain DNS & Mail Posture: Audits MX, SPF, and DMARC records to spot missing email spoofing protections.",
                "Username Availability (Python Engine): Checks 5–50 public sites using the bundled WhatsMyName dataset inside Python 3.10."
            )
        ),
        ManualSection(
            number = 7,
            id = "wireless_tools",
            title = "7. Wireless Tools",
            summary = "Inspect your active Wi-Fi link and check nearby wireless networks for weak security.",
            bullets = listOf(
                "Active Wi-Fi Connection Info: Displays frequency band, link speed, signal level (RSSI), and SSID/BSSID when permitted.",
                "Nearby Wi-Fi Access Points: Lists nearby networks and flags open or legacy WEP networks."
            ),
            note = "Android limits apps to 4 Wi-Fi scans every 2 minutes and requires Location Services plus Wi-Fi/Location permission. Monitor mode and deauth attacks are unsupported."
        ),
        ManualSection(
            number = 8,
            id = "cryptography",
            title = "8. Cryptography",
            summary = "Offline utilities for integrity checksums, format conversions, and secure random tokens.",
            bullets = listOf(
                "Cryptographic Hash Generator: Calculates SHA-256 and SHA-512 digests (and marks MD5/SHA-1 as legacy).",
                "Constant-Time Hash Verifier: Compares text against an expected SHA-256 or SHA-512 digest safely.",
                "Encoding & Cipher Utility: Converts Base64, Hex, URL encoding, and ROT13 (remember these are encodings, not encryption).",
                "Secure Random Token Generator: Generates 8–128 byte CSPRNG tokens in Hex, Base64URL, and UUID v4."
            )
        ),
        ManualSection(
            number = 9,
            id = "forensics",
            title = "9. Forensics (File Inspection)",
            summary = "Inspect individual files safely using Android's standard file picker without granting broad storage access.",
            bullets = listOf(
                "File Signature & Evidence Hasher: Detects true file type from magic bytes, computes SHA-256/512, and saves an integrity record.",
                "Image EXIF Metadata Audit: Reveals hidden GPS coordinates, camera model, and timestamps inside photos.",
                "Safe Hex Dump & Strings Extractor: Extracts readable ASCII strings and a 320-byte hex preview (capped at 256 KB for speed)."
            )
        ),
        ManualSection(
            number = 10,
            id = "reports_exporting",
            title = "10. Reports and Exporting",
            summary = "Every completed check saves a structured JSON file and a styled HTML report in Sword's private storage.",
            bullets = listOf(
                "Tap any report in the Reports tab to open Sword's built-in Report Viewer with four tabs: Summary, JSON, HTML, and Explain.",
                "JSON tab: Displays syntax-highlighted, indented JSON with a Copy button.",
                "HTML tab: Renders the full styled report directly inside Sword using a locked-down local WebView (JavaScript and file access disabled).",
                "Explain tab: Translates structured findings offline on your device into plain English or Persian while preserving IPs, CVEs, domains, ports, and original severity.",
                "Export / Save lets you save a copy of the JSON or HTML file to Downloads or another folder via Android SAF.",
                "Share lets you send the HTML or JSON file to another app; Delete removes individual or all reports."
            )
        ),
        ManualSection(
            number = 11,
            id = "permissions_device",
            title = "11. Permissions and Device Requirements",
            summary = "Sword only asks for runtime permissions when a specific Wi-Fi feature needs them.",
            bullets = listOf(
                "Network, Web, OSINT, Cryptography, and Preset tools need no runtime permissions.",
                "File Forensics uses Android's file picker so you grant access only to the single file you select.",
                "Wi-Fi diagnostics use Nearby Wi-Fi Devices (Android 13+) or Fine Location (Android 8–12) plus the device Location switch.",
                "Use Settings > Device Capabilities Audit to check connectivity, API level, and Python 3.10 runtime readiness."
            )
        ),
        ManualSection(
            number = 12,
            id = "settings_troubleshooting",
            title = "12. Settings and Troubleshooting",
            summary = "Customize appearance, language, timeouts, DNS resolver, and optional proxy—or troubleshoot common issues.",
            bullets = listOf(
                "Theme, Language & Replay Tutorial: Switch between Dark/Light/System themes and English or Persian (automatic RTL), or replay the onboarding tutorial anytime at the top of Settings.",
                "Network Timeout & Concurrency: Increase timeout on slow networks or lower parallel probe limits if routers rate-limit you.",
                "Encrypted DNS (DoH): Choose Cloudflare, Google, or Quad9.",
                "Proxy: Enter http://host:port or socks5://host:port if needed, or leave blank and save to disable.",
                "Troubleshooting Wi-Fi: Turn on both Wi-Fi and system Location, grant permission in Permissions Manager, and wait 2 minutes if throttled."
            )
        ),
        ManualSection(
            number = 13,
            id = "security_responsible_use",
            title = "13. Security and Responsible Use",
            summary = "Sword is built around defensive transparency and Android security boundaries.",
            bullets = listOf(
                "Always obtain explicit ownership or written permission before scanning external domains, ports, or networks.",
                "Keep 'Confirm Before Active Checks' enabled in Settings to prevent accidental scans.",
                "All network OSINT and DNS lookups run over HTTPS; cleartext HTTP traffic is blocked in the app manifest.",
                "Saved reports stay private inside the app until you choose to export or share them."
            )
        )
    )

    private val persianSections = listOf(
        ManualSection(
            number = 1,
            id = "intro",
            title = "۱. معرفی Sword Android",
            summary = "برنامه Sword Android یک جعبه ابزار بومی اندروید برای بررسی امنیت شبکه، وب‌سایت، اطلاعات عمومی، رمزنگاری و تحلیل فایل است که بدون نیاز به روت یا Termux اجرا می‌شود.",
            bullets = listOf(
                "بررسی اتصال شبکه، DNS، امنیت وب‌سایت‌ها، شبکه‌های وای‌فای، هش‌ها و فایل‌های دستگاه.",
                "ترکیب رابط کاربری بومی اندروید و موتور پایتون ۳.۱۰ تعبیه‌شده (Chaquopy) داخل خود برنامه.",
                "ذخیره خودکار گزارش‌های منظم JSON و HTML در حافظه خصوصی برنامه."
            ),
            note = "Sword فقط برای یادگیری، عیب‌یابی دفاعی و بررسی سیستم‌های مجاز طراحی شده و ابزارهای مخرب یا تهاجمی در آن وجود ندارد."
        ),
        ManualSection(
            number = 2,
            id = "getting_started",
            title = "۲. شروع سریع کار با برنامه",
            summary = "برای استفاده از بیشتر ابزارهای Sword به هیچ تنظیم پیچیده‌ای نیاز ندارید.",
            bullets = listOf(
                "در اولین ورود، زبان مورد نظر خود (فارسی یا انگلیسی) را انتخاب کنید و آموزش کوتاه شروع را ببینید.",
                "از صفحه خانه یکی از گزینه‌های «شروع سریع» را بزنید یا وارد تب «ابزارها» شوید.",
                "آدرس سایت، دامنه، IP، متن یا فایل مورد نظر را وارد کرده و «شروع بررسی» را بزنید.",
                "نتیجه را همان لحظه ببینید، برای فهم ساده‌تر دکمه «توضیح ساده نتایج» را بزنید یا بعداً در تب «گزارش‌ها» مطالعه کنید."
            )
        ),
        ManualSection(
            number = 3,
            id = "home_screen",
            title = "۳. آشنایی با صفحه خانه",
            summary = "صفحه خانه طوری طراحی شده که کاربران عادی هم بدون دانش فنی تخصصی بتوانند سریع کار خود را شروع کنند.",
            bullets = listOf(
                "بخش «شروع سریع» دسترسی یک‌لمسی به بررسی سایت، گواهی SSL، استعلام دامنه، ساخت هش و بررسی فایل را می‌دهد.",
                "کارت «آمادگی دستگاه» وضعیت آنلاین بودن و تعداد مجوزهای فعال را خلاصه نشان می‌دهد.",
                "بخش «دسته‌بندی ابزارها» شما را مستقیماً به شبکه، امنیت وب، اطلاعات عمومی، وای‌فای، رمزنگاری، بررسی فایل و بررسی‌های ترکیبی می‌برد.",
                "بخش «آخرین گزارش‌ها» امکان باز کردن فوری گزارش‌های اخیر در نمایشگر داخلی را فراهم می‌کند."
            )
        ),
        ManualSection(
            number = 4,
            id = "network_tools",
            title = "۴. ابزارهای شبکه",
            summary = "وضعیت اتصال شبکه گوشی را بررسی کنید و دامنه‌ها یا شبکه محلی مجاز خود را بسنجید.",
            bullets = listOf(
                "بررسی رابط‌های شبکه: نمایش IP فعال، سرورهای DNS و MTU بدون نیاز به روت.",
                "استعلام DNS رمزگذاری‌شده (DoH): دریافت رکوردهای A، AAAA، MX، TXT، NS، CNAME و SOA از طریق HTTPS.",
                "تست اتصال و تأخیر (Latency): اندازه‌گیری سرعت تبدیل دامنه و زمان پاسخ TCP.",
                "بررسی پورت‌های TCP مجاز: بررسی حداکثر ۶۴ پورت روی هدفی که مالک آن هستید یا اجازه دارید.",
                "شناسایی دستگاه‌های شبکه محلی (LAN): یافتن دستگاه‌های فعال در زیرشبکه /24 وای‌فای شما."
            ),
            note = "اسکن خام SYN (مانند hping3 یا nmap -sS) روی اندروید عادی بدون روت غیرفعال و پشتیبانی‌نشده است."
        ),
        ManualSection(
            number = 5,
            id = "web_tools",
            title = "۵. ابزارهای امنیت وب",
            summary = "بررسی کنید که یک وب‌سایت تا چه حد از HTTPS و سربرگ‌های امنیتی استاندارد استفاده می‌کند.",
            bullets = listOf(
                "تحلیلگر هدرهای امنیتی HTTP: بررسی HSTS، CSP، محافظت کلیک‌جکینگ، nosniff و پرچم امنیتی کوکی‌ها.",
                "بررسی گواهی TLS / X.509: نمایش نسخه TLS، الگوریتم رمزنگاری، صادرکننده، دامنه‌های مجاز (SAN) و روزهای باقی‌مانده تا انقضا.",
                "بررسی پیکربندی عمومی سایت: مطالعه غیرتهاجمی فایل‌های /.well-known/security.txt و /robots.txt."
            )
        ),
        ManualSection(
            number = 6,
            id = "osint_tools",
            title = "۶. ابزارهای اطلاعات عمومی (OSINT)",
            summary = "جمع‌آوری اطلاعات کاملاً عمومی درباره دامنه‌ها، آدرس‌های IP، ایمیل دامنه و نام‌های کاربری از طریق HTTPS.",
            bullets = listOf(
                "استعلام RDAP / WHOIS دامنه: نمایش ثبت‌کننده، سرورهای نام و تاریخ ثبت و انقضای دامنه.",
                "اطلاعات IP عمومی و ASN: نمایش شرکت ارائه‌دهنده اینترنت، شماره ASN و کشور IP.",
                "بررسی وضعیت DNS و ایمیل دامنه: بررسی رکوردهای MX، SPF و DMARC برای تشخیص ضعف در برابر جعل ایمیل.",
                "بررسی نام کاربری (موتور پایتون): جستجوی نام کاربری در ۵ تا ۵۰ سایت عمومی با دیتاست WhatsMyName."
            )
        ),
        ManualSection(
            number = 7,
            id = "wireless_tools",
            title = "۷. ابزارهای وای‌فای (Wireless)",
            summary = "بررسی کیفیت اتصال وای‌فای فعلی و امنیت شبکه‌های وای‌فای اطراف.",
            bullets = listOf(
                "اطلاعات اتصال فعال وای‌فای: نمایش باند فرکانسی، سرعت لینک، قدرت سیگنال و نام/شناسه شبکه در صورت داشتن مجوز.",
                "شبکه‌های وای‌فای اطراف: شناسایی شبکه‌های نزدیک و هشدار درباره شبکه‌های باز یا رمزنگاری ضعیف WEP."
            ),
            note = "سیستم‌عامل اندروید اسکن وای‌فای را به ۴ بار در هر ۲ دقیقه محدود می‌کند و نیازمند روشن بودن Location و مجوز وای‌فای/مکان است. حملات قطع اتصال یا مانیتور مود وجود ندارند."
        ),
        ManualSection(
            number = 8,
            id = "cryptography",
            title = "۸. رمزنگاری و هش",
            summary = "ابزارهای کاملاً آفلاین برای تولید و بررسی هش، تبدیل قالب متن و ساخت توکن‌های تصادفی امن.",
            bullets = listOf(
                "تولید هش رمزنگاری: محاسبه SHA-256 و SHA-512 (و مشخص کردن MD5/SHA-1 به‌عنوان روش قدیمی).",
                "تأییدکننده هش با زمان ثابت: مقایسه امن متن با هش SHA-256 یا SHA-512 مورد انتظار.",
                "تبدیل قالب و کدگذاری: Base64، Hex، URL و ROT13 (توجه داشته باشید این موارد رمزنگاری امن محسوب نمی‌شوند).",
                "ساخت توکن تصادفی امن: تولید کلیدهای ۸ تا ۱۲۸ بایتی با SecureRandom در قالب Hex، Base64URL و UUID v4."
            )
        ),
        ManualSection(
            number = 9,
            id = "forensics",
            title = "۹. بررسی فایل (جرم‌شناسی دیجیتال)",
            summary = "تحلیل ایمن فایل‌های انتخاب‌شده از طریق انتخابگر استاندارد اندروید بدون نیاز به دسترسی کامل حافظه.",
            bullets = listOf(
                "تشخیص امضای فایل و ثبت هش مدرک: شناسایی نوع واقعی فایل از بایت‌های آغازین و ذخیره SHA-256/512 در تاریخچه.",
                "بررسی اطلاعات پنهان تصویر (EXIF): کشف مختصات GPS، مدل دوربین و زمان ثبت داخل عکس‌ها.",
                "استخراج رشته‌های متنی و هگز ایمن: نمایش متون قابل خواندن و ۳۲۰ بایت اول هگز فایل (محدود به ۲۵۶ کیلوبایت برای سرعت)."
            )
        ),
        ManualSection(
            number = 10,
            id = "reports_exporting",
            title = "۱۰. گزارش‌ها و خروجی گرفتن",
            summary = "پس از پایان هر بررسی، یک گزارش JSON و یک گزارش HTML به‌صورت خودکار در فضای خصوصی برنامه ذخیره می‌شود.",
            bullets = listOf(
                "با لمس هر گزارش در تب «گزارش‌ها»، نمایشگر داخلی Sword با چهار تب «خلاصه»، «JSON»، «HTML» و «توضیح ساده» باز می‌شود.",
                "تب JSON: نمایش کد قالب‌بندی‌شده و رنگی با دکمه کپی.",
                "تب HTML: نمایش مستقیم صفحه گزارش داخل برنامه با WebView ایمن (بدون جاوااسکریپت و بدون دسترسی به فایل‌های دیگر).",
                "تب توضیح ساده: توضیح یافته‌های فنی را به‌صورت کاملاً آفلاین روی گوشی به فارسی یا انگلیسی ساده بدون تغییر داده‌های اصلی نمایش می‌دهد.",
                "گزینه «ذخیره فایل» امکان ذخیره کپی گزارش JSON یا HTML در پوشه دلخواه گوشی را می‌دهد.",
                "گزینه «اشتراک‌گذاری» برای ارسال فایل گزارش و «حذف» برای پاک کردن گزارش‌ها در دسترس است."
            )
        ),
        ManualSection(
            number = 11,
            id = "permissions_device",
            title = "۱۱. مجوزها و پیش‌نیازهای دستگاه",
            summary = "Sword فقط زمانی مجوز می‌خواهد که واقعاً برای آن ابزار لازم باشد.",
            bullets = listOf(
                "ابزارهای شبکه، وب، OSINT، رمزنگاری و بررسی‌های ترکیبی به هیچ مجوز حساسی نیاز ندارند.",
                "بررسی فایل از انتخابگر اندروید استفاده می‌کند و فقط به همان یک فایل انتخابی دسترسی دارد.",
                "ابزارهای وای‌فای به مجوز دستگاه‌های وای‌فای نزدیک (اندروید ۱۳+) یا مکان دقیق (اندروید ۸ تا ۱۲) و روشن بودن GPS/Location نیاز دارند.",
                "از بخش تنظیمات > ممیزی قابلیت‌های دستگاه می‌توانید وضعیت اینترنت و پایتون ۳.۱۰ را بررسی کنید."
            )
        ),
        ManualSection(
            number = 12,
            id = "settings_troubleshooting",
            title = "۱۲. تنظیمات و رفع اشکال",
            summary = "تنظیم ظاهر، زبان، مهلت انتظار، DNS امن، پراکسی اختیاری و راه‌حل خطاهای رایج.",
            bullets = listOf(
                "تم، زبان و نمایش دوباره آموزش: انتخاب بین حالت تیره، روشن یا مطابق سیستم، تغییر زبان انگلیسی/فارسی (با راست‌به‌چپ خودکار) یا مرور دوباره راهنمای شروع از بالای صفحه تنظیمات.",
                "مهلت انتظار و هم‌زمانی: در اینترنت کند مهلت انتظار را بیشتر کنید و در صورت محدودیت روتر، تعداد درخواست هم‌زمان را کم کنید.",
                "DNS امن (DoH): انتخاب بین Cloudflare، Google یا Quad9.",
                "پراکسی: فقط در صورت نیاز آدرس پراکسی را وارد کنید؛ برای اتصال مستقیم کادر را خالی کرده و ذخیره بزنید.",
                "رفع خطای اسکن وای‌فای: وای‌فای و Location گوشی را روشن کنید، مجوز را در مدیریت مجوزها بدهید و در صورت محدودیت ۲ دقیقه صبر کنید."
            )
        ),
        ManualSection(
            number = 13,
            id = "security_responsible_use",
            title = "۱۳. امنیت و استفاده مسئولانه",
            summary = "Sword بر پایه شفافیت دفاعی و مرزهای امنیتی اندروید ساخته شده است.",
            bullets = listOf(
                "پیش از بررسی پورت، شبکه یا وب‌سایت، حتماً مطمئن شوید مالک هدف هستید یا اجازه کتبی دارید.",
                "گزینه «پرسش پیش از بررسی شبکه/وب» را در تنظیمات روشن نگه دارید.",
                "تمام ارتباطات OSINT و DNS به‌صورت رمزگذاری‌شده HTTPS انجام می‌شوند و ترافیک غیررمزگذاری‌شده مسدود است.",
                "گزارش‌ها داخل فضای خصوصی برنامه باقی می‌مانند تا زمانی که خودتان آن‌ها را ذخیره یا به اشتراک بگذارید."
            )
        )
    )
}
