package com.sword.security.android

import com.sword.security.android.core.model.ModuleCategory
import com.sword.security.android.core.model.SwordCompatibility
import com.sword.security.android.data.repository.ModuleRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ModuleRegistryTest {

    @Test
    fun `all module IDs are unique and lookup works`() {
        val ids = ModuleRegistry.allTools.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
        assertNotNull(ModuleRegistry.findTool("net_dns_lookup"))
        assertNotNull(ModuleRegistry.findTool("osint_username_check"))
    }

    @Test
    fun `every tool category has registered modules`() {
        val toolCategories = ModuleCategory.entries.filter { it.isToolCategory }
        for (cat in toolCategories) {
            val tools = ModuleRegistry.getToolsByCategory(cat)
            assertTrue("Expected tools for ${cat.title}", tools.isNotEmpty())
        }
    }

    @Test
    fun `unsupported legacy tools are marked non-executable`() {
        val unsupported = ModuleRegistry.allTools.filter {
            it.compatibility == SwordCompatibility.UNSUPPORTED_ON_ANDROID
        }
        assertTrue(unsupported.isNotEmpty())
        unsupported.forEach { tool ->
            assertFalse("Unsupported tool ${tool.id} must not be executable", tool.isExecutable)
        }
    }
}
