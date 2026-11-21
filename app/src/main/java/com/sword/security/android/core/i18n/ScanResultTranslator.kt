package com.sword.security.android.core.i18n

import com.sword.security.android.core.model.SecurityFinding
import java.util.Locale

data class TranslatedFindingExplanation(
    val originalTitle: String,
    val originalSeverity: String,
    val simpleTitle: String,
    val whatItMeans: String,
    val whyItMatters: String,
    val potentialImpact: String,
    val recommendedNextSteps: String,
    val preservedIdentifiers: List<String>,
    val hasSufficientContext: Boolean,
    val limitationNote: String? = null
)

data class TranslatedScanSummary(
    val explanationLanguage: String,
    val toolName: String,
    val targetSummary: String,
    val overallPlainSummary: String,
    val findings: List<TranslatedFindingExplanation>,
    val privacyNotice: String,
    val limitationNotice: String? = null
)

object ScanResultTranslator {

    private val cveRegex = Regex("""\bCVE-\d{4}-\d{4,}\b""", RegexOption.IGNORE_CASE)
    private val ipv4Regex = Regex("""\b(?:\d{1,3}\.){3}\d{1,3}(?:/\d{1,2})?\b""")
    private val domainRegex = Regex("""\b(?:[a-zA-Z0-9-]+\.)+(?:com|net|org|io|dev|edu|gov|info|biz|co|ir|de|uk|app|ai|local)\b""", RegexOption.IGNORE_CASE)
    private val tcpPortRegex = Regex("""\bTCP/\d+\b|\bport\s+\d+\b""", RegexOption.IGNORE_CASE)
    private val urlOrPathRegex = Regex("""https?://[^\s,)"']+|/(?:\.well-known/)?[a-zA-Z0-9._-]+(?:/[a-zA-Z0-9._-]+)*""")
    private val tlsProtoRegex = Regex("""\bTLSv?1\.[0-3]\b""", RegexOption.IGNORE_CASE)
    private val httpCodeRegex = Regex("""\bHTTP\s*\d{3}\b""", RegexOption.IGNORE_CASE)
    private val headerOrStandardRegex = Regex(
        """\b(?:Strict-Transport-Security|HSTS|Content-Security-Policy|CSP|X-Frame-Options|frame-ancestors|X-Content-Type-Options|nosniff|Referrer-Policy|SPF|DMARC|DKIM|v=spf1|_dmarc\.[^\s,)"']+|MX|RDAP|ASN|AS\d+|SHA-256|SHA-512|SHA-1|MD5|WEP|WPA2|WPA3|EXIF|GPS|UUID|CSPRNG|Base64(?:URL)?|ROT13)\b"""
    )

    fun extractTechnicalIdentifiers(vararg inputs: String): List<String> {
        val combined = inputs.filter { it.isNotBlank() }.joinToString(" ")
        if (combined.isBlank()) return emptyList()
        val found = linkedSetOf<String>()
        listOf(
            cveRegex,
            ipv4Regex,
            urlOrPathRegex,
            domainRegex,
            tlsProtoRegex,
            httpCodeRegex,
            tcpPortRegex,
            headerOrStandardRegex
        ).forEach { regex ->
            regex.findAll(combined).forEach { match ->
                val token = match.value.trim().trimEnd('.', ',', ';', ')')
                if (token.isNotBlank() && token != "/") {
                    found.add(token)
                }
            }
        }
        return found.toList()
    }

    fun translate(
        toolName: String,
        targetSummary: String,
        findings: List<SecurityFinding>,
        languageCode: String,
        rawOutputAvailable: Boolean = true
    ): TranslatedScanSummary {
        val lang = if (languageCode == "fa") "fa" else "en"
        val isFa = lang == "fa"
        val cleanTool = toolName.ifBlank { if (isFa) "ابزار امنیتی" else "Security Check" }
        val cleanTarget = targetSummary.ifBlank { if (isFa) "هدف مشخص‌نشده" else "Unspecified target" }

        if (findings.isEmpty()) {
            val overall = if (isFa) {
                "ابزار «$cleanTool» روی «$cleanTarget» هیچ یافته ساختاریافته‌ای ثبت نکرده است."
            } else {
                "$cleanTool on $cleanTarget did not record structured findings."
            }
            val limit = if (rawOutputAvailable) {
                if (isFa) {
                    "توضیح خودکار فقط روی یافته‌های ساختاریافته کار می‌کند تا اطلاعات نادرست ساخته نشود. خروجی خام در بخش خلاصه یا JSON قابل مشاهده است."
                } else {
                    "Automatic plain-language explanations use structured findings to avoid guessing. Review the raw output in Summary or JSON."
                }
            } else {
                if (isFa) {
                    "داده‌ای از نتیجه یا یافته‌ها در این گزارش موجود نیست."
                } else {
                    "No finding details or output were available in this report."
                }
            }
            return TranslatedScanSummary(
                explanationLanguage = lang,
                toolName = cleanTool,
                targetSummary = cleanTarget,
                overallPlainSummary = overall,
                findings = emptyList(),
                privacyNotice = SwordStrings.label("explain_privacy_note", lang),
                limitationNotice = limit
            )
        }

        val translatedFindings = findings.map { explainFinding(it, lang) }
        val highOrCriticalCount = findings.count {
            it.severity.equals("Critical", ignoreCase = true) ||
                it.severity.equals("High", ignoreCase = true)
        }
        val mediumCount = findings.count { it.severity.equals("Medium", ignoreCase = true) }

        val overall = if (isFa) {
            when {
                highOrCriticalCount > 0 ->
                    "در بررسی «$cleanTool» روی «$cleanTarget»، تعداد ${findings.size} مورد ثبت شده که $highOrCriticalCount مورد آن‌ها اولویت بالا یا بحرانی دارد و نیاز به بررسی سریع‌تر دارد."
                mediumCount > 0 ->
                    "در بررسی «$cleanTool» روی «$cleanTarget»، تعداد ${findings.size} مورد ثبت شده که $mediumCount مورد آن قابل بهبود با شدت متوسط است."
                else ->
                    "در بررسی «$cleanTool» روی «$cleanTarget»، تعداد ${findings.size} مورد عمدتاً اطلاعاتی یا با خطر پایین ثبت شده است."
            }
        } else {
            when {
                highOrCriticalCount > 0 ->
                    "$cleanTool recorded ${findings.size} finding(s) for $cleanTarget, including $highOrCriticalCount High or Critical item(s) worth reviewing promptly."
                mediumCount > 0 ->
                    "$cleanTool recorded ${findings.size} finding(s) for $cleanTarget, including $mediumCount Medium item(s) where security can be hardened."
                else ->
                    "$cleanTool recorded ${findings.size} finding(s) for $cleanTarget, mostly informational or low-risk."
            }
        }

        return TranslatedScanSummary(
            explanationLanguage = lang,
            toolName = cleanTool,
            targetSummary = cleanTarget,
            overallPlainSummary = overall,
            findings = translatedFindings,
            privacyNotice = SwordStrings.label("explain_privacy_note", lang),
            limitationNotice = if (translatedFindings.any { !it.hasSufficientContext }) {
                if (isFa) {
                    "برای برخی موارد اطلاعات ساختاری کافی در اختیار نبود؛ فقط موارد مستند توضیح داده شده‌اند."
                } else {
                    "Some findings did not match a known rule; only factual recorded fields are shown for those items."
                }
            } else {
                null
            }
        )
    }

    fun explainFinding(
        finding: SecurityFinding,
        languageCode: String
    ): TranslatedFindingExplanation {
        val isFa = languageCode == "fa"
        val title = finding.title.trim()
        val lower = title.lowercase(Locale.US)
        val detail = finding.detail.trim()
        val rec = finding.recommendation.trim()
        val identifiers = extractTechnicalIdentifiers(title, detail, rec)
        val sev = finding.severity.ifBlank { "Info" }

        val rule = matchRule(lower, title, detail, rec, sev, identifiers, isFa)
        if (rule != null) {
            return rule
        }

        // Fallback when no rule matches or details are minimal — NEVER invent vulnerabilities or steps.
        val missingDetail = detail.isBlank()
        val nextSteps = if (rec.isNotBlank()) {
            rec
        } else if (isFa) {
            "گام پیشنهادی مشخصی در داده اصلی گزارش ثبت نشده است. جزئیات فنی بالا را بررسی کنید."
        } else {
            "No automated remediation step was recorded in this finding. Review the factual details above."
        }
        val limitation = if (title.isBlank() || missingDetail) {
            if (isFa) {
                "داده اصلی این یافته برای توضیح کامل کافی نیست؛ Sword اطلاعات حدسی اضافه نمی‌کند."
            } else {
                "This finding lacks enough recorded detail for a fuller beginner explanation; Sword will not guess missing information."
            }
        } else {
            if (isFa) {
                "این یافته دارای عنوان تخصصی یا سفارشی است و توضیح ساده آن بر اساس متن واقعی ثبت‌شده توسط ابزار نمایش داده شده است."
            } else {
                "This finding does not match a predefined beginner template; showing strictly the factual details recorded by the tool."
            }
        }

        return TranslatedFindingExplanation(
            originalTitle = title.ifBlank { if (isFa) "(بدون عنوان)" else "(Untitled finding)" },
            originalSeverity = sev,
            simpleTitle = title.ifBlank { if (isFa) "یافته ثبت‌شده" else "Recorded finding" },
            whatItMeans = detail.ifBlank {
                if (isFa) "توضیح متنی بیشتری در داده اصلی این یافته ثبت نشده است." else "No further detail text was recorded for this finding."
            },
            whyItMatters = if (isFa) {
                "این مورد توسط ابزار با سطح اهمیت «$sev» ثبت شده است و می‌تواند وضعیت واقعی هدف را نشان دهد."
            } else {
                "Recorded with severity '$sev' to reflect an observed condition on the inspected target."
            },
            potentialImpact = impactSummaryForSeverity(sev, isFa),
            recommendedNextSteps = nextSteps,
            preservedIdentifiers = identifiers,
            hasSufficientContext = false,
            limitationNote = limitation
        )
    }

    private fun matchRule(
        lower: String,
        title: String,
        detail: String,
        rec: String,
        sev: String,
        identifiers: List<String>,
        isFa: Boolean
    ): TranslatedFindingExplanation? {
        fun build(
            simpleTitleEn: String,
            simpleTitleFa: String,
            meansEn: String,
            meansFa: String,
            whyEn: String,
            whyFa: String,
            impactEn: String,
            impactFa: String
        ): TranslatedFindingExplanation {
            val factualDetail = if (detail.isNotBlank()) {
                if (isFa) "\nجزئیات ثبت‌شده: $detail" else "\nRecorded detail: $detail"
            } else {
                ""
            }
            val nextSteps = if (rec.isNotBlank()) {
                rec
            } else if (isFa) {
                "نیاز به اقدام فوری ثبت نشده است؛ تنظیمات را طبق استاندارد امنیتی خود بازبینی کنید."
            } else {
                "No specific remediation action was recorded; review against your security baseline."
            }
            return TranslatedFindingExplanation(
                originalTitle = title,
                originalSeverity = sev,
                simpleTitle = if (isFa) simpleTitleFa else simpleTitleEn,
                whatItMeans = (if (isFa) meansFa else meansEn) + factualDetail,
                whyItMatters = if (isFa) whyFa else whyEn,
                potentialImpact = if (isFa) impactFa else impactEn,
                recommendedNextSteps = nextSteps,
                preservedIdentifiers = identifiers,
                hasSufficientContext = true,
                limitationNote = null
            )
        }

        return when {
            "not enforcing https" in lower -> build(
                simpleTitleEn = "Website does not enforce HTTPS encryption",
                simpleTitleFa = "وب‌سایت از اتصال رمزگذاری‌شده HTTPS استفاده نمی‌کند",
                meansEn = "The inspected address responded over plain HTTP instead of redirecting to encrypted HTTPS.",
                meansFa = "آدرس بررسی‌شده به جای هدایت اجباری به HTTPS، از اتصال رمزگذاری‌نشده HTTP پاسخ داده است.",
                whyEn = "Without HTTPS, data sent between a visitor's browser and the server travels in plain text.",
                whyFa = "بدون HTTPS، اطلاعات ردوبدل‌شده بین مرورگر کاربر و سرور به‌صورت متن ساده در شبکه منتقل می‌شود.",
                impactEn = "Anyone on the same Wi-Fi or network path could read or tamper with page content or logins.",
                impactFa = "افراد روی یک وای‌فای مشترک یا مسیر شبکه ممکن است بتوانند محتوای صفحه یا اطلاعات ورود را ببینند یا تغییر دهند."
            )

            "strict-transport-security" in lower || "hsts" in lower -> build(
                simpleTitleEn = "Website is missing HSTS protection",
                simpleTitleFa = "سربرگ امنیتی HSTS روی وب‌سایت فعال نیست",
                meansEn = "The server did not send the Strict-Transport-Security (HSTS) header.",
                meansFa = "سرور سربرگ Strict-Transport-Security (HSTS) را در پاسخ خود ارسال نکرده است.",
                whyEn = "HSTS tells browsers to always connect using HTTPS automatically, even if a user types http://.",
                whyFa = "سربرگ HSTS به مرورگر دستور می‌دهد همیشه به‌طور خودکار فقط از HTTPS استفاده کند.",
                impactEn = "First-time visitors could briefly connect over unencrypted HTTP before a redirect happens.",
                impactFa = "کاربران ممکن است در اولین لحظه اتصال پیش از ریدایرکت، از اتصال ناامن HTTP عبور کنند."
            )

            "content-security-policy" in lower || "(csp)" in lower -> build(
                simpleTitleEn = "Website is missing Content-Security-Policy (CSP)",
                simpleTitleFa = "سیاست امنیتی Content-Security-Policy (CSP) تنظیم نشده است",
                meansEn = "The response does not include a Content-Security-Policy (CSP) rule restricting scripts and resources.",
                meansFa = "سایت سربرگ Content-Security-Policy (CSP) برای محدود کردن اسکریپت‌ها و منابع مجاز ندارد.",
                whyEn = "CSP acts as a safety guard inside the browser so unauthorized scripts cannot easily run.",
                whyFa = "CSP مانند یک دیوار دفاعی در مرورگر است که جلوی اجرای کدهای ناخواسته را می‌گیرد.",
                impactEn = "If the site ever has a script injection bug (XSS), there is no CSP browser barrier to contain it.",
                impactFa = "در صورت وجود باگ تزریق اسکریپت (XSS) در سایت، مرورگر مانع اضافه برای مهار آن نخواهد داشت."
            )

            "clickjacking" in lower -> build(
                simpleTitleEn = "Missing clickjacking protection (X-Frame-Options)",
                simpleTitleFa = "محافظت در برابر کلیک‌جکینگ (X-Frame-Options) وجود ندارد",
                meansEn = "Neither X-Frame-Options nor CSP frame-ancestors is set to block embedding this page inside another site's frame.",
                meansFa = "نه X-Frame-Options و نه CSP frame-ancestors برای جلوگیری از قرارگیری صفحه داخل iframe سایت‌های دیگر تنظیم نشده‌اند.",
                whyEn = "Attackers can hide a clickable page inside an invisible frame to trick users into clicking unintended buttons.",
                whyFa = "سایت‌های مخرب می‌توانند صفحه را داخل یک قاب پنهان قرار دهند تا کاربر ناخواسته روی دکمه‌ای کلیک کند.",
                impactEn = "Low to medium UI-redressing risk on interactive or logged-in pages.",
                impactFa = "خطر فریب کلیک (Clickjacking) روی صفحات حساس یا دارای حساب کاربری افزایش می‌یابد."
            )

            "x-content-type-options" in lower || "nosniff" in lower -> build(
                simpleTitleEn = "Missing X-Content-Type-Options: nosniff header",
                simpleTitleFa = "سربرگ X-Content-Type-Options: nosniff وجود ندارد",
                meansEn = "The server did not instruct browsers to strictly follow the declared file Content-Type (`nosniff`).",
                meansFa = "سرور دستور `nosniff` را نفرستاده تا مرورگر نوع فایل‌ها را دقیقاً طبق اعلام سرور تفسیر کند.",
                whyEn = "Without `nosniff`, older browsers may guess a file's type and accidentally execute harmless uploads as scripts.",
                whyFa = "بدون `nosniff`، مرورگر ممکن است نوع یک فایل را حدس بزند و محتوای متنی را به‌عنوان اسکریپت اجرا کند.",
                impactEn = "Low risk of MIME-sniffing confusion on sites hosting user-uploaded content.",
                impactFa = "احتمال خطای تشخیص نوع فایل (MIME-sniffing) به‌خصوص در سایت‌های دارای فایل آپلودی کاربران."
            )

            "referrer-policy" in lower -> build(
                simpleTitleEn = "Missing Referrer-Policy privacy header",
                simpleTitleFa = "سربرگ حریم خصوصی Referrer-Policy تنظیم نشده است",
                meansEn = "The website does not explicitly specify how much page URL information is shared when clicking external links via Referrer-Policy.",
                meansFa = "سایت مشخص نکرده هنگام کلیک کاربر روی لینک‌های خروجی، چقدر از آدرس صفحه مبدا از طریق Referrer-Policy ارسال شود.",
                whyEn = "Default browser behavior may share page paths with third-party websites.",
                whyFa = "تنظیم نبودن آن می‌تواند مسیر آدرس‌های داخلی صفحه را به سایت‌های دیگر ارسال کند.",
                impactEn = "Informational privacy hygiene item; low impact unless sensitive tokens appear in URLs.",
                impactFa = "نکته بهداشت حریم خصوصی با ریسک پایین (مگر اینکه اطلاعات حساس در URL قرار گیرد)."
            )

            "server banner disclosed" in lower -> build(
                simpleTitleEn = "Web server software name is visible in headers",
                simpleTitleFa = "نام یا نسخه سرور در پاسخ وب نمایش داده می‌شود",
                meansEn = "The HTTP `Server` header advertises details about the web server software.",
                meansFa = "سربرگ `Server` نام یا مشخصات نرم‌افزار وب‌سرور را نشان می‌دهد.",
                whyEn = "This alone is not a vulnerability, though hiding unnecessary version numbers reduces automated recon.",
                whyFa = "این مورد به‌تنهایی حفره امنیتی نیست، اما پنهان کردن نسخه‌های دقیق کار شناسایی خودکار را سخت‌تر می‌کند.",
                impactEn = "Informational exposure only.",
                impactFa = "فقط افشای اطلاعات عمومی سرور (اطلاع‌رسانی)."
            )

            "cookie missing security attributes" in lower -> build(
                simpleTitleEn = "A website cookie is missing security flags",
                simpleTitleFa = "یکی از کوکی‌های سایت پرچم‌های امنیتی لازم را ندارد",
                meansEn = "A `Set-Cookie` header lacks `Secure`, `HttpOnly`, or `SameSite` protections.",
                meansFa = "کوکی ارسال‌شده توسط سایت فاقد یکی از تنظیمات `Secure`، `HttpOnly` یا `SameSite` است.",
                whyEn = "Security flags keep session cookies encrypted over HTTPS, hidden from browser scripts, and safer against forged requests.",
                whyFa = "این پرچم‌ها مانع خوانده شدن کوکی توسط اسکریپت‌ها یا ارسال روی اتصال ناامن می‌شوند.",
                impactEn = "If the cookie stores a login session, missing flags raise session hijacking or CSRF risk.",
                impactFa = "اگر کوکی مربوط به ورود کاربر باشد، ریسک سرقت نشست یا درخواست‌های جعلی افزایش می‌یابد."
            )

            "strong baseline security headers present" in lower -> build(
                simpleTitleEn = "Good baseline web security headers detected",
                simpleTitleFa = "سربرگ‌های امنیتی پایه وب‌سایت به‌خوبی تنظیم شده‌اند",
                meansEn = "The audited URL enforces HTTPS along with core headers like HSTS, CSP, clickjacking protection, and nosniff.",
                meansFa = "آدرس بررسی‌شده از HTTPS و سربرگ‌های مهمی مثل HSTS، CSP و nosniff پشتیبانی می‌کند.",
                whyEn = "These browser headers protect visitors against downgrade attacks, injected scripts, and framing tricks.",
                whyFa = "این تنظیمات از بازدیدکنندگان در برابر تزریق اسکریپت و شنود محافظت می‌کنند.",
                impactEn = "Positive security finding — no weakness indicated here.",
                impactFa = "یافته مثبت و مطلوب — نشان‌دهنده وضعیت مناسب است."
            )

            "tls hostname verification mismatch" in lower -> build(
                simpleTitleEn = "HTTPS certificate does not match the website domain",
                simpleTitleFa = "گواهی HTTPS با نام دامنه مطابقت ندارد",
                meansEn = "The server's TLS certificate names do not match the hostname inspected.",
                meansFa = "نام‌های ثبت‌شده در گواهی امنیتی سرور با دامنه بررسی‌شده یکسان نیستند.",
                whyEn = "Browsers use hostname verification to prove you are talking to the real site and not an impostor.",
                whyFa = "مرورگر از این تطابق مطمئن می‌شود که به سرور واقعی متصل شده است و نه سرور جعلی.",
                impactEn = "Critical trust failure: visitors see security warnings and connections could be intercepted or misrouted.",
                impactFa = "خطر بحرانی اعتماد: کاربران با خطای امنیتی روبه‌رو می‌شوند و امکان جعل یا شنود وجود دارد."
            )

            "expired tls certificate" in lower -> build(
                simpleTitleEn = "Website HTTPS certificate has expired",
                simpleTitleFa = "تاریخ اعتبار گواهی HTTPS سایت منقضی شده است",
                meansEn = "The X.509 certificate validity end date is in the past.",
                meansFa = "تاریخ انقضای گواهی امنیتی (TLS/X.509) سایت به پایان رسیده است.",
                whyEn = "Expired certificates cause browsers and apps to reject or block HTTPS connections.",
                whyFa = "گواهی منقضی‌شده باعث می‌شود مرورگرها اتصال به سایت را ناامن تشخیص دهند و مسدود کنند.",
                impactEn = "Critical availability and trust issue; users cannot safely verify the website.",
                impactFa = "اختلال بحرانی در دسترسی کاربران و کاهش شدید امنیت و اعتماد."
            )

            "tls certificate expiring soon" in lower -> build(
                simpleTitleEn = "HTTPS certificate will expire soon",
                simpleTitleFa = "گواهی HTTPS به‌زودی منقضی می‌شود",
                meansEn = "The site's TLS certificate is still valid today but expires in fewer than 14 days.",
                meansFa = "گواهی امنیتی فعلاً معتبر است اما کمتر از ۱۴ روز تا انقضای آن باقی مانده است.",
                whyEn = "Renewing before expiration prevents sudden website outages and browser warning screens.",
                whyFa = "تمدید به‌موقع مانع از قطع ناگهانی دسترسی امن سایت می‌شود.",
                impactEn = "Medium operational risk if automatic renewal fails.",
                impactFa = "ریسک متوسط از کار افتادن HTTPS در صورت تمدید نشدن به موقع."
            )

            "legacy tls protocol negotiated" in lower -> build(
                simpleTitleEn = "Server uses an outdated TLS encryption version",
                simpleTitleFa = "سرور از نسخه قدیمی رمزنگاری TLS استفاده می‌کند",
                meansEn = "The handshake negotiated an older TLS version instead of modern TLSv1.2 or TLSv1.3.",
                meansFa = "ارتباط رمزنگاری‌شده با نسخه قدیمی TLS (پایین‌تر از TLSv1.2 / TLSv1.3) برقرار شده است.",
                whyEn = "Older TLS 1.0/1.1 protocols have known cryptographic weaknesses and are deprecated.",
                whyFa = "نسخه‌های قدیمی‌تر مثل TLS 1.0 و 1.1 دارای ضعف‌های شناخته‌شده امنیتی هستند.",
                impactEn = "High cryptographic risk; older ciphers are easier to attack and blocked by modern clients.",
                impactFa = "ریسک بالای رمزنگاری و عدم سازگاری با مرورگرهای امروزی."
            )

            "weak certificate signature algorithm" in lower -> build(
                simpleTitleEn = "Certificate uses a weak SHA-1 or MD5 signature",
                simpleTitleFa = "گواهی امنیتی از امضای ضعیف (SHA-1 یا MD5) استفاده می‌کند",
                meansEn = "The certificate is signed using deprecated SHA-1 or MD5 hashing.",
                meansFa = "الگوریتم امضای گواهی بر پایه روش قدیمی SHA-1 یا MD5 است.",
                whyEn = "MD5 and SHA-1 are vulnerable to collision attacks; modern certificates must use SHA-256 or stronger.",
                whyFa = "روش‌های MD5 و SHA-1 دیگر ایمن نیستند و باید با SHA-256 یا قوی‌تر جایگزین شوند.",
                impactEn = "High cryptographic trust risk; modern browsers reject SHA-1/MD5 certificates.",
                impactFa = "خطر بالای اعتبار؛ مرورگرهای مدرن این گواهی‌ها را معتبر نمی‌دانند."
            )

            "valid tls configuration" in lower -> build(
                simpleTitleEn = "HTTPS certificate and TLS encryption look healthy",
                simpleTitleFa = "گواهی HTTPS و تنظیمات TLS سالم و معتبر است",
                meansEn = "The certificate matches the domain, has not expired, and uses modern TLS encryption.",
                meansFa = "گواهی با دامنه مطابقت دارد، منقضی نشده است و از نسخه مدرن TLS استفاده می‌کند.",
                whyEn = "Healthy TLS protects data in transit and verifies the identity of the server.",
                whyFa = "TLS سالم امنیت داده‌ها را در مسیر انتقال و اصالت سرور را تضمین می‌کند.",
                impactEn = "Positive security finding.",
                impactFa = "یافته مثبت و مطلوب."
            )

            "security.txt published" in lower -> build(
                simpleTitleEn = "Security contact policy (security.txt) is published",
                simpleTitleFa = "فایل تماس امنیتی (security.txt) روی سایت منتشر شده است",
                meansEn = "The site publishes `/.well-known/security.txt` (RFC 9116) so researchers know how to report bugs safely.",
                meansFa = "سایت فایل `/.well-known/security.txt` را برای اعلام روش تماس گزارش باگ‌های امنیتی دارد.",
                whyEn = "Makes responsible vulnerability disclosure straightforward.",
                whyFa = "باعث می‌شود گزارش‌دهی مسئولانه مشکلات امنیتی ساده‌تر انجام شود.",
                impactEn = "Positive defensive transparency item.",
                impactFa = "نکته مثبت و حرفه‌ای (اطلاع‌رسانی)."
            )

            "no /.well-known/security.txt found" in lower -> build(
                simpleTitleEn = "No security.txt contact file found",
                simpleTitleFa = "فایل تماس امنیتی security.txt پیدا نشد",
                meansEn = "The site does not publish `/.well-known/security.txt`.",
                meansFa = "مسیر `/.well-known/security.txt` روی این دامنه وجود ندارد.",
                whyEn = "Having a `security.txt` file is an optional best practice so security contacts are easy to reach.",
                whyFa = "داشتن این فایل یک استاندارد اختیاری است تا پژوهشگران بدانند به کجا پیام بدهند.",
                impactEn = "Low informational finding; not a security vulnerability by itself.",
                impactFa = "مورد اختیاری با اهمیت پایین؛ به تنهایی ضعف امنیتی محسوب نمی‌شود."
            )

            "sensitive path hints in robots.txt" in lower -> build(
                simpleTitleEn = "robots.txt lists potentially sensitive folder names",
                simpleTitleFa = "فایل robots.txt به مسیرهای حساس اشاره کرده است",
                meansEn = "Public entries inside `/robots.txt` mention words like admin, backup, login, or api.",
                meansFa = "فایل عمومی `/robots.txt` مسیرهایی شامل کلماتی مثل admin یا backup را لیست کرده است.",
                whyEn = "`robots.txt` only asks search engines not to index pages—it does not block anyone from visiting them.",
                whyFa = "فایل `robots.txt` فقط به موتورهای جستجو می‌گوید چه صفحه‌ای نمایش داده نشود، اما جلوی دسترسی افراد را نمی‌گیرد.",
                impactEn = "Low information disclosure if those listed paths lack proper login protection.",
                impactFa = "افشای نام مسیرهای داخلی؛ در صورتی که آن صفحات قفل و احراز هویت درست نداشته باشند ریسک ایجاد می‌کند."
            )

            "open tcp port" in lower -> build(
                simpleTitleEn = "An open network service port was found",
                simpleTitleFa = "یک درگاه (پورت) شبکه باز روی هدف پیدا شد",
                meansEn = "The target accepted a TCP connection on the listed port.",
                meansFa = "دستگاه هدف روی پورت مشخص‌شده به اتصال شبکه پاسخ داد و سرویس آن فعال است.",
                whyEn = "Open ports show which services (like web HTTP/HTTPS or remote login) are listening.",
                whyFa = "پورت‌های باز نشان می‌دهند کدام سرویس‌ها روی شبکه در حال گوش دادن هستند.",
                impactEn = "Expected services like 443 (HTTPS) are normal, while exposed administrative ports (like 22/SSH or 3389) should be restricted.",
                impactFa = "باز بودن پورت وب مثل 443 عادی است، اما باز بودن سرویس‌های مدیریتی برای همه ممکن است ریسک حمله ایجاد کند."
            )

            "no open ports in tested set" in lower -> build(
                simpleTitleEn = "None of the tested TCP ports were open",
                simpleTitleFa = "هیچ‌کدام از پورت‌های بررسی‌شده باز نبودند",
                meansEn = "All ports in the tested list timed out or refused connections.",
                meansFa = "پورت‌های آزمایش‌شده یا بسته بودند یا توسط فایروال مسدود شده بودند.",
                whyEn = "Confirms no checked ports are exposed to the test device.",
                whyFa = "نشان می‌دهد سرویس‌های بررسی‌شده در دسترس اتصال از بیرون نیستند.",
                impactEn = "Informational / low exposure on the tested port list.",
                impactFa = "اطلاعاتی؛ سطح دسترسی شبکه محدود است."
            )

            "target tcp port" in lower && "unreachable" in lower -> build(
                simpleTitleEn = "Target port could not be reached",
                simpleTitleFa = "اتصال به پورت مورد نظر برقرار نشد",
                meansEn = "Sword could not complete a TCP connection to the specified host and port.",
                meansFa = "برنامه نتوانست به هاست و پورت مشخص‌شده متصل شود.",
                whyEn = "Helpful to check whether a service is offline or blocked by a firewall or Wi-Fi network.",
                whyFa = "نشان می‌دهد سرویس خاموش است یا توسط فایروال و شبکه مسدود شده است.",
                impactEn = "Connectivity check only (low informational note).",
                impactFa = "فقط نتیجه تست اتصال است."
            )

            "host reachable via tcp" in lower || "dns-over-https lookup completed" in lower || "active non-loopback interfaces" in lower || "responsive host(s)" in lower -> build(
                simpleTitleEn = "Network diagnostic completed cleanly",
                simpleTitleFa = "بررسی شبکه با موفقیت انجام شد",
                meansEn = "The network check gathered live connection, DNS, latency, or LAN host information.",
                meansFa = "ابزار مشخصات واقعی اتصال، DNS، سرعت پاسخ یا دستگاه‌های شبکه محلی را ثبت کرد.",
                whyEn = "Helps verify how your device connects and which hosts or DNS records are active.",
                whyFa = "به عیب‌یابی شبکه، تشخیص دستگاه‌های متصل به وای‌فای خودتان و بررسی DNS کمک می‌کند.",
                impactEn = "Informational diagnostic result.",
                impactFa = "گزارش اطلاعاتی جهت آگاهی از وضعیت شبکه."
            )

            "missing spf record" in lower -> build(
                simpleTitleEn = "Domain lacks SPF email spoofing protection",
                simpleTitleFa = "دامنه فاقد رکورد SPF برای جلوگیری از جعل ایمیل است",
                meansEn = "No `v=spf1` DNS TXT record was found for the domain.",
                meansFa = "رکورد متنی `v=spf1` در تنظیمات DNS این دامنه یافت نشد.",
                whyEn = "SPF lists which mail servers are legitimately allowed to send email for a domain.",
                whyFa = "رکورد SPF مشخص می‌کند کدام سرورها مجاز به ارسال ایمیل با نام این دامنه هستند.",
                impactEn = "Without SPF, attackers can more easily forge phishing emails pretending to come from the domain.",
                impactFa = "بدون SPF، ارسال ایمیل جعلی (فیشینگ) با نام دامنه برای مهاجمان ساده‌تر می‌شود."
            )

            "missing dmarc policy" in lower -> build(
                simpleTitleEn = "Domain lacks a DMARC anti-spoofing policy",
                simpleTitleFa = "دامنه فاقد سیاست امنیتی DMARC برای ایمیل است",
                meansEn = "No DMARC TXT policy was found at `_dmarc.<domain>`.",
                meansFa = "رکورد DMARC در زیردامنه `_dmarc` این دامنه تنظیم نشده است.",
                whyEn = "DMARC tells receiving mail providers how to quarantine or reject fake emails.",
                whyFa = "DMARC به سرویس‌های ایمیل می‌گوید اگر ایمیلی جعلی بود، آن را مسدود یا به اسپم منتقل کنند.",
                impactEn = "Medium email spoofing risk: forged emails may reach inboxes instead of being blocked.",
                impactFa = "ریسک متوسط جعل ایمیل با نام دامنه شما."
            )

            "spf & dmarc records configured" in lower -> build(
                simpleTitleEn = "Domain email spoofing protections (SPF & DMARC) are set",
                simpleTitleFa = "تنظیمات ضدجعل ایمیل (SPF و DMARC) فعال هستند",
                meansEn = "Both `v=spf1` and `_dmarc` DNS records were observed for the domain.",
                meansFa = "هر دو رکورد SPF و DMARC برای این دامنه تنظیم شده‌اند.",
                whyEn = "Helps email services block forged phishing emails claiming to be from this domain.",
                whyFa = "به جلوگیری از سوءاستفاده از نام دامنه در ایمیل‌های جعلی کمک می‌کند.",
                impactEn = "Positive defensive finding.",
                impactFa = "یافته مثبت و مطلوب."
            )

            "domain transfer lock not observed" in lower -> build(
                simpleTitleEn = "Domain transfer lock status was not visible in RDAP",
                simpleTitleFa = "وضعیت قفل انتقال دامنه در اطلاعات RDAP دیده نشد",
                meansEn = "The public RDAP status list did not show `clientTransferProhibited`.",
                meansFa = "در پاسخ عمومی RDAP عبارت `clientTransferProhibited` مشاهده نشد.",
                whyEn = "Registrar transfer locks add extra protection against unauthorized domain hijacking.",
                whyFa = "قفل انتقال در شرکت ثبت‌کننده دامنه جلوی انتقال غیرمجاز دامنه را می‌گیرد.",
                impactEn = "Low domain administration hardening item.",
                impactFa = "نکته مدیریتی با ریسک پایین برای محافظت بیشتر از مالکیت دامنه."
            )

            "rdap domain record retrieved" in lower || "public ip & asn mapped" in lower || "public platforms" in lower -> build(
                simpleTitleEn = "Public OSINT lookup summary",
                simpleTitleFa = "خلاصه استعلام اطلاعات عمومی (OSINT)",
                meansEn = "Sword collected publicly registered domain, IP network (ASN), or username availability details over HTTPS.",
                meansFa = "برنامه داده‌های کاملاً عمومی دامنه، شبکه IP یا وضعیت نام کاربری را از طریق HTTPS بررسی کرد.",
                whyEn = "Useful for verifying who operates an IP/domain or checking username availability before registering.",
                whyFa = "برای شناخت مالک شبکه، وضعیت ثبت دامنه یا بررسی در دسترس بودن نام کاربری کاربرد دارد.",
                impactEn = "Informational lookup — no security flaw by itself.",
                impactFa = "نتیجه اطلاعاتی است و به تنهایی به معنی آسیب‌پذیری نیست."
            )

            "legacy wep network detected" in lower -> build(
                simpleTitleEn = "Nearby Wi-Fi network uses broken WEP security",
                simpleTitleFa = "یک شبکه وای‌فای نزدیک از رمزنگاری قدیمی و ناامن WEP استفاده می‌کند",
                meansEn = "At least one nearby Wi-Fi router is using outdated WEP encryption.",
                meansFa = "حداقل یک روتر وای‌فای اطراف از استاندارد بسیار قدیمی WEP استفاده می‌کند.",
                whyEn = "WEP encryption was broken years ago and can be cracked quickly.",
                whyFa = "رمزنگاری WEP کاملاً منسوخ شده و به‌سادگی قابل شکستن است.",
                impactEn = "High wireless security risk if you connect to that network; traffic and passwords can be exposed.",
                impactFa = "ریسک بالای شنود در صورت اتصال به این وای‌فای؛ بهتر است از WPA2 یا WPA3 استفاده شود."
            )

            "open / unencrypted wi-fi" in lower -> build(
                simpleTitleEn = "Open (unencrypted) Wi-Fi network detected nearby",
                simpleTitleFa = "شبکه وای‌فای باز و بدون رمز عبور در اطراف پیدا شد",
                meansEn = "One or more visible Wi-Fi networks have no wireless encryption or password.",
                meansFa = "یک یا چند وای‌فای اطراف بدون رمزنگاری (باز) هستند.",
                whyEn = "On an open Wi-Fi network, local wireless traffic is not encrypted between your phone and the router.",
                whyFa = "در وای‌فای باز، ارتباط بی‌سیم بین گوشی و روتر رمزنگاری نمی‌شود.",
                impactEn = "Medium risk on public Wi-Fi; stick strictly to HTTPS websites or a trusted VPN if you ever connect.",
                impactFa = "در صورت اتصال، از انجام کارهای حساس بدون HTTPS یا VPN مطمئن خودداری کنید."
            )

            "weak wi-fi signal strength" in lower -> build(
                simpleTitleEn = "Wi-Fi signal strength is weak",
                simpleTitleFa = "قدرت سیگنال وای‌فای ضعیف است",
                meansEn = "The measured signal (dBm) is low, which can reduce speed or cause dropouts.",
                meansFa = "میزان سیگنال (dBm) پایین است و ممکن است باعث کندی یا قطع اتصال شود.",
                whyEn = "Moving closer to the router or switching bands often stabilizes the connection.",
                whyFa = "نزدیک‌تر شدن به مودم می‌تواند کیفیت اتصال را بهتر کند.",
                impactEn = "Low connection quality issue only (not a security breach).",
                impactFa = "فقط موضوع کیفیت و سرعت اتصال است."
            )

            "wi-fi identifiers redacted" in lower || "wi-fi connection diagnostics" in lower || "nearby wi-fi" in lower || "nearby wireless network" in lower -> build(
                simpleTitleEn = "Wi-Fi diagnostic summary",
                simpleTitleFa = "گزارش وضعیت اتصال یا شبکه‌های وای‌فای",
                meansEn = "Shows current Wi-Fi link or nearby scan results respecting Android privacy/Location rules.",
                meansFa = "جزئیات اتصال وای‌فای یا شبکه‌های اطراف با رعایت محدودیت‌های حریم خصوصی اندروید ثبت شده است.",
                whyEn = "Helps check encryption type, band speed, or why Android hides SSID names when Location/permission is off.",
                whyFa = "کمک می‌کند نوع رمزنگاری و دلیل نمایش داده نشدن نام وای‌فای (در صورت بسته بودن مجوز یا Location) را متوجه شوید.",
                impactEn = "Informational wireless diagnostic.",
                impactFa = "گزارش اطلاعاتی وضعیت وای‌فای."
            )

            "integrity digest mismatch" in lower -> build(
                simpleTitleEn = "Hash check FAILED — content does not match expected checksum",
                simpleTitleFa = "تطابق هش برقرار نیست — محتوا تغییر کرده یا اشتباه است",
                meansEn = "The computed SHA-256 or SHA-512 hash is different from the expected digest you entered.",
                meansFa = "هش محاسبه‌شده با مقدار مورد انتظار شما مطابقت ندارد.",
                whyEn = "Even a 1-character or 1-bit change produces a completely different cryptographic hash.",
                whyFa = "حتی تغییر یک کاراکتر یا یک بایت، هش خروجی را کاملاً تغییر می‌دهد.",
                impactEn = "High integrity warning: the file or text may be corrupted, altered, or wrong.",
                impactFa = "هشدار بالای یکپارچگی: متن یا فایل ممکن است تغییر کرده، خراب شده یا جعلی باشد."
            )

            "integrity digest verified" in lower || "digests computed" in lower || "encode completed" in lower || "decode completed" in lower || "rot13 completed" in lower || "cryptographic secret" in lower -> build(
                simpleTitleEn = "Offline cryptography utility completed",
                simpleTitleFa = "عملیات رمزنگاری / هش به‌صورت آفلاین انجام شد",
                meansEn = "The offline cryptographic hash, verification, encoding, or CSPRNG token generation succeeded on your device.",
                meansFa = "محاسبه هش، تأیید صحت، تبدیل کدگذاری یا تولید توکن تصادفی امن روی گوشی انجام شد.",
                whyEn = "Supports verifying file integrity or creating strong random secrets without internet.",
                whyFa = "برای بررسی سالم بودن داده‌ها یا ساخت کلیدهای تصادفی قوی بدون نیاز به اینترنت کاربرد دارد.",
                impactEn = "Informational utility result (remember Base64/Hex/ROT13 are reversible encodings, not secret encryption).",
                impactFa = "نتیجه ابزار آفلاین (توجه: Base64، Hex و ROT13 رمزگذاری امن نیستند)."
            )

            "embedded gps location found in image exif" in lower -> build(
                simpleTitleEn = "Photo contains hidden GPS location coordinates",
                simpleTitleFa = "در اطلاعات پنهان عکس (EXIF) مختصات موقعیت GPS وجود دارد",
                meansEn = "The inspected photo's EXIF metadata stores latitude and longitude coordinates showing where it was taken.",
                meansFa = "فایل تصویر حاوی مختصات جغرافیایی (طول و عرض) محل گرفتن عکس در بخش EXIF است.",
                whyEn = "Sharing original photos online can accidentally reveal your home, workplace, or travel location.",
                whyFa = "ارسال عکس اصلی در شبکه‌های اجتماعی می‌تواند آدرس خانه یا محل حضور شما را افشا کند.",
                impactEn = "Medium personal privacy risk if the image is shared publicly without removing EXIF tags.",
                impactFa = "ریسک متوسط حریم خصوصی هنگام انتشار عمومی تصویر."
            )

            "device & software fingerprint in exif" in lower -> build(
                simpleTitleEn = "Photo contains camera model or software tags",
                simpleTitleFa = "عکس شامل نام مدل دوربین یا نرم‌افزار ویرایش در EXIF است",
                meansEn = "EXIF tags reveal the phone/camera model, timestamp, or software used.",
                meansFa = "فایل تصویر مشخصات مدل گوشی/دوربین یا زمان ثبت را در متادیتا نگه داشته است.",
                whyEn = "Useful to notice before sharing anonymous or sensitive photos.",
                whyFa = "پیش از انتشار عکس‌های حساس بهتر است این متادیتا را پاک کنید.",
                impactEn = "Low privacy exposure.",
                impactFa = "افشای محدود اطلاعات دستگاه در عکس."
            )

            "no sensitive exif" in lower -> build(
                simpleTitleEn = "No GPS or device location tags found in image",
                simpleTitleFa = "موقعیت مکانی یا اطلاعات حساس در EXIF عکس پیدا نشد",
                meansEn = "No standard GPS latitude/longitude or device tags were found in the photo.",
                meansFa = "تگ‌های رایج GPS یا مشخصات حساس دستگاه در این تصویر وجود نداشت.",
                whyEn = "Indicates the photo does not carry obvious EXIF location metadata.",
                whyFa = "نشان می‌دهد فایل از نظر متادیتای مکانی رایج تمیز است.",
                impactEn = "Informational positive privacy check.",
                impactFa = "نتیجه مثبت بررسی حریم خصوصی تصویر."
            )

            "executable binary signature detected" in lower -> build(
                simpleTitleEn = "File is a runnable program / executable binary",
                simpleTitleFa = "فایل انتخاب‌شده یک برنامه اجرایی (باینری) است",
                meansEn = "Magic-byte inspection found executable program headers rather than a plain document or photo.",
                meansFa = "بررسی امضای بایت‌های آغازین نشان داد این فایل یک برنامه اجرایی است، نه سند یا عکس معمولی.",
                whyEn = "Disguised executables (for example an app/program renamed as a PDF) are a common trick.",
                whyFa = "برخی فایل‌های مشکوک ظاهر سند دارند اما در باطن کد اجرایی هستند.",
                impactEn = "Medium safety caution: do not install or execute unknown binary files.",
                impactFa = "هشدار ایمنی: فایل‌های اجرایی ناشناس را اجرا یا نصب نکنید."
            )

            "evidence integrity record saved" in lower || "printable string" in lower -> build(
                simpleTitleEn = "File inspection and integrity hash recorded",
                simpleTitleFa = "بررسی فایل و ثبت اثر انگشت SHA-256 انجام شد",
                meansEn = "Sword inspected the selected file safely via Android SAF without modifying it and computed its SHA-256 checksum.",
                meansFa = "برنامه فایل انتخاب‌شده را بدون تغییر بررسی کرد و هش یکتای SHA-256 آن را ثبت نمود.",
                whyEn = "An integrity hash lets you prove later whether the file has changed by even one byte.",
                whyFa = "ثبت هش باعث می‌شود بعداً بتوانید مطمئن شوید فایل دستکاری نشده است.",
                impactEn = "Informational forensics record.",
                impactFa = "ثبت مدرک و بررسی سالم فایل."
            )

            "embedded python bridge executed safely" in lower -> build(
                simpleTitleEn = "Built-in Python 3.10 module test succeeded",
                simpleTitleFa = "اجرای موتور پایتون ۳.۱۰ داخلی با موفقیت انجام شد",
                meansEn = "Sword's sandboxed Python 3.10 runtime (Chaquopy) ran the selected pre-audited check cleanly.",
                meansFa = "موتور پایتون ۳.۱۰ داخل برنامه بدون مشکل دستور از پیش تعریف‌شده را اجرا کرد.",
                whyEn = "Confirms Python-backed features like username availability checks are healthy.",
                whyFa = "نشان می‌دهد قابلیت‌های مبتنی بر پایتون در گوشی شما آماده کار هستند.",
                impactEn = "Informational health check.",
                impactFa = "تست سلامت موتور داخلی."
            )

            else -> null
        }
    }

    private fun impactSummaryForSeverity(severity: String, isFa: Boolean): String {
        return when (severity.uppercase(Locale.US)) {
            "CRITICAL" -> if (isFa) {
                "اولویت بسیار بالا: این مورد می‌تواند بلافاصله باعث اختلال امنیتی یا از دست رفتن اعتماد اتصال شود."
            } else {
                "Critical priority: this condition can directly compromise connection trust or security."
            }
            "HIGH" -> if (isFa) {
                "اولویت بالا: ریسک امنیتی قابل توجهی در داده‌های اصلی گزارش شده است."
            } else {
                "High priority: the recorded finding indicates a notable security or integrity risk."
            }
            "MEDIUM" -> if (isFa) {
                "اولویت متوسط: بهتر است برای افزایش ایمنی برطرف شود."
            } else {
                "Medium priority: worth hardening to reduce exposure."
            }
            "LOW" -> if (isFa) {
                "اولویت پایین: بهبود توصیه‌شده با ریسک محدود."
            } else {
                "Low priority: a minor hardening or informational improvement."
            }
            else -> if (isFa) {
                "سطح اطلاعاتی: برای آگاهی و مستندسازی ثبت شده است."
            } else {
                "Informational level: recorded for visibility and documentation."
            }
        }
    }
}
