package com.sword.security.android.ui.components

import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sword.security.android.R
import com.sword.security.android.core.i18n.HelpTopic
import com.sword.security.android.core.i18n.SwordStrings
import com.sword.security.android.core.model.CapabilityState
import com.sword.security.android.ui.theme.LocalSwordColors

val LocalSwordAnimationsEnabled = staticCompositionLocalOf { true }

@Composable
fun rememberEffectiveAnimationsEnabled(userPreferenceEnabled: Boolean): Boolean {
    val context = LocalContext.current
    val systemScale = remember(context) {
        runCatching {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f
            )
        }.getOrDefault(1.0f)
    }
    return userPreferenceEnabled && systemScale > 0f
}

@Composable
fun SwordCircuitBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val swordColors = LocalSwordColors.current
    val bgColor = MaterialTheme.colorScheme.background
    val traceAlpha = if (swordColors.isDark) 0.32f else 0.45f
    val nodeAlpha = if (swordColors.isDark) 0.13f else 0.10f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val traceColor = swordColors.border.copy(alpha = traceAlpha)
            val nodeColor = swordColors.accent.copy(alpha = nodeAlpha)
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
    val swordColors = LocalSwordColors.current
    Image(
        painter = painterResource(id = R.drawable.sword_emblem),
        contentDescription = "Sword Android Security Toolkit Emblem",
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, swordColors.accent.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
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
    val swordColors = LocalSwordColors.current
    CenterAlignedTopAppBar(
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = swordColors.topBarContent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = swordColors.accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = swordColors.accent
                    )
                }
            }
        },
        actions = { actions() },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = swordColors.topBarBackground,
            titleContentColor = swordColors.topBarContent
        )
    )
}

@Composable
fun SwordCard(
    modifier: Modifier = Modifier,
    borderColor: Color? = null,
    containerColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val swordColors = LocalSwordColors.current
    val animationsEnabled = LocalSwordAnimationsEnabled.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (onClick != null && isPressed && animationsEnabled) 0.985f else 1f,
        animationSpec = tween(durationMillis = if (animationsEnabled) 100 else 0),
        label = "card_press_scale"
    )
    val resolvedBorder = when {
        borderColor != null -> borderColor
        onClick != null && isPressed -> swordColors.accent.copy(alpha = 0.7f)
        else -> swordColors.border
    }

    val shape = RoundedCornerShape(14.dp)
    val baseModifier = modifier
        .fillMaxWidth()
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clip(shape)
        .let {
            if (onClick != null) {
                it.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
            } else {
                it
            }
        }

    Surface(
        modifier = baseModifier,
        shape = shape,
        color = containerColor ?: swordColors.cardSurface,
        contentColor = swordColors.textPrimary,
        border = BorderStroke(1.dp, resolvedBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun CapabilityBadge(
    state: CapabilityState,
    languageCode: String = "en"
) {
    val swordColors = LocalSwordColors.current
    val color = when (state) {
        CapabilityState.AVAILABLE -> swordColors.success
        CapabilityState.AVAILABLE_WITH_LIMITATIONS -> swordColors.warning
        CapabilityState.REQUIRES_PERMISSION -> swordColors.accent
        CapabilityState.UNSUPPORTED -> swordColors.error
        CapabilityState.NOT_IMPLEMENTED -> swordColors.textSecondary
    }
    val bgAlpha = if (swordColors.isDark) 0.14f else 0.10f

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = bgAlpha))
            .border(1.dp, color.copy(alpha = 0.55f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = SwordStrings.capabilityLabel(state, languageCode),
            style = MaterialTheme.typography.labelSmall.copy(
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        )
    }
}

@Composable
fun SeverityBadge(severity: String) {
    val swordColors = LocalSwordColors.current
    val color = when (severity.uppercase()) {
        "CRITICAL" -> swordColors.severityCritical
        "HIGH" -> swordColors.severityHigh
        "MEDIUM" -> swordColors.severityMedium
        "LOW" -> swordColors.severityLow
        else -> swordColors.severityInfo
    }
    val bgAlpha = if (swordColors.isDark) 0.14f else 0.10f

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = bgAlpha))
            .border(1.dp, color.copy(alpha = 0.65f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = severity.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    onHelpClick: (() -> Unit)? = null,
    helpContentDescription: String = "Help"
) {
    val swordColors = LocalSwordColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(18.dp)
                    .background(swordColors.accent, RoundedCornerShape(2.dp))
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = swordColors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            if (onHelpClick != null) {
                HelpIconButton(
                    onClick = onHelpClick,
                    contentDescription = helpContentDescription
                )
            }
        }
        if (actionText != null && onAction != null) {
            TextButton(
                onClick = onAction,
                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
            ) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelLarge,
                    color = swordColors.accent
                )
            }
        }
    }
}

@Composable
fun HelpIconButton(
    onClick: () -> Unit,
    contentDescription: String = "Help",
    modifier: Modifier = Modifier
) {
    val swordColors = LocalSwordColors.current
    IconButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
            contentDescription = contentDescription,
            tint = swordColors.accent,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun ContextualHelpDialog(
    topic: HelpTopic,
    languageCode: String,
    onDismiss: () -> Unit
) {
    val swordColors = LocalSwordColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = swordColors.cardSurface,
        titleContentColor = swordColors.textPrimary,
        textContentColor = swordColors.textSecondary,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                    contentDescription = null,
                    tint = swordColors.accent
                )
                Text(
                    text = topic.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = swordColors.textPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HelpSectionBlock(
                    label = SwordStrings.label("help_what_it_does", languageCode),
                    body = topic.whatItDoes
                )
                HelpSectionBlock(
                    label = SwordStrings.label("help_when_to_use", languageCode),
                    body = topic.whenToUse
                )
                HelpSectionBlock(
                    label = SwordStrings.label("help_prerequisites", languageCode),
                    body = topic.prerequisites
                )
                Surface(
                    color = swordColors.elevatedSurface,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, swordColors.warning.copy(alpha = 0.45f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = SwordStrings.label("help_security_note", languageCode),
                            style = MaterialTheme.typography.labelLarge,
                            color = swordColors.warning
                        )
                        Text(
                            text = topic.securityNote,
                            style = MaterialTheme.typography.bodyMedium,
                            color = swordColors.textPrimary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(SwordStrings.label("close", languageCode))
            }
        }
    )
}

@Composable
private fun HelpSectionBlock(label: String, body: String) {
    val swordColors = LocalSwordColors.current
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = swordColors.accent
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = swordColors.textPrimary
        )
    }
}

@Composable
fun ExpandableHelpPanel(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    note: String? = null,
    initiallyExpanded: Boolean = false
) {
    val swordColors = LocalSwordColors.current
    val animationsEnabled = LocalSwordAnimationsEnabled.current
    var expanded by remember { mutableStateOf(initiallyExpanded) }

    SwordCard(
        modifier = modifier,
        borderColor = if (expanded) swordColors.accent.copy(alpha = 0.55f) else swordColors.border,
        onClick = { expanded = !expanded }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 36.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (expanded) swordColors.accent else swordColors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = swordColors.accent
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter = if (animationsEnabled) expandVertically() + fadeIn() else fadeIn(tween(0)),
            exit = if (animationsEnabled) shrinkVertically() + fadeOut() else fadeOut(tween(0))
        ) {
            Column(
                modifier = Modifier.padding(top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = swordColors.textPrimary
                )
                if (!note.isNullOrBlank()) {
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodyMedium,
                        color = swordColors.warning
                    )
                }
            }
        }
    }
}

@Composable
fun swordFilterChipColors(): SelectableChipColors {
    val swordColors = LocalSwordColors.current
    val cs = MaterialTheme.colorScheme
    return FilterChipDefaults.filterChipColors(
        containerColor = swordColors.elevatedSurface,
        labelColor = swordColors.textPrimary,
        iconColor = swordColors.textSecondary,
        selectedContainerColor = cs.primary,
        selectedLabelColor = cs.onPrimary,
        selectedLeadingIconColor = cs.onPrimary,
        selectedTrailingIconColor = cs.onPrimary,
        disabledContainerColor = swordColors.cardSurface.copy(alpha = 0.5f),
        disabledLabelColor = swordColors.textSecondary.copy(alpha = 0.5f),
        disabledSelectedContainerColor = cs.primary.copy(alpha = 0.4f)
    )
}

@Composable
fun swordFilterChipBorder(selected: Boolean, enabled: Boolean = true): BorderStroke {
    val swordColors = LocalSwordColors.current
    val cs = MaterialTheme.colorScheme
    return FilterChipDefaults.filterChipBorder(
        enabled = enabled,
        selected = selected,
        borderColor = swordColors.border,
        selectedBorderColor = cs.primary,
        disabledBorderColor = swordColors.border.copy(alpha = 0.4f),
        disabledSelectedBorderColor = cs.primary.copy(alpha = 0.4f),
        borderWidth = 1.dp,
        selectedBorderWidth = 1.5.dp
    )
}

@Composable
fun SwordFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showCheckWhenSelected: Boolean = true
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.defaultMinSize(minHeight = 40.dp),
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                )
            )
        },
        leadingIcon = if (selected && showCheckWhenSelected) {
            {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else {
            null
        },
        colors = swordFilterChipColors(),
        border = swordFilterChipBorder(selected = selected, enabled = enabled)
    )
}

@Composable
fun swordSwitchColors(): SwitchColors {
    val swordColors = LocalSwordColors.current
    val cs = MaterialTheme.colorScheme
    return SwitchDefaults.colors(
        checkedThumbColor = cs.onPrimary,
        checkedTrackColor = cs.primary,
        checkedBorderColor = cs.primary,
        uncheckedThumbColor = swordColors.textSecondary,
        uncheckedTrackColor = swordColors.elevatedSurface,
        uncheckedBorderColor = swordColors.border
    )
}

@Composable
fun swordSliderColors(): SliderColors {
    val swordColors = LocalSwordColors.current
    val cs = MaterialTheme.colorScheme
    return SliderDefaults.colors(
        thumbColor = cs.primary,
        activeTrackColor = cs.primary,
        inactiveTrackColor = swordColors.border,
        activeTickColor = cs.onPrimary,
        inactiveTickColor = swordColors.textSecondary
    )
}

@Composable
fun swordOutlinedTextFieldColors(): TextFieldColors {
    val swordColors = LocalSwordColors.current
    val cs = MaterialTheme.colorScheme
    return OutlinedTextFieldDefaults.colors(
        focusedBorderColor = cs.primary,
        unfocusedBorderColor = swordColors.border,
        focusedTextColor = swordColors.textPrimary,
        unfocusedTextColor = swordColors.textPrimary,
        focusedPlaceholderColor = swordColors.textSecondary,
        unfocusedPlaceholderColor = swordColors.textSecondary,
        focusedContainerColor = swordColors.cardSurface,
        unfocusedContainerColor = swordColors.cardSurface,
        cursorColor = cs.primary
    )
}

@Composable
fun TerminalOutputBox(text: String, modifier: Modifier = Modifier) {
    val swordColors = LocalSwordColors.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = swordColors.codeSurface,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, swordColors.border)
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            SelectionContainer {
                Text(
                    text = text.ifBlank { "No output generated." },
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = swordColors.codeText
                    )
                )
            }
        }
    }
}

@Composable
fun SwordEmptyStateCard(
    title: String,
    message: String,
    icon: ImageVector = Icons.Default.Shield,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    val swordColors = LocalSwordColors.current
    SwordCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = swordColors.accent,
                modifier = Modifier.size(36.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = swordColors.textPrimary
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = swordColors.textSecondary
            )
            if (actionLabel != null && onAction != null) {
                Spacer(Modifier.height(4.dp))
                OutlinedButton(
                    onClick = onAction,
                    border = BorderStroke(1.dp, swordColors.accent)
                ) {
                    Text(actionLabel, color = swordColors.accent)
                }
            }
        }
    }
}

@Composable
fun SwordErrorCard(
    title: String,
    message: String,
    rawFallback: String? = null
) {
    val swordColors = LocalSwordColors.current
    SwordCard(borderColor = swordColors.error.copy(alpha = 0.65f)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = swordColors.error
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = swordColors.error
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = swordColors.textPrimary
        )
        if (!rawFallback.isNullOrBlank()) {
            Spacer(Modifier.height(10.dp))
            TerminalOutputBox(text = rawFallback.take(4000))
        }
    }
}

@Composable
fun AuthorizationConfirmDialog(
    toolName: String,
    targetSummary: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    languageCode: String = "en"
) {
    val swordColors = LocalSwordColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = swordColors.cardSurface,
        titleContentColor = swordColors.textPrimary,
        textContentColor = swordColors.textSecondary,
        title = {
            Text(
                text = SwordStrings.label("auth_dialog_title", languageCode),
                style = MaterialTheme.typography.titleLarge,
                color = swordColors.textPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = SwordStrings.label("auth_dialog_body", languageCode),
                    style = MaterialTheme.typography.bodyMedium,
                    color = swordColors.textSecondary
                )
                Surface(
                    color = swordColors.elevatedSurface,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, swordColors.border)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(toolName, style = MaterialTheme.typography.titleMedium, color = swordColors.accent)
                        Text(
                            text = targetSummary.ifBlank { "Device / Target" },
                            style = MaterialTheme.typography.labelSmall,
                            color = swordColors.textSecondary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(SwordStrings.label("auth_dialog_confirm", languageCode))
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = BorderStroke(1.dp, swordColors.border)
            ) {
                Text(SwordStrings.label("cancel", languageCode), color = swordColors.textSecondary)
            }
        }
    )
}
