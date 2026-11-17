package com.sword.security.android.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sword.security.android.core.i18n.ManualSection
import com.sword.security.android.core.i18n.SwordManualContent
import com.sword.security.android.core.i18n.SwordStrings
import com.sword.security.android.ui.components.LocalSwordAnimationsEnabled
import com.sword.security.android.ui.components.SwordCard
import com.sword.security.android.ui.components.SwordCircuitBackdrop
import com.sword.security.android.ui.components.SwordEmptyStateCard
import com.sword.security.android.ui.components.SwordTopBar
import com.sword.security.android.ui.components.swordOutlinedTextFieldColors
import com.sword.security.android.ui.theme.LocalSwordColors

@Composable
fun UserManualScreen(
    languageCode: String,
    onBack: () -> Unit
) {
    val swordColors = LocalSwordColors.current
    val animationsEnabled = LocalSwordAnimationsEnabled.current
    var searchQuery by remember { mutableStateOf("") }
    val allSections = remember(languageCode) { SwordManualContent.sections(languageCode) }
    val expandedIds = remember(languageCode) {
        mutableStateMapOf<String, Boolean>().apply {
            allSections.firstOrNull()?.let { put(it.id, true) }
        }
    }

    val filtered = remember(allSections, searchQuery) {
        val q = searchQuery.trim()
        if (q.isEmpty()) {
            allSections
        } else {
            allSections.filter { sec ->
                sec.title.contains(q, ignoreCase = true) ||
                    sec.summary.contains(q, ignoreCase = true) ||
                    sec.bullets.any { it.contains(q, ignoreCase = true) } ||
                    (sec.note?.contains(q, ignoreCase = true) == true)
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            SwordTopBar(
                title = SwordStrings.label("user_manual", languageCode),
                subtitle = SwordStrings.label("user_manual_sub", languageCode),
                onBack = onBack
            )
        }
    ) { innerPadding ->
        SwordCircuitBackdrop(modifier = Modifier.padding(innerPadding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    placeholder = { Text(SwordStrings.label("manual_search_hint", languageCode)) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = swordColors.accent)
                    },
                    singleLine = true,
                    colors = swordOutlinedTextFieldColors()
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (filtered.isEmpty()) {
                        item {
                            SwordEmptyStateCard(
                                icon = Icons.Default.MenuBook,
                                title = SwordStrings.label("user_manual", languageCode),
                                message = SwordStrings.label("no_matching_tools", languageCode),
                                actionLabel = SwordStrings.label("clear_filters", languageCode),
                                onAction = { searchQuery = "" }
                            )
                        }
                    } else {
                        items(filtered, key = { it.id }) { section ->
                            val isExpanded = searchQuery.isNotBlank() || (expandedIds[section.id] == true)
                            ManualSectionCard(
                                section = section,
                                expanded = isExpanded,
                                animationsEnabled = animationsEnabled,
                                onToggle = {
                                    expandedIds[section.id] = !(expandedIds[section.id] ?: false)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ManualSectionCard(
    section: ManualSection,
    expanded: Boolean,
    animationsEnabled: Boolean,
    onToggle: () -> Unit
) {
    val swordColors = LocalSwordColors.current
    SwordCard(
        borderColor = if (expanded) swordColors.accent.copy(alpha = 0.55f) else swordColors.border,
        onClick = onToggle
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 40.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = section.title,
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
                    text = section.summary,
                    style = MaterialTheme.typography.bodyLarge,
                    color = swordColors.textPrimary
                )
                section.bullets.forEach { bullet ->
                    Text(
                        text = "• $bullet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = swordColors.textSecondary
                    )
                }
                if (!section.note.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Surface(
                        color = swordColors.elevatedSurface,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, swordColors.warning.copy(alpha = 0.45f))
                    ) {
                        Text(
                            text = section.note,
                            modifier = Modifier.padding(10.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = swordColors.warning
                        )
                    }
                }
            }
        }
    }
}
