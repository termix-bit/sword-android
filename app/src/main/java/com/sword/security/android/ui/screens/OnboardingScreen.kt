package com.sword.security.android.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.sword.security.android.core.i18n.SwordStrings
import com.sword.security.android.ui.components.SwordCard
import com.sword.security.android.ui.components.SwordCircuitBackdrop
import com.sword.security.android.ui.components.SwordLogoBadge
import com.sword.security.android.ui.theme.LocalSwordColors

private data class TutorialStepContent(
    val icon: ImageVector,
    val badge: String,
    val title: String,
    val summary: String,
    val points: List<String>,
    val tip: String? = null
)

@Composable
fun OnboardingScreen(
    languageCode: String = "en",
    isReplay: Boolean = false,
    onSelectLanguage: (String) -> Unit = {},
    onFinishOnboarding: () -> Unit,
    onSkipOnboarding: () -> Unit = onFinishOnboarding
) {
    val swordColors = LocalSwordColors.current
    val isFa = SwordStrings.isPersian(languageCode)
    val layoutDir = if (isFa) LayoutDirection.Rtl else LayoutDirection.Ltr

    // Step 0 = Language selection screen; Steps 1..6 = 6 concise beginner tutorial steps
    var currentStep by rememberSaveable { mutableIntStateOf(0) }
    val tutorialSteps = buildTutorialSteps(isFa)
    val totalTutorialSteps = tutorialSteps.size

    CompositionLocalProvider(LocalLayoutDirection provides layoutDir) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            SwordCircuitBackdrop(modifier = Modifier.padding(innerPadding)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    // Top action bar: step indicator + Skip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (currentStep == 0) {
                                SwordStrings.label("onboarding_welcome", languageCode)
                            } else {
                                if (isFa) {
                                    "مرحله $currentStep از $totalTutorialSteps"
                                } else {
                                    "Step $currentStep of $totalTutorialSteps"
                                }
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = swordColors.accent
                        )
                        TextButton(
                            onClick = onSkipOnboarding,
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                        ) {
                            Text(
                                text = SwordStrings.label("onboarding_skip", languageCode),
                                style = MaterialTheme.typography.labelLarge,
                                color = swordColors.textSecondary
                            )
                        }
                    }

                    if (currentStep > 0) {
                        LinearProgressIndicator(
                            progress = { currentStep.toFloat() / totalTutorialSteps.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                            color = swordColors.accent,
                            trackColor = swordColors.border
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Scrollable central body so smaller phones never clip content
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        if (currentStep == 0) {
                            LanguageSelectionStep(
                                selectedLanguageCode = languageCode,
                                onSelectLanguage = onSelectLanguage
                            )
                        } else {
                            val step = tutorialSteps[currentStep - 1]
                            TutorialStepView(step = step)
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Bottom Navigation: Back + Next / Finish
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentStep > 0) {
                            OutlinedButton(
                                onClick = { currentStep -= 1 },
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 50.dp),
                                border = BorderStroke(1.dp, swordColors.border)
                            ) {
                                Text(
                                    text = SwordStrings.label("onboarding_back", languageCode),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = swordColors.textPrimary
                                )
                            }
                        } else if (isReplay) {
                            OutlinedButton(
                                onClick = onSkipOnboarding,
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 50.dp),
                                border = BorderStroke(1.dp, swordColors.border)
                            ) {
                                Text(
                                    text = SwordStrings.label("close", languageCode),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = swordColors.textPrimary
                                )
                            }
                        }

                        val isLastStep = currentStep == totalTutorialSteps
                        Button(
                            onClick = {
                                if (isLastStep) {
                                    onFinishOnboarding()
                                } else {
                                    currentStep += 1
                                }
                            },
                            modifier = Modifier
                                .weight(1.35f)
                                .defaultMinSize(minHeight = 50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(
                                text = SwordStrings.label(
                                    if (isLastStep) "onboarding_finish" else "onboarding_next",
                                    languageCode
                                ),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LanguageSelectionStep(
    selectedLanguageCode: String,
    onSelectLanguage: (String) -> Unit
) {
    val swordColors = LocalSwordColors.current

    Spacer(Modifier.height(4.dp))
    SwordLogoBadge(size = 64.dp)

    Text(
        text = SwordStrings.label("onboarding_welcome", selectedLanguageCode),
        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
        color = swordColors.textPrimary
    )
    Text(
        text = SwordStrings.label("app_subtitle", selectedLanguageCode),
        style = MaterialTheme.typography.titleMedium,
        color = swordColors.accent
    )

    SwordCard(borderColor = swordColors.accent.copy(alpha = 0.45f)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = null,
                tint = swordColors.accent,
                modifier = Modifier.size(22.dp)
            )
            Column {
                Text(
                    text = SwordStrings.label("onboarding_lang_title", selectedLanguageCode),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = swordColors.textPrimary
                )
                Text(
                    text = SwordStrings.label("onboarding_lang_sub", selectedLanguageCode),
                    style = MaterialTheme.typography.bodyMedium,
                    color = swordColors.textSecondary
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LanguageChoiceCard(
                title = "English",
                subtitle = "LTR • English interface & explanations",
                selected = selectedLanguageCode != "fa",
                onClick = { onSelectLanguage("en") }
            )
            LanguageChoiceCard(
                title = "فارسی (Persian)",
                subtitle = "راست‌به‌چپ (RTL) • رابط و توضیحات فارسی",
                selected = selectedLanguageCode == "fa",
                onClick = { onSelectLanguage("fa") }
            )
        }
    }
}

@Composable
private fun LanguageChoiceCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val swordColors = LocalSwordColors.current
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 60.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else swordColors.elevatedSurface,
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else swordColors.border
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else swordColors.textPrimary
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    } else {
                        swordColors.textSecondary
                    }
                )
            }
            if (selected) {
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun TutorialStepView(step: TutorialStepContent) {
    val swordColors = LocalSwordColors.current
    Spacer(Modifier.height(6.dp))
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = swordColors.accent.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, swordColors.accent.copy(alpha = 0.45f))
    ) {
        Box(
            modifier = Modifier.padding(18.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = step.icon,
                contentDescription = null,
                tint = swordColors.accent,
                modifier = Modifier.size(42.dp)
            )
        }
    }

    Text(
        text = step.badge,
        style = MaterialTheme.typography.labelLarge,
        color = swordColors.accent
    )

    Text(
        text = step.title,
        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
        color = swordColors.textPrimary
    )

    SwordCard(borderColor = swordColors.accent.copy(alpha = 0.4f)) {
        Text(
            text = step.summary,
            style = MaterialTheme.typography.bodyLarge,
            color = swordColors.textPrimary
        )
        Spacer(Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            step.points.forEach { bullet ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.titleMedium,
                        color = swordColors.accent
                    )
                    Text(
                        text = bullet,
                        style = MaterialTheme.typography.bodyMedium,
                        color = swordColors.textSecondary
                    )
                }
            }
        }

        if (!step.tip.isNullOrBlank()) {
            Spacer(Modifier.height(12.dp))
            Surface(
                color = swordColors.elevatedSurface,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, swordColors.accent.copy(alpha = 0.35f))
            ) {
                Text(
                    text = step.tip,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = swordColors.textPrimary
                )
            }
        }
    }
}

private fun buildTutorialSteps(isFa: Boolean): List<TutorialStepContent> = if (isFa) {
    listOf(
        TutorialStepContent(
            icon = Icons.Default.Security,
            badge = "معرفی Sword",
            title = "ابزار ساده و امن برای بررسی‌های روزمره",
            summary = "Sword به شما کمک می‌کند وضعیت امنیت وب‌سایت‌ها، وای‌فای، اتصال شبکه و فایل‌های خود را مستقیماً در اندروید و بدون نیاز به روت بررسی کنید.",
            points = listOf(
                "مناسب هم برای افراد تازه‌کار و هم کاربران فنی.",
                "دارای موتور داخلی پایتون ۳.۱۰ برای برخی تحلیل‌های تخصصی بدون نیاز به نصب اضافی."
            ),
            tip = "فقط وب‌سایت‌ها، شبکه‌ها و دستگاه‌هایی را بررسی کنید که مالک آن هستید یا اجازه دارید."
        ),
        TutorialStepContent(
            icon = Icons.Default.Dashboard,
            badge = "صفحه خانه (Home)",
            title = "شروع فوری از صفحه خانه",
            summary = "در صفحه نخست «خانه»، گزینه‌های «شروع سریع» قرار دارند تا با یک لمس کار را آغاز کنید.",
            points = listOf(
                "بررسی امنیت سایت، گواهی SSL، استعلام دامنه، ساخت هش و بررسی فایل از همین صفحه قابل اجراست.",
                "بخش «آمادگی دستگاه» نشان می‌دهد اینترنت و مجوزهای اختیاری آماده هستند یا خیر."
            )
        ),
        TutorialStepContent(
            icon = Icons.Default.Apps,
            badge = "تب ابزارها (Tools)",
            title = "دسته‌بندی ۷ گانه ابزارها",
            summary = "از نوار پایین، تب «ابزارها» را بزنید تا تمام ماژول‌ها را با جستجو و فیلتر ببینید.",
            points = listOf(
                "ابزارها به دسته‌های شبکه، امنیت وب، اطلاعات عمومی، وای‌فای، رمزنگاری، بررسی فایل و بررسی‌های ترکیبی تقسیم شده‌اند.",
                "در صفحه هر ابزار، توضیح ساده، پیش‌نیازها و دکمه «تنظیم و اجرا» را می‌بینید."
            )
        ),
        TutorialStepContent(
            icon = Icons.Default.Description,
            badge = "نتایج، گزارش‌ها و توضیح ساده",
            title = "مشاهده و فهم آسان نتایج اسکن",
            summary = "نتیجه هر بررسی به‌صورت خودکار در تب «تاریخچه» و تب «گزارش‌ها» ذخیره می‌شود.",
            points = listOf(
                "در نمایشگر گزارش می‌توانید «خلاصه»، کد «JSON»، نسخه «HTML» یا تب جدید «توضیح ساده» را ببینید.",
                "توضیح ساده کاملاً آفلاین روی گوشی ساخته می‌شود تا یافته‌های فنی را بدون تغییر داده‌های اصلی متوجه شوید."
            )
        ),
        TutorialStepContent(
            icon = Icons.AutoMirrored.Filled.MenuBook,
            badge = "تنظیمات و راهنمای کاربر",
            title = "جای دقیق تنظیمات و راهنمای کامل کاربر",
            summary = "برای تغییر ظاهر (تیره/روشن)، زبان یا خواندن راهنمای گام‌به‌گام، به تب «تنظیمات» در نوار پایین بروید.",
            points = listOf(
                "کارت «راهنمای کامل کاربر» با جستجوی فارسی و انگلیسی درست در بالای صفحه تنظیمات قرار دارد.",
                "گزینه «نمایش دوباره راهنمای شروع» نیز در بخش «راهنما و وضعیت دستگاه» در تنظیمات قرار دارد."
            )
        ),
        TutorialStepContent(
            icon = Icons.AutoMirrored.Filled.HelpOutline,
            badge = "راهنمای متنی در لحظه",
            title = "هر جا سوالی داشتید، علامت ؟ را بزنید",
            summary = "کنار تیتر صفحات و تنظیمات مهم، دکمه راهنما (؟) قرار دارد.",
            points = listOf(
                "با لمس ؟ یک پنجره کوتاه باز می‌شود که توضیح می‌دهد آن قابلیت چه کاری می‌کند، چه زمانی به کار می‌آید و چه پیش‌نیازی دارد.",
                "هیچ وقت نگران فراموش کردن اصطلاحات نباشید؛ راهنما همیشه کنار دست شماست."
            )
        )
    )
} else {
    listOf(
        TutorialStepContent(
            icon = Icons.Default.Shield,
            badge = "What Sword Does",
            title = "Practical security checks, made clear",
            summary = "Sword helps you inspect websites, HTTPS certificates, networks, Wi-Fi links, hashes, and files natively on Android without root.",
            points = listOf(
                "Built for both everyday beginners and technical security checks.",
                "Includes a sandboxed Python 3.10 runtime right inside the app—no terminal setup required."
            ),
            tip = "Only inspect websites, networks, and devices you own or have permission to check."
        ),
        TutorialStepContent(
            icon = Icons.Default.Dashboard,
            badge = "Home Tab",
            title = "Start fast from the Home screen",
            summary = "Your Home screen puts Quick Checks front and center so you can start in one tap.",
            points = listOf(
                "Run Check Website, Check Certificate, Domain Lookup, Create Hash, or Inspect File right away.",
                "See Device Readiness and Recent Reports without technical clutter."
            )
        ),
        TutorialStepContent(
            icon = Icons.Default.Apps,
            badge = "Tools Tab",
            title = "Browse all tool categories cleanly",
            summary = "Tap Tools in the bottom bar to explore every module across seven clear categories.",
            points = listOf(
                "Categories cover Network, Web Safety, Public Intel, Wi-Fi Info, Cryptography, File Forensics, and Preset Audits.",
                "Tap any tool to see what it does, whether it works on your phone, and Configure & Run."
            )
        ),
        TutorialStepContent(
            icon = Icons.Default.Translate,
            badge = "Reports & Explain Results",
            title = "Open reports and translate findings",
            summary = "Every completed check is saved automatically under the History and Reports tabs.",
            points = listOf(
                "Inside Report Viewer, switch between Summary, highlighted JSON, offline HTML, and the Explain tab.",
                "Explain Results runs 100% offline on your device to turn technical terms into plain English or Persian without altering original data."
            )
        ),
        TutorialStepContent(
            icon = Icons.Default.Settings,
            badge = "Settings & User Manual",
            title = "Find Settings & the User Manual",
            summary = "Tap the Settings tab in the bottom bar whenever you want to customize Sword or read instructions.",
            points = listOf(
                "The full searchable User Manual card sits right at the top of Settings.",
                "You will also find Replay Tutorial in Settings under Manual & Device Status, alongside Theme and Language."
            )
        ),
        TutorialStepContent(
            icon = Icons.AutoMirrored.Filled.HelpOutline,
            badge = "Help Everywhere",
            title = "Tap ? anytime a feature is unfamiliar",
            summary = "You will see a small ? Help button next to screens, settings, and report tabs.",
            points = listOf(
                "Tapping ? opens a short plain-language card explaining what it does, when to use it, and requirements.",
                "You are ready to go—tap Finish below to open your Home screen."
            )
        )
    )
}
