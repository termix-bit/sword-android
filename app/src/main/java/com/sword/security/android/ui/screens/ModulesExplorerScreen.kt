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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sword.security.android.core.model.CapabilityState
import com.sword.security.android.core.model.ModuleCategory
import com.sword.security.android.core.model.ToolDescriptor
import com.sword.security.android.ui.components.CapabilityBadge
import com.sword.security.android.ui.components.SwordCard
import com.sword.security.android.ui.components.SwordCircuitBackdrop
import com.sword.security.android.ui.components.SwordTopBar
import com.sword.security.android.ui.theme.SwordBackground
import com.sword.security.android.ui.theme.SwordBorder
import com.sword.security.android.ui.theme.SwordCardSurface
import com.sword.security.android.ui.theme.SwordDeepNavy
import com.sword.security.android.ui.theme.SwordPrimaryCyan
import com.sword.security.android.ui.theme.SwordTextPrimary
import com.sword.security.android.ui.theme.SwordTextSecondary

@Composable
fun ModulesExplorerScreen(
    tools: List<ToolDescriptor>,
    initialCategory: ModuleCategory? = null,
    resolveCapability: (ToolDescriptor) -> CapabilityState,
    onSelectTool: (ToolDescriptor) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember(initialCategory) { mutableStateOf(initialCategory) }

    val toolCategories = remember { ModuleCategory.entries.filter { it.isToolCategory } }

    val filtered = remember(tools, searchQuery, selectedCategory) {
        tools.filter { tool ->
            val catMatch = selectedCategory == null || tool.category == selectedCategory
            val q = searchQuery.trim()
            val textMatch = q.isEmpty() ||
                tool.name.contains(q, ignoreCase = true) ||
                tool.summary.contains(q, ignoreCase = true) ||
                tool.category.title.contains(q, ignoreCase = true)
            catMatch && textMatch
        }
    }

    Scaffold(
        containerColor = SwordBackground,
        topBar = {
            SwordTopBar(
                title = "Modules Explorer",
                subtitle = "${filtered.size} Security Tools"
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
                    placeholder = { Text("Search modules, DNS, TLS, OSINT, forensics...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SwordPrimaryCyan) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SwordPrimaryCyan,
                        unfocusedBorderColor = SwordBorder,
                        focusedTextColor = SwordTextPrimary,
                        unfocusedTextColor = SwordTextPrimary,
                        focusedContainerColor = SwordCardSurface,
                        unfocusedContainerColor = SwordCardSurface
                    )
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { selectedCategory = null },
                            label = { Text("All") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SwordPrimaryCyan,
                                selectedLabelColor = SwordDeepNavy,
                                labelColor = SwordTextSecondary
                            )
                        )
                    }
                    items(toolCategories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = if (selectedCategory == cat) null else cat },
                            label = { Text(cat.title) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SwordPrimaryCyan,
                                selectedLabelColor = SwordDeepNavy,
                                labelColor = SwordTextSecondary
                            )
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filtered, key = { it.id }) { tool ->
                        val cap = resolveCapability(tool)
                        SwordCard(onClick = { onSelectTool(tool) }) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = tool.category.title.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SwordPrimaryCyan
                                )
                                CapabilityBadge(cap)
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(tool.name, style = MaterialTheme.typography.titleMedium, color = SwordTextPrimary)
                            Spacer(Modifier.height(4.dp))
                            Text(tool.summary, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
