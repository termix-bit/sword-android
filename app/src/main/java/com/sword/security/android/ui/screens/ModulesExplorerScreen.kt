package com.sword.security.android.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sword.security.android.core.i18n.HelpTopic
import com.sword.security.android.core.i18n.SwordHelpContent
import com.sword.security.android.core.i18n.SwordStrings
import com.sword.security.android.core.model.CapabilityState
import com.sword.security.android.core.model.ModuleCategory
import com.sword.security.android.core.model.ToolDescriptor
import com.sword.security.android.ui.components.CapabilityBadge
import com.sword.security.android.ui.components.ContextualHelpDialog
import com.sword.security.android.ui.components.HelpIconButton
import com.sword.security.android.ui.components.LocalSwordAnimationsEnabled
import com.sword.security.android.ui.components.SwordCard
import com.sword.security.android.ui.components.SwordCircuitBackdrop
import com.sword.security.android.ui.components.SwordEmptyStateCard
import com.sword.security.android.ui.components.SwordFilterChip
import com.sword.security.android.ui.components.SwordTopBar
import com.sword.security.android.ui.components.swordOutlinedTextFieldColors
import com.sword.security.android.ui.theme.LocalSwordColors

@Composable
fun ModulesExplorerScreen(
    tools: List<ToolDescriptor>,
    initialCategory: ModuleCategory? = null,
    languageCode: String = "en",
    scrollToTopTrigger: Int = 0,
    resetFilterTrigger: Int = 0,
    resolveCapability: (ToolDescriptor) -> CapabilityState,
    onSelectTool: (ToolDescriptor) -> Unit
) {
    val swordColors = LocalSwordColors.current
    val animationsEnabled = LocalSwordAnimationsEnabled.current
    val listState = rememberLazyListState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember(initialCategory) { mutableStateOf(initialCategory) }
    var activeHelpTopic by remember { mutableStateOf<HelpTopic?>(null) }

    LaunchedEffect(resetFilterTrigger) {
        if (resetFilterTrigger > 0) {
            selectedCategory = null
            searchQuery = ""
        }
    }

    LaunchedEffect(scrollToTopTrigger) {
        if (scrollToTopTrigger > 0) {
            if (animationsEnabled) {
                listState.animateScrollToItem(0)
            } else {
                listState.scrollToItem(0)
            }
        }
    }

    activeHelpTopic?.let { topic ->
        ContextualHelpDialog(
            topic = topic,
            languageCode = languageCode,
            onDismiss = { activeHelpTopic = null }
        )
    }

    val toolCategories = remember { ModuleCategory.entries.filter { it.isToolCategory } }

    val filtered = remember(tools, searchQuery, selectedCategory, languageCode) {
        tools.filter { tool ->
            val catMatch = selectedCategory == null || tool.category == selectedCategory
            val q = searchQuery.trim()
            val localizedCat = SwordStrings.categoryTitle(tool.category, languageCode)
            val textMatch = q.isEmpty() ||
                tool.name.contains(q, ignoreCase = true) ||
                tool.summary.contains(q, ignoreCase = true) ||
                tool.category.title.contains(q, ignoreCase = true) ||
                localizedCat.contains(q, ignoreCase = true)
            catMatch && textMatch
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            SwordTopBar(
                title = SwordStrings.label("modules_title", languageCode),
                subtitle = "${filtered.size}",
                actions = {
                    HelpIconButton(
                        onClick = {
                            activeHelpTopic = selectedCategory?.let {
                                SwordHelpContent.forCategory(it, languageCode)
                            } ?: SwordHelpContent.forKey("home_overview", languageCode)
                        },
                        contentDescription = SwordStrings.label("help", languageCode)
                    )
                }
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
                    placeholder = { Text(SwordStrings.label("modules_search_hint", languageCode)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = swordColors.accent) },
                    singleLine = true,
                    colors = swordOutlinedTextFieldColors()
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        SwordFilterChip(
                            selected = selectedCategory == null,
                            onClick = { selectedCategory = null },
                            label = SwordStrings.label("all", languageCode)
                        )
                    }
                    items(toolCategories) { cat ->
                        SwordFilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = if (selectedCategory == cat) null else cat },
                            label = SwordStrings.categoryTitle(cat, languageCode)
                        )
                    }
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (filtered.isEmpty()) {
                        item {
                            SwordEmptyStateCard(
                                icon = Icons.Default.Search,
                                title = SwordStrings.label("modules_title", languageCode),
                                message = SwordStrings.label("no_matching_tools", languageCode),
                                actionLabel = SwordStrings.label("clear_filters", languageCode),
                                onAction = {
                                    searchQuery = ""
                                    selectedCategory = null
                                }
                            )
                        }
                    } else {
                        items(filtered, key = { it.id }) { tool ->
                            val cap = resolveCapability(tool)
                            SwordCard(onClick = { onSelectTool(tool) }) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = SwordStrings.categoryTitle(tool.category, languageCode),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = swordColors.accent
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CapabilityBadge(cap, languageCode = languageCode)
                                        HelpIconButton(
                                            onClick = {
                                                activeHelpTopic = SwordHelpContent.forTool(tool, languageCode)
                                            },
                                            contentDescription = "${SwordStrings.label("help", languageCode)}: ${tool.name}"
                                        )
                                    }
                                }
                                Text(tool.name, style = MaterialTheme.typography.titleMedium, color = swordColors.textPrimary)
                                Spacer(Modifier.height(4.dp))
                                Text(tool.summary, style = MaterialTheme.typography.bodyMedium, color = swordColors.textSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}
