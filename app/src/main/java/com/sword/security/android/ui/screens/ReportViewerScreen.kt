package com.sword.security.android.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.sword.security.android.core.i18n.SwordHelpContent
import com.sword.security.android.core.i18n.SwordStrings
import com.sword.security.android.data.local.db.ReportMetadataEntity
import com.sword.security.android.data.reports.ReportViewerData
import com.sword.security.android.ui.components.ContextualHelpDialog
import com.sword.security.android.ui.components.HelpIconButton
import com.sword.security.android.ui.components.JsonHighlightPalette
import com.sword.security.android.ui.components.JsonSyntaxHighlighter
import com.sword.security.android.ui.components.SectionHeader
import com.sword.security.android.ui.components.SeverityBadge
import com.sword.security.android.ui.components.SwordCard
import com.sword.security.android.ui.components.SwordCircuitBackdrop
import com.sword.security.android.ui.components.SwordEmptyStateCard
import com.sword.security.android.ui.components.SwordErrorCard
import com.sword.security.android.ui.components.SwordTopBar
import com.sword.security.android.ui.components.TerminalOutputBox
import com.sword.security.android.ui.theme.LocalSwordColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReportViewerScreen(
    reportMeta: ReportMetadataEntity?,
    viewerData: ReportViewerData?,
    languageCode: String,
    onBack: () -> Unit,
    onDeleteReport: (ReportMetadataEntity) -> Unit,
    onExportReport: (String, Uri) -> Unit,
    getShareUri: (String) -> Uri?,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val swordColors = LocalSwordColors.current

    var selectedTab by remember { mutableIntStateOf(0) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var pendingExportFileName by remember { mutableStateOf<String?>(null) }

    val jsonExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        val fileName = pendingExportFileName
        if (uri != null && fileName != null) {
            onExportReport(fileName, uri)
        }
    }

    val htmlExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/html")
    ) { uri ->
        val fileName = pendingExportFileName
        if (uri != null && fileName != null) {
            onExportReport(fileName, uri)
        }
    }

    if (showHelpDialog) {
        ContextualHelpDialog(
            topic = SwordHelpContent.forKey("reports_viewer", languageCode),
            languageCode = languageCode,
            onDismiss = { showHelpDialog = false }
        )
    }

    if (showDeleteConfirm && reportMeta != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = swordColors.cardSurface,
            titleContentColor = swordColors.textPrimary,
            textContentColor = swordColors.textSecondary,
            title = { Text(SwordStrings.label("delete", languageCode), color = swordColors.textPrimary) },
            text = { Text(SwordStrings.label("delete_report_confirm", languageCode), color = swordColors.textSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteReport(reportMeta)
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = swordColors.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(SwordStrings.label("delete", languageCode))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteConfirm = false },
                    border = BorderStroke(1.dp, swordColors.border)
                ) {
                    Text(SwordStrings.label("cancel", languageCode), color = swordColors.textSecondary)
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            SwordTopBar(
                title = reportMeta?.title ?: SwordStrings.label("report_viewer", languageCode),
                subtitle = reportMeta?.let {
                    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date(it.createdEpochMs))
                },
                onBack = onBack,
                actions = {
                    HelpIconButton(
                        onClick = { showHelpDialog = true },
                        contentDescription = SwordStrings.label("help", languageCode)
                    )
                }
            )
        }
    ) { innerPadding ->
        SwordCircuitBackdrop(modifier = Modifier.padding(innerPadding)) {
            if (reportMeta == null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    SwordEmptyStateCard(
                        icon = Icons.Default.Description,
                        title = SwordStrings.label("report_viewer", languageCode),
                        message = SwordStrings.label("report_not_found", languageCode),
                        actionLabel = SwordStrings.label("back", languageCode),
                        onAction = onBack
                    )
                }
                return@SwordCircuitBackdrop
            }

            Column(modifier = Modifier.fillMaxSize()) {
                val tabs = listOf(
                    SwordStrings.label("tab_summary", languageCode),
                    SwordStrings.label("tab_json", languageCode),
                    SwordStrings.label("tab_html", languageCode)
                )
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = swordColors.topBarBackground,
                    contentColor = swordColors.accent,
                    indicator = { tabPositions ->
                        if (selectedTab < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = swordColors.accent
                            )
                        }
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            selectedContentColor = swordColors.accent,
                            unselectedContentColor = swordColors.textSecondary,
                            text = {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                                    )
                                )
                            }
                        )
                    }
                }

                // Clear, separate report actions bar
                Surface(
                    color = swordColors.cardSurface,
                    border = BorderStroke(0.5.dp, swordColors.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val activeIsHtml = selectedTab == 2
                        val activeFileName = if (activeIsHtml) reportMeta.htmlFileName else reportMeta.jsonFileName
                        val activeMime = if (activeIsHtml) "text/html" else "application/json"

                        OutlinedButton(
                            onClick = {
                                pendingExportFileName = activeFileName
                                if (activeIsHtml) {
                                    htmlExportLauncher.launch(activeFileName)
                                } else {
                                    jsonExportLauncher.launch(activeFileName)
                                }
                            },
                            border = BorderStroke(1.dp, swordColors.border)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = swordColors.accent, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = SwordStrings.label(if (activeIsHtml) "export_html" else "export_json", languageCode),
                                color = swordColors.textPrimary
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                val uri = getShareUri(activeFileName)
                                if (uri != null) {
                                    val share = Intent(Intent.ACTION_SEND).apply {
                                        type = activeMime
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(share, reportMeta.title))
                                } else {
                                    onShowMessage(SwordStrings.label("report_not_found", languageCode))
                                }
                            },
                            border = BorderStroke(1.dp, swordColors.border)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = swordColors.accent, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = SwordStrings.label(if (activeIsHtml) "share_html" else "share_json", languageCode),
                                color = swordColors.textPrimary
                            )
                        }

                        if (selectedTab == 1 && viewerData != null && viewerData.formattedJsonText.isNotBlank()) {
                            OutlinedButton(
                                onClick = {
                                    clipboard.setText(AnnotatedString(viewerData.formattedJsonText))
                                    onShowMessage(SwordStrings.label("json_copied", languageCode))
                                },
                                border = BorderStroke(1.dp, swordColors.accent.copy(alpha = 0.6f))
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = swordColors.accent, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(SwordStrings.label("copy_json", languageCode), color = swordColors.accent)
                            }
                        }

                        OutlinedButton(
                            onClick = { showDeleteConfirm = true },
                            border = BorderStroke(1.dp, swordColors.error.copy(alpha = 0.55f))
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = swordColors.error, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(SwordStrings.label("delete", languageCode), color = swordColors.error)
                        }
                    }
                }

                if (viewerData == null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = swordColors.accent)
                    }
                } else {
                    when (selectedTab) {
                        0 -> ReportSummaryTab(viewerData = viewerData, reportMeta = reportMeta, languageCode = languageCode)
                        1 -> ReportJsonTab(
                            viewerData = viewerData,
                            languageCode = languageCode
                        )
                        else -> ReportHtmlTab(viewerData = viewerData, languageCode = languageCode)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportSummaryTab(
    viewerData: ReportViewerData,
    reportMeta: ReportMetadataEntity,
    languageCode: String
) {
    val swordColors = LocalSwordColors.current
    val parsed = viewerData.parsedReport
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (parsed == null) {
            SwordErrorCard(
                title = SwordStrings.label("json_error_title", languageCode),
                message = viewerData.jsonError ?: SwordStrings.label("report_not_found", languageCode),
                rawFallback = viewerData.rawJsonText.takeIf { it.isNotBlank() }
            )
            return
        }

        SwordCard(borderColor = swordColors.accent.copy(alpha = 0.45f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(parsed.toolName, style = MaterialTheme.typography.titleLarge, color = swordColors.textPrimary)
                    Text(parsed.target, style = MaterialTheme.typography.bodyMedium, color = swordColors.accent)
                }
                SeverityBadge(parsed.highestSeverity)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "${parsed.category} • ${parsed.timestampIso} • ${parsed.durationMs} ms",
                style = MaterialTheme.typography.labelSmall,
                color = swordColors.textSecondary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "JSON: ${reportMeta.jsonFileName}",
                style = MaterialTheme.typography.labelSmall,
                color = swordColors.textSecondary
            )
        }

        SectionHeader(title = "${SwordStrings.label("findings", languageCode)} (${parsed.findings.size})")
        if (parsed.findings.isEmpty()) {
            SwordCard {
                Text(
                    text = "No structured security findings recorded.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = swordColors.textSecondary
                )
            }
        } else {
            parsed.findings.forEach { f ->
                SwordCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = f.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = swordColors.textPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        SeverityBadge(f.severity)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(f.detail, style = MaterialTheme.typography.bodyMedium, color = swordColors.textPrimary)
                    if (f.recommendation.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "${SwordStrings.label("recommendation", languageCode)} ${f.recommendation}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = swordColors.accent
                        )
                    }
                }
            }
        }

        SectionHeader(title = SwordStrings.label("detailed_output", languageCode))
        TerminalOutputBox(text = parsed.output)
    }
}

@Composable
private fun ReportJsonTab(
    viewerData: ReportViewerData,
    languageCode: String
) {
    val swordColors = LocalSwordColors.current
    val palette = remember(swordColors) {
        JsonHighlightPalette(
            keyColor = swordColors.jsonKey,
            stringColor = swordColors.jsonString,
            numberColor = swordColors.jsonNumber,
            booleanColor = swordColors.jsonBoolean,
            punctuationColor = swordColors.jsonPunctuation,
            fallbackColor = swordColors.codeText
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (viewerData.jsonError != null) {
            SwordErrorCard(
                title = SwordStrings.label("json_error_title", languageCode),
                message = viewerData.jsonError,
                rawFallback = viewerData.rawJsonText.takeIf { it.isNotBlank() }
            )
            return
        }

        val highlighted = remember(viewerData.formattedJsonText, palette) {
            JsonSyntaxHighlighter.highlight(viewerData.formattedJsonText, palette)
        }

        Text(
            text = viewerData.jsonFileName,
            style = MaterialTheme.typography.labelSmall,
            color = swordColors.textSecondary
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            color = swordColors.codeSurface,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, swordColors.border)
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                SelectionContainer {
                    Text(
                        text = highlighted,
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .horizontalScroll(rememberScrollState())
                            .padding(14.dp),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.5.sp,
                            lineHeight = 19.sp
                        )
                    )
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun ReportHtmlTab(
    viewerData: ReportViewerData,
    languageCode: String
) {
    if (viewerData.htmlError != null || viewerData.htmlText.isBlank()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            SwordErrorCard(
                title = SwordStrings.label("html_error_title", languageCode),
                message = viewerData.htmlError ?: SwordStrings.label("report_not_found", languageCode)
            )
        }
        return
    }

    val swordColors = LocalSwordColors.current
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, swordColors.border),
        color = swordColors.codeSurface
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.apply {
                        javaScriptEnabled = false
                        allowFileAccess = false
                        allowContentAccess = false
                        @Suppress("DEPRECATION")
                        allowFileAccessFromFileURLs = false
                        @Suppress("DEPRECATION")
                        allowUniversalAccessFromFileURLs = false
                        domStorageEnabled = false
                        mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        builtInZoomControls = true
                        displayZoomControls = false
                    }
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean = true
                    }
                    loadDataWithBaseURL(null, viewerData.htmlText, "text/html", "UTF-8", null)
                }
            },
            update = { webView ->
                webView.loadDataWithBaseURL(null, viewerData.htmlText, "text/html", "UTF-8", null)
            }
        )
    }
}
