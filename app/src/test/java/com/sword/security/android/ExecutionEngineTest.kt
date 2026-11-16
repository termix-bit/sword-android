package com.sword.security.android

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.sword.security.android.core.capability.CapabilityManager
import com.sword.security.android.core.capability.PermissionManager
import com.sword.security.android.core.execution.ExecutionEngine
import com.sword.security.android.core.model.ExecutionErrorType
import com.sword.security.android.core.model.ExecutionStatus
import com.sword.security.android.core.python.PythonBridge
import com.sword.security.android.data.local.db.SwordDatabase
import com.sword.security.android.data.local.prefs.UserPreferencesRepository
import com.sword.security.android.data.reports.ReportStorageManager
import com.sword.security.android.data.repository.ModuleRegistry
import com.sword.security.android.domain.tools.CryptoToolsExecutor
import com.sword.security.android.domain.tools.CustomToolsExecutor
import com.sword.security.android.domain.tools.ForensicsToolsExecutor
import com.sword.security.android.domain.tools.NetworkToolsExecutor
import com.sword.security.android.domain.tools.OsintToolsExecutor
import com.sword.security.android.domain.tools.WebSecurityToolsExecutor
import com.sword.security.android.domain.tools.WirelessToolsExecutor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExecutionEngineTest {

    private lateinit var context: Context
    private lateinit var db: SwordDatabase
    private lateinit var engine: ExecutionEngine
    private lateinit var reportStorage: ReportStorageManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, SwordDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val prefs = UserPreferencesRepository(context)
        val perm = PermissionManager(context)
        val py = PythonBridge(context)
        val cap = CapabilityManager(context, perm, py)
        reportStorage = ReportStorageManager(context)
        val net = NetworkToolsExecutor(context)
        val web = WebSecurityToolsExecutor()
        val osint = OsintToolsExecutor(py, net)
        val wifi = WirelessToolsExecutor(context, perm)
        val crypto = CryptoToolsExecutor()
        val forensics = ForensicsToolsExecutor(context, db.evidenceDao())
        val custom = CustomToolsExecutor(web, osint, net, py)

        engine = ExecutionEngine(
            capabilityManager = cap,
            preferencesRepository = prefs,
            scanDao = db.scanDao(),
            reportDao = db.reportDao(),
            activityDao = db.activityDao(),
            reportStorageManager = reportStorage,
            networkExecutor = net,
            webExecutor = web,
            osintExecutor = osint,
            wirelessExecutor = wifi,
            cryptoExecutor = crypto,
            forensicsExecutor = forensics,
            customExecutor = custom
        )
    }

    @After
    fun tearDown() = runBlocking {
        reportStorage.deleteAllReportFiles()
        db.close()
    }

    @Test
    fun `execution engine runs crypto tool and persists scan and report`() = runBlocking {
        val tool = ModuleRegistry.findTool("crypto_hash_gen")!!
        engine.startExecution(tool, mapOf("text" to "sword-android"))

        val finalState = waitForCompletion()
        assertEquals(ExecutionStatus.COMPLETED, finalState.status)
        assertNotNull(finalState.result?.reportId)
        assertTrue(finalState.result!!.rawOutput.contains("SHA-256"))

        val scans = db.scanDao().observeAllScans().first()
        val reports = db.reportDao().observeAllReports().first()
        assertEquals(1, scans.size)
        assertEquals(1, reports.size)
    }

    @Test
    fun `execution engine classifies unsupported tool error properly`() = runBlocking {
        val tool = ModuleRegistry.findTool("wifi_monitor_deauth_unsupported")!!
        engine.startExecution(tool, emptyMap())

        val finalState = waitForCompletion()
        assertEquals(ExecutionStatus.FAILED, finalState.status)
        assertEquals(ExecutionErrorType.CAPABILITY_UNAVAILABLE, finalState.result?.errorType)
    }

    @Test
    fun `execution engine classifies validation error properly`() = runBlocking {
        val tool = ModuleRegistry.findTool("net_dns_lookup")!!
        engine.startExecution(tool, mapOf("domain" to "bad domain!!", "record_type" to "A"))

        val finalState = waitForCompletion()
        assertEquals(ExecutionStatus.FAILED, finalState.status)
        assertEquals(ExecutionErrorType.VALIDATION_ERROR, finalState.result?.errorType)
    }

    private suspend fun waitForCompletion() = run {
        repeat(60) {
            val state = engine.progress.value
            if (state.status == ExecutionStatus.COMPLETED ||
                state.status == ExecutionStatus.FAILED ||
                state.status == ExecutionStatus.CANCELLED
            ) {
                return@run state
            }
            delay(100)
        }
        engine.progress.value
    }
}
