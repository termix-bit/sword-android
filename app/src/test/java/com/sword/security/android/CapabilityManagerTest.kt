package com.sword.security.android

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.sword.security.android.core.capability.CapabilityManager
import com.sword.security.android.core.capability.PermissionManager
import com.sword.security.android.core.model.CapabilityState
import com.sword.security.android.core.python.PythonBridge
import com.sword.security.android.data.local.db.SwordDatabase
import com.sword.security.android.data.repository.ModuleRegistry
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CapabilityManagerTest {

    private lateinit var context: Context
    private lateinit var db: SwordDatabase
    private lateinit var permissionManager: PermissionManager
    private lateinit var capabilityManager: CapabilityManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, SwordDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        permissionManager = PermissionManager(context)
        val pythonBridge = PythonBridge(context)
        capabilityManager = CapabilityManager(context, permissionManager, pythonBridge)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `unsupported legacy tools resolve to UNSUPPORTED state`() = runTest {
        val rawPacketsTool = ModuleRegistry.findTool("net_raw_packets_unsupported")
        val monitorTool = ModuleRegistry.findTool("wifi_monitor_deauth_unsupported")
        assertNotNull(rawPacketsTool)
        assertNotNull(monitorTool)

        assertEquals(CapabilityState.UNSUPPORTED, capabilityManager.resolveToolCapability(rawPacketsTool!!))
        assertEquals(CapabilityState.UNSUPPORTED, capabilityManager.resolveToolCapability(monitorTool!!))
    }

    @Test
    fun `crypto and interface tools are available on standard device`() = runTest {
        val hashTool = ModuleRegistry.findTool("crypto_hash_gen")!!
        val ifaceTool = ModuleRegistry.findTool("net_interfaces")!!
        assertEquals(CapabilityState.AVAILABLE, capabilityManager.resolveToolCapability(hashTool))
        assertEquals(CapabilityState.AVAILABLE, capabilityManager.resolveToolCapability(ifaceTool))
    }

    @Test
    fun `permission manager enumerates all managed permissions`() {
        val perms = permissionManager.getAllManagedPermissions()
        assertEquals(5, perms.size)
    }
}
