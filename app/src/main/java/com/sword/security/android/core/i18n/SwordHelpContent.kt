package com.sword.security.android.core.i18n

import com.sword.security.android.core.model.ModuleCategory
import com.sword.security.android.core.model.ToolDescriptor

data class HelpTopic(
    val id: String,
    val title: String,
    val whatItDoes: String,
    val whenToUse: String,
    val prerequisites: String,
    val securityNote: String
)

object SwordHelpContent {

    fun forKey(key: String, languageCode: String): HelpTopic {
        val fa = languageCode == "fa"
        return when (key) {
            "home_overview" -> HelpTopic(
                id = key,
                title = if (fa) "درباره صفحه خانه Sword" else "About the Sword Home Screen",
                whatItDoes = if (fa) {
                    "این صفحه نقطه شروع سریع شماست: می‌توانید با یک لمس بررسی‌های پرکاربرد را اجرا کنید، دسته‌های ابزار را باز کنید یا آخرین گزارش‌های امنیتی را ببینید."
                } else {
                    "This screen is your starting point. Run common checks in one tap, browse tool categories, or open your latest saved reports."
                },
                whenToUse = if (fa) {
                    "هر زمان که می‌خواهید سریع امنیت یک وب‌سایت، وضعیت وای‌فای یا سلامت یک فایل را بررسی کنید."
                } else {
                    "Use it whenever you want to quickly check a website, Wi-Fi connection, or file without digging through menus."
                },
                prerequisites = if (fa) {
                    "ابزارهای پایه آفلاین (مثل رمزنگاری و بررسی فایل) بدون اینترنت کار می‌کنند؛ بررسی‌های وب و دامنه به اتصال اینترنت نیاز دارند."
                } else {
                    "Offline tools (hashing, file inspection) work anywhere. Web and domain checks require an internet connection."
                },
                securityNote = if (fa) {
                    "Sword یک ابزار بررسی دفاعی و آموزشی است و نیازی به روت بودن گوشی ندارد. فقط اهدافی را بررسی کنید که مالک آن هستید یا اجازه دارید."
                } else {
                    "Sword is a defensive diagnostic toolkit that runs safely without root. Only scan systems, domains, or networks you own or have permission to test."
                }
            )

            "quick_checks" -> HelpTopic(
                id = key,
                title = if (fa) "بررسی‌های سریع" else "Quick Checks",
                whatItDoes = if (fa) {
                    "میانبر مستقیم به پنج ابزار پرکاربرد بدون نیاز به جستجو در فهرست کامل ابزارها."
                } else {
                    "Direct shortcuts to the five most commonly used tools so you can start a check in seconds."
                },
                whenToUse = if (fa) {
                    "وقتی می‌خواهید سریع هدرهای یک سایت، گواهی SSL، رکورد دامنه، هش یک متن یا نوع واقعی یک فایل را بررسی کنید."
                } else {
                    "When you want to inspect a website's headers, SSL certificate, domain records, text hash, or file type right away."
                },
                prerequisites = if (fa) {
                    "بررسی‌های وب و دامنه به اینترنت نیاز دارند؛ بررسی فایل از انتخابگر استاندارد اندروید استفاده می‌کند."
                } else {
                    "Website and domain checks require internet access; file checks use Android's standard file picker."
                },
                securityNote = if (fa) {
                    "هر بررسی پس از پایان به‌صورت خودکار یک گزارش JSON و HTML در حافظه خصوصی برنامه ذخیره می‌کند."
                } else {
                    "Every completed check automatically saves a private JSON and HTML report inside the app."
                }
            )

            "device_readiness" -> HelpTopic(
                id = key,
                title = if (fa) "آمادگی دستگاه و مجوزها" else "Device Readiness & Permissions",
                whatItDoes = if (fa) {
                    "نشان می‌دهد آیا دستگاه به شبکه متصل است و چه تعداد از مجوزهای اختیاری برنامه فعال هستند."
                } else {
                    "Shows whether your phone is online and how many optional permissions are granted."
                },
                whenToUse = if (fa) {
                    "اگر ابزاری مثل اسکن وای‌فای پیغام کمبود مجوز داد، از اینجا به صفحه مجوزها یا قابلیت‌ها بروید."
                } else {
                    "Check this if a Wi-Fi or network tool asks for permission or reports limited access."
                },
                prerequisites = if (fa) {
                    "بیشتر ابزارهای Sword بدون هیچ مجوز حساسی کار می‌کنند؛ فقط اسکن وای‌فای به مجوز مکان یا دستگاه‌های نزدیک نیاز دارد."
                } else {
                    "Most Sword tools work without sensitive permissions; only Wi-Fi scanning needs Location or Nearby Wi-Fi permission."
                },
                securityNote = if (fa) {
                    "Sword هرگز مجوز دسترسی کامل به همه فایل‌های گوشی درخواست نمی‌کند."
                } else {
                    "Sword never requests broad external storage permissions."
                }
            )

            "auth_confirmation" -> HelpTopic(
                id = key,
                title = if (fa) "تأیید مجوز پیش از اسکن" else "Authorization Confirmation Prompt",
                whatItDoes = if (fa) {
                    "پیش از اجرای بررسی‌های فعال شبکه یا وب (مانند اسکن پورت، جستجوی شبکه محلی یا بررسی هدر سایت) یک پنجره تأیید نمایش می‌دهد."
                } else {
                    "Displays a confirmation dialog before active network or web checks (port probes, LAN discovery, or HTTP header audits)."
                },
                whenToUse = if (fa) {
                    "پیشنهاد می‌شود همیشه روشن بماند تا به‌صورت تصادفی به هدف ناخواسته درخواست ارسال نشود."
                } else {
                    "Recommended to keep enabled so active checks are never launched unintentionally."
                },
                prerequisites = if (fa) {
                    "نیاز به تنظیم خاص دیگری ندارد."
                } else {
                    "No extra setup required."
                },
                securityNote = if (fa) {
                    "اسکن کردن شبکه یا وب‌سایت دیگران بدون اجازه ممکن است غیرقانونی یا خلاف قوانین شبکه باشد."
                } else {
                    "Testing external networks or websites without explicit authorization may violate local laws or network policies."
                }
            )

            "doh_resolver" -> HelpTopic(
                id = key,
                title = if (fa) "سرویس DNS رمزگذاری‌شده (DoH)" else "Encrypted DNS (DNS-over-HTTPS)",
                whatItDoes = if (fa) {
                    "نام دامنه را از طریق اتصال رمزگذاری‌شده HTTPS به IP و رکوردهای DNS تبدیل می‌کند تا در مسیر شبکه شنود نشود."
                } else {
                    "Looks up domain DNS records over an encrypted HTTPS connection instead of plain unencrypted DNS."
                },
                whenToUse = if (fa) {
                    "هنگام استفاده از ابزارهای استعلام DNS یا بررسی تنظیمات ایمیل دامنه (SPF / DMARC)."
                } else {
                    "Used automatically by Domain Lookup and Domain DNS & Mail Posture tools."
                },
                prerequisites = if (fa) {
                    "اتصال اینترنت فعال لازم است."
                } else {
                    "Requires an active internet connection."
                },
                securityNote = if (fa) {
                    "می‌توانید بین Cloudflare، Google یا Quad9 یکی را انتخاب کنید."
                } else {
                    "You can choose between Cloudflare, Google, or Quad9 as your trusted resolver."
                }
            )

            "parallel_concurrency" -> HelpTopic(
                id = key,
                title = if (fa) "تعداد بررسی‌های هم‌زمان" else "Parallel Check Limit",
                whatItDoes = if (fa) {
                    "مشخص می‌کند ابزارهای شبکه مانند بررسی پورت‌ها یا دستگاه‌های شبکه محلی چند بررسی را هم‌زمان انجام دهند."
                } else {
                    "Controls how many simultaneous connections tools like Port Probe or LAN Discovery open at once."
                },
                whenToUse = if (fa) {
                    "اگر شبکه کند است یا باطری/روتر تحت فشار قرار می‌گیرد، این عدد را کاهش دهید (مثلاً ۲ یا ۳)."
                } else {
                    "Lower this number (e.g., 2–3) on slow Wi-Fi or sensitive routers; raise it up to 8 for faster local checks."
                },
                prerequisites = if (fa) {
                    "مقدار مجاز بین ۱ تا ۸ است."
                } else {
                    "Adjustable from 1 to 8 concurrent probes."
                },
                securityNote = if (fa) {
                    "تعداد بسیار بالا ممکن است باعث مسدود شدن موقت توسط فایروال شبکه شود."
                } else {
                    "Higher concurrency may trigger rate limits on some routers or firewalls."
                }
            )

            "proxy_config" -> HelpTopic(
                id = key,
                title = if (fa) "پراکسی شبکه" else "Application Proxy",
                whatItDoes = if (fa) {
                    "ترافیک ابزارهای وب و OSINT را از طریق سرور پراکسی HTTP یا SOCKS5 مشخص‌شده عبور می‌دهد."
                } else {
                    "Routes Sword's HTTP/HTTPS inspection requests through a local or external HTTP or SOCKS5 proxy."
                },
                whenToUse = if (fa) {
                    "فقط زمانی استفاده کنید که بخواهید درخواست‌ها را از طریق ابزار تحلیل محلی یا پراکسی شبکه خود عبور دهید."
                } else {
                    "Only needed if you intentionally route diagnostics through a local proxy or corporate gateway."
                },
                prerequisites = if (fa) {
                    "فرمت معتبر: http://host:port یا socks5://host:port."
                } else {
                    "Must use a valid format such as http://127.0.0.1:8080 or socks5://127.0.0.1:1080."
                },
                securityNote = if (fa) {
                    "برای اتصال مستقیم بدون پراکسی، کادر را خالی بگذارید و ذخیره کنید."
                } else {
                    "Leave blank and save to return to direct connections."
                }
            )

            "reports_viewer" -> HelpTopic(
                id = key,
                title = if (fa) "مشاهده، ذخیره و اشتراک گزارش‌ها" else "Viewing, Saving & Sharing Reports",
                whatItDoes = if (fa) {
                    "گزارش‌ها مستقیماً داخل خود Sword در سه بخش «خلاصه»، «JSON» (با رنگ‌بندی خوانا و دکمه کپی) و «HTML» (نمایش کامل در صفحه داخلی) باز می‌شوند."
                } else {
                    "Reports open directly inside Sword with three views: Summary, formatted JSON (with syntax highlighting and Copy), and rendered HTML."
                },
                whenToUse = if (fa) {
                    "هر زمان که بخواهید نتایج بررسی قبلی را بخوانید، کپی کنید یا به‌صورت فایل به پوشه Downloads منتقل کنید."
                } else {
                    "Use this whenever you want to inspect findings, copy structured JSON, or export a report file to your device."
                },
                prerequisites = if (fa) {
                    "تمام گزارش‌ها در فضای خصوصی و امن خود برنامه ذخیره می‌شوند."
                } else {
                    "Reports are stored in Sword's private app directory until you explicitly share or export them."
                },
                securityNote = if (fa) {
                    "برای حفظ امنیت، نمایشگر HTML داخلی بدون جاوااسکریپت و بدون دسترسی به فایل‌های دیگر گوشی اجرا می‌شود."
                } else {
                    "For safety, the built-in HTML viewer runs with JavaScript, file access, and external navigation disabled."
                }
            )

            "permissions_overview" -> HelpTopic(
                id = key,
                title = if (fa) "چرا این مجوزها پرسیده می‌شوند؟" else "Why Sword Uses These Permissions",
                whatItDoes = if (fa) {
                    "مدیریت مجوزها وضعیت واقعی دسترسی‌های اندروید را نشان می‌دهد و توضیح می‌دهد هر مجوز دقیقاً برای کدام ابزار است."
                } else {
                    "Shows the live grant state of Android permissions and exactly which tools depend on each one."
                },
                whenToUse = if (fa) {
                    "اگر ابزارهای وای‌فای نام شبکه را نشان نمی‌دهند یا دکمه اجرا غیرفعال است."
                } else {
                    "Open this if Wi-Fi diagnostics cannot read SSID/BSSID or nearby scan results."
                },
                prerequisites = if (fa) {
                    "در اندروید، خواندن نام وای‌فای و اسکن شبکه‌های اطراف علاوه بر مجوز، نیازمند روشن بودن Location گوشی نیز هست."
                } else {
                    "On Android, reading Wi-Fi SSIDs and nearby networks also requires the system Location toggle to be turned on."
                },
                securityNote = if (fa) {
                    "بررسی فایل‌ها بدون هیچ مجوز حافظه انجام می‌شود و فقط فایلی را می‌خواند که خودتان انتخاب کنید."
                } else {
                    "File Forensics uses Android's document picker so you only share the single file you pick."
                }
            )

            else -> HelpTopic(
                id = key,
                title = if (fa) "راهنمای بخش" else "Section Help",
                whatItDoes = if (fa) "توضیحات راهنما برای استفاده بهتر و ایمن‌تر از این بخش." else "Guidance for using this feature safely and effectively.",
                whenToUse = if (fa) "هنگام کار با امکانات اصلی برنامه." else "When configuring or reviewing results in this screen.",
                prerequisites = if (fa) "دسترسی استاندارد دستگاه اندروید." else "Standard Android permissions.",
                securityNote = if (fa) "همیشه فقط روی اهداف مجاز استفاده کنید." else "Always operate only on authorized targets."
            )
        }
    }

    fun forCategory(category: ModuleCategory, languageCode: String): HelpTopic {
        val fa = languageCode == "fa"
        return when (category) {
            ModuleCategory.NETWORK -> HelpTopic(
                id = "cat_network",
                title = SwordStrings.categoryTitle(category, languageCode),
                whatItDoes = if (fa) {
                    "مشخصات اتصال اینترنت گوشی، رکوردهای DNS دامنه، سرعت اتصال (تأخیر) و باز بودن پورت‌های مجاز را بررسی می‌کند."
                } else {
                    "Inspects your device's active IP/DNS settings, looks up domain DNS records, measures connection latency, and checks authorized ports or local LAN devices."
                },
                whenToUse = if (fa) {
                    "برای عیب‌یابی کندی اینترنت، اطمینان از تنظیم درست DNS یا شناسایی دستگاه‌های متصل به وای‌فای خانگی خودتان."
                } else {
                    "Use to troubleshoot slow connections, verify DNS setup, or discover devices on your own home/office Wi-Fi."
                },
                prerequisites = if (fa) {
                    "بیشتر ابزارها به اینترنت یا اتصال وای‌فای نیاز دارند؛ بدون نیاز به روت."
                } else {
                    "Requires an active Wi-Fi or mobile network connection (no root required)."
                },
                securityNote = if (fa) {
                    "اسکن پورت به حداکثر ۶۴ پورت محدود شده است و روش‌های تهاجمی شبکه (Raw SYN) روی اندروید عادی مجاز نیستند."
                } else {
                    "Port probes are capped at 64 ports per run; raw-socket SYN scans are intentionally excluded on standard Android."
                }
            )

            ModuleCategory.WEB_SECURITY -> HelpTopic(
                id = "cat_web",
                title = SwordStrings.categoryTitle(category, languageCode),
                whatItDoes = if (fa) {
                    "سربرگ‌های امنیتی وب‌سایت‌ها (HSTS، CSP، کوکی‌ها)، اعتبار گواهی رمزنگاری HTTPS/TLS و فایل‌های عمومی security.txt و robots.txt را بررسی می‌کند."
                } else {
                    "Checks website HTTPS certificates, security response headers (HSTS, CSP, cookies), and public security.txt / robots.txt configuration."
                },
                whenToUse = if (fa) {
                    "برای مدیران سایت یا کاربران عادی که می‌خواهند بدانند یک وب‌سایت از HTTPS و هدرهای امنیتی استاندارد استفاده می‌کند یا نه."
                } else {
                    "Use to verify whether a website uses valid HTTPS certificates and modern browser security headers."
                },
                prerequisites = if (fa) {
                    "اتصال اینترنت و آدرس یا دامنه معتبر وب‌سایت."
                } else {
                    "Requires internet access and a valid domain or HTTP/HTTPS URL."
                },
                securityNote = if (fa) {
                    "ابزارهای تهاجمی تست نفوذ وب (مانند SQLMap) در این برنامه وجود ندارند و فقط بررسی‌های غیرتهاجمی انجام می‌شود."
                } else {
                    "Only passive and non-destructive inspections are performed; offensive web attack tools are excluded."
                }
            )

            ModuleCategory.OSINT -> HelpTopic(
                id = "cat_osint",
                title = SwordStrings.categoryTitle(category, languageCode),
                whatItDoes = if (fa) {
                    "اطلاعات عمومی ثبت دامنه (RDAP)، مشخصات شبکه و کشور IP، تنظیمات ضدجعل ایمیل (SPF/DMARC) و وجود نام کاربری در سایت‌های عمومی را بررسی می‌کند."
                } else {
                    "Looks up public domain registration (RDAP), IP ASN/ISP details, email anti-spoofing DNS records (SPF/DMARC), and public username availability."
                },
                whenToUse = if (fa) {
                    "برای بررسی اعتبار دامنه، مالکیت شبکه یک IP یا بررسی تکراری نبودن نام کاربری قبل از ثبت حساب."
                } else {
                    "Helpful when checking who hosts a domain/IP, auditing your email DNS posture, or checking username availability."
                },
                prerequisites = if (fa) {
                    "اتصال اینترنت لازم است. بررسی نام کاربری از موتور پایتون داخلی برنامه (با محدودیت ۵ تا ۵۰ سایت) استفاده می‌کند."
                } else {
                    "Requires internet access. Username checks run via Sword's built-in Python 3.10 engine (capped at 5–50 sites)."
                },
                securityNote = if (fa) {
                    "تمامی استعلام‌ها به‌صورت رمزگذاری‌شده (HTTPS) انجام می‌شوند و فقط داده‌های عمومی را می‌خوانند."
                } else {
                    "All lookups use encrypted HTTPS and read strictly public registration or profile information."
                }
            )

            ModuleCategory.WIRELESS -> HelpTopic(
                id = "cat_wireless",
                title = SwordStrings.categoryTitle(category, languageCode),
                whatItDoes = if (fa) {
                    "جزئیات وای‌فای فعلی (فرکانس، سرعت لینک، قدرت سیگنال) و شبکه‌های وای‌فای اطراف (از جمله هشدار شبکه‌های باز یا ناامن WEP) را نمایش می‌دهد."
                } else {
                    "Shows details about your active Wi-Fi connection (speed, band, signal strength) and scans nearby Wi-Fi networks for weak or open security."
                },
                whenToUse = if (fa) {
                    "پیش از اتصال به وای‌فای عمومی یا برای بررسی کیفیت و نوع رمزنگاری روتر خودتان."
                } else {
                    "Use before connecting to public Wi-Fi or to check your router's signal strength and encryption type."
                },
                prerequisites = if (fa) {
                    "وای‌فای گوشی و GPS/Location باید روشن باشند و مجوز مکان یا Wi-Fi نزدیک داده شده باشد."
                } else {
                    "Requires Wi-Fi enabled, Location Services turned on, and Nearby Wi-Fi / Location permission granted."
                },
                securityNote = if (fa) {
                    "اندروید اسکن وای‌فای را به ۴ بار در هر ۲ دقیقه محدود می‌کند. حمله قطع اتصال وای‌فای (Deauth) در Sword وجود ندارد."
                } else {
                    "Android limits Wi-Fi scans to 4 times every 2 minutes. Packet injection and deauth attacks are unsupported and excluded."
                }
            )

            ModuleCategory.CRYPTOGRAPHY -> HelpTopic(
                id = "cat_crypto",
                title = SwordStrings.categoryTitle(category, languageCode),
                whatItDoes = if (fa) {
                    "هش‌های رمزنگاری SHA-256 و SHA-512 می‌سازد، تطابق هش را بررسی می‌کند، متن را به Base64/Hex/URL/ROT13 تبدیل می‌کند و رمز/توکن تصادفی امن تولید می‌کند."
                } else {
                    "Computes SHA-256/512 hashes, verifies checksums in constant time, converts Base64/Hex/URL/ROT13 text, and generates high-entropy random tokens."
                },
                whenToUse = if (fa) {
                    "برای ساخت رمز عبور یا کلید تصادفی قوی، بررسی صحت متن و رمزگشایی کدهای Base64 یا Hex به‌صورت کاملاً آفلاین."
                } else {
                    "Use offline to generate secure random tokens, verify file/text checksums, or decode Base64/Hex strings."
                },
                prerequisites = if (fa) {
                    "کاملاً آفلاین کار می‌کند و نیازی به اینترنت یا مجوز ندارد."
                } else {
                    "Works 100% offline with no permissions required."
                },
                securityNote = if (fa) {
                    "توجه: Base64، Hex و ROT13 رمزگذاری امن نیستند و فقط برای تبدیل قالب متن استفاده می‌شوند."
                } else {
                    "Note: Base64, Hex, URL encoding, and ROT13 are reversible encodings—not encryption for secrets."
                }
            )

            ModuleCategory.FORENSICS -> HelpTopic(
                id = "cat_forensics",
                title = SwordStrings.categoryTitle(category, languageCode),
                whatItDoes = if (fa) {
                    "نوع واقعی فایل را از روی بایت‌های آغازین تشخیص می‌دهد، هش زنجیره نگهداری می‌سازد، موقعیت مکانی (GPS) پنهان در عکس‌ها را پیدا می‌کند و رشته‌های متنی/هگز فایل را استخراج می‌کند."
                } else {
                    "Detects real file types from magic bytes, logs SHA-256 integrity hashes, finds hidden GPS/camera EXIF metadata in photos, and extracts safe text strings/hex."
                },
                whenToUse = if (fa) {
                    "پیش از اشتراک‌گذاری عکس (برای اطمینان از نبود GPS)، یا هنگام بررسی یک فایل مشکوک دانلودشده روی گوشی."
                } else {
                    "Use before sharing photos (to check for hidden GPS tags) or when inspecting a downloaded file's true format and checksum."
                },
                prerequisites = if (fa) {
                    "فایل مستقیماً توسط خود شما از طریق انتخابگر استاندارد اندروید (SAF) انتخاب می‌شود."
                } else {
                    "You choose each file directly via Android's Storage Access Framework picker—no broad storage permission needed."
                },
                securityNote = if (fa) {
                    "برای جلوگیری از کندی گوشی، استخراج متن و هگز حداکثر روی ۲۵۶ کیلوبایت اول فایل انجام می‌شود."
                } else {
                    "Memory-bounded inspection caps strings/hex extraction at the first 256 KB to keep your phone responsive."
                }
            )

            ModuleCategory.CUSTOM_TOOLS -> HelpTopic(
                id = "cat_custom",
                title = SwordStrings.categoryTitle(category, languageCode),
                whatItDoes = if (fa) {
                    "چند بررسی مرتبط را با هم در یک گزارش ترکیب می‌کند (مانند بررسی کامل یک وب‌سایت یا پروفایل دامنه) و امکان آزمایش پل پایتون داخلی برنامه را فراهم می‌کند."
                } else {
                    "Combines multiple checks into one preset report (Full Website Audit, Domain OSINT Profile) and lets you test the sandboxed Python 3.10 runtime."
                },
                whenToUse = if (fa) {
                    "وقتی می‌خواهید به‌جای اجرای چند ابزار جداگانه، همه بررسی‌های اصلی یک دامنه را یک‌جا انجام دهید."
                } else {
                    "Use when you want a combined multi-step check for a domain or website in one report."
                },
                prerequisites = if (fa) {
                    "پریست‌های وب و دامنه به اینترنت نیاز دارند؛ پل پایتون به‌صورت آفلاین داخل APK اجرا می‌شود."
                } else {
                    "Web and OSINT presets require internet; the Python Bridge check works locally inside the APK."
                },
                securityNote = if (fa) {
                    "پل پایتون فقط توابع از پیش بررسی‌شده را اجرا می‌کند و امکان اجرای کد دلخواه یا دستورات شل وجود ندارد."
                } else {
                    "The Python Bridge runs only pre-audited stdlib helpers—no arbitrary script execution or shell access."
                }
            )

            else -> forKey("home_overview", languageCode)
        }
    }

    fun forTool(tool: ToolDescriptor, languageCode: String): HelpTopic {
        val fa = languageCode == "fa"
        val catHelp = forCategory(tool.category, languageCode)
        val prereqParts = mutableListOf<String>()
        if (tool.requiresNetwork) {
            prereqParts += if (fa) "نیازمند اتصال اینترنت" else "Requires an active network connection"
        } else {
            prereqParts += if (fa) "قابل اجرا به‌صورت آفلاین" else "Can run offline"
        }
        if (tool.requiredPermissions.isNotEmpty() || tool.category == ModuleCategory.WIRELESS) {
            prereqParts += if (fa) "ممکن است به مجوز مکان یا وای‌فای نیاز داشته باشد" else "May require Wi-Fi / Location permission"
        }
        if (tool.parameters.any { it.type == com.sword.security.android.core.model.ParameterType.FILE_URI }) {
            prereqParts += if (fa) "انتخاب فایل از طریق انتخابگر اندروید" else "Select a file using Android's file picker"
        }

        val note = buildString {
            if (!tool.staticLimitationReason.isNullOrBlank()) {
                append(tool.staticLimitationReason)
                append(" ")
            }
            if (tool.requiresAuthorizationConfirmation) {
                append(
                    if (fa) "فقط روی سیستم‌ها یا دامنه‌هایی اجرا کنید که مجوز دارید."
                    else "Only run against targets you own or are authorized to test."
                )
            } else {
                append(catHelp.securityNote)
            }
        }

        return HelpTopic(
            id = "tool_${tool.id}",
            title = tool.name,
            whatItDoes = tool.description,
            whenToUse = tool.summary,
            prerequisites = prereqParts.joinToString(if (fa) " · " else " · "),
            securityNote = note.trim()
        )
    }
}
