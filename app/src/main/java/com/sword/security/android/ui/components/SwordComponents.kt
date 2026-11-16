package com.sword.security.android.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sword.security.android.R
import com.sword.security.android.core.model.CapabilityState
import com.sword.security.android.ui.theme.SwordBackground
import com.sword.security.android.ui.theme.SwordBorder
import com.sword.security.android.ui.theme.SwordCardSurface
import com.sword.security.android.ui.theme.SwordDeepNavy
import com.sword.security.android.ui.theme.SwordError
import com.sword.security.android.ui.theme.SwordPrimaryCyan
import com.sword.security.android.ui.theme.SwordSecondaryTurquoise
import com.sword.security.android.ui.theme.SwordSeverityCritical
import com.sword.security.android.ui.theme.SwordSeverityHigh
import com.sword.security.android.ui.theme.SwordSeverityInfo
import com.sword.security.android.ui.theme.SwordSeverityLow
import com.sword.security.android.ui.theme.SwordSeverityMedium
import com.sword.security.android.ui.theme.SwordSuccess
import com.sword.security.android.ui.theme.SwordTextPrimary
import com.sword.security.android.ui.theme.SwordTextSecondary
import com.sword.security.android.ui.theme.SwordWarning

@Composable
fun SwordCircuitBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SwordBackground)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val traceColor = SwordBorder.copy(alpha = 0.35f)
            val nodeColor = SwordPrimaryCyan.copy(alpha = 0.14f)
            val step = 96.dp.toPx()

            var y = step * 0.6f
            var row = 0
            while (y < size.height) {
                val startX = if (row % 2 == 0) 0f else size.width * 0.15f
                val midX = size.width * (if (row % 2 == 0) 0.35f else 0.72f)
                drawLine(traceColor, Offset(startX, y), Offset(midX, y), strokeWidth = 1.2f)
                drawLine(traceColor, Offset(midX, y), Offset(midX + 28f, y + 28f), strokeWidth = 1.2f)
                drawCircle(nodeColor, radius = 3.5f, center = Offset(midX + 28f, y + 28f))
                y += step * 1.4f
                row++
            }
        }
        content()
    }
}

@Composable
fun SwordLogoBadge(
    size: Dp = 56.dp,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = R.drawable.sword_emblem),
        contentDescription = "Sword Android Security Toolkit Emblem",
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, SwordPrimaryCyan.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwordTopBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = SwordPrimaryCyan,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = SwordPrimaryCyan
                    )
                }
            }
        },
        actions = { actions() },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = SwordDeepNavy,
            titleContentColor = SwordTextPrimary
        )
    )
}

@Composable
fun SwordCard(
    modifier: Modifier = Modifier,
    borderColor: Color = SwordBorder,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    val baseModifier = modifier
        .fillMaxWidth()
        .clip(shape)
        .let { if (onClick != null) it.clickable(onClick = onClick) else it }

    Surface(
        modifier = baseModifier,
        shape = shape,
        color = SwordCardSurface,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun CapabilityBadge(state: CapabilityState) {
    val (color, bg) = when (state) {
        CapabilityState.AVAILABLE -> SwordSuccess to SwordSuccess.copy(alpha = 0.12f)
        CapabilityState.AVAILABLE_WITH_LIMITATIONS -> SwordWarning to SwordWarning.copy(alpha = 0.12f)
        CapabilityState.REQUIRES_PERMISSION -> SwordPrimaryCyan to SwordPrimaryCyan.copy(alpha = 0.12f)
        CapabilityState.UNSUPPORTED -> SwordError to SwordError.copy(alpha = 0.12f)
        CapabilityState.NOT_IMPLEMENTED -> SwordTextSecondary to SwordTextSecondary.copy(alpha = 0.12f)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = state.label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        )
    }
}

@Composable
fun SeverityBadge(severity: String) {
    val color = when (severity.uppercase()) {
        "CRITICAL" -> SwordSeverityCritical
        "HIGH" -> SwordSeverityHigh
        "MEDIUM" -> SwordSeverityMedium
        "LOW" -> SwordSeverityLow
        else -> SwordSeverityInfo
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.65f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = severity.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        )
    }
}

@Composable
fun SectionHeader(title: String, actionText: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(16.dp)
                    .background(SwordPrimaryCyan, RoundedCornerShape(2.dp))
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = SwordTextPrimary
            )
        }
        if (actionText != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelLarge,
                    color = SwordPrimaryCyan
                )
            }
        }
    }
}

@Composable
fun TerminalOutputBox(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFF0A1219),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, SwordBorder)
    ) {
        Text(
            text = text.ifBlank { "No output generated." },
            modifier = Modifier.padding(14.dp),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = Color(0xFFDCEEF5)
            )
        )
    }
}

@Composable
fun AuthorizationConfirmDialog(
    toolName: String,
    targetSummary: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SwordCardSurface,
        titleContentColor = SwordTextPrimary,
        textContentColor = SwordTextSecondary,
        title = {
            Text(
                text = "Authorized Testing Confirmation",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Confirm that you own or have explicit written authorization to inspect:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Surface(
                    color = SwordDeepNavy,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, SwordBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(toolName, style = MaterialTheme.typography.titleMedium, color = SwordPrimaryCyan)
                        Text(targetSummary.ifBlank { "Device / Target" }, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SwordPrimaryCyan,
                    contentColor = SwordDeepNavy
                )
            ) {
                Text("I Am Authorized — Run")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = BorderStroke(1.dp, SwordBorder)
            ) {
                Text("Cancel", color = SwordTextSecondary)
            }
        }
    )
}
