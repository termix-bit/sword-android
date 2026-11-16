package com.sword.security.android

import android.app.Application
import com.sword.security.android.core.capability.CapabilityManager
import com.sword.security.android.core.capability.PermissionManager
import com.sword.security.android.core.execution.ExecutionEngine
import com.sword.security.android.core.python.PythonBridge
import com.sword.security.android.data.local.db.SwordDatabase
import com.sword.security.android.data.local.prefs.UserPreferencesRepository
import com.sword.security.android.data.reports.ReportStorageManager
import com.sword.security.android.domain.tools.CryptoToolsExecutor
import com.sword.security.android.domain.tools.CustomToolsExecutor
import com.sword.security.android.domain.tools.ForensicsToolsExecutor
import com.sword.security.android.domain.tools.NetworkToolsExecutor
import com.sword.security.android.domain.tools.OsintToolsExecutor
import com.sword.security.android.domain.tools.WebSecurityToolsExecutor
import com.sword.security.android.domain.tools.WirelessToolsExecutor

class SwordApplication : Application() {

    lateinit var database: SwordDatabase
        private set
    lateinit var preferencesRepository: UserPreferencesRepository
        private set
    lateinit var permissionManager: PermissionManager
        private set
    lateinit var pythonBridge: PythonBridge
        private set
    lateinit var capabilityManager: CapabilityManager
        private set
    lateinit var reportStorageManager: ReportStorageManager
        private set
    lateinit var executionEngine: ExecutionEngine
        private set

    override fun onCreate() {
        super.onCreate()
        database = SwordDatabase.getInstance(this)
        preferencesRepository = UserPreferencesRepository(this)
        permissionManager = PermissionManager(this)
        pythonBridge = PythonBridge(this)
        capabilityManager = CapabilityManager(this, permissionManager, pythonBridge)
        reportStorageManager = ReportStorageManager(this)

        val networkExecutor = NetworkToolsExecutor(this)
        val webExecutor = WebSecurityToolsExecutor()
        val osintExecutor = OsintToolsExecutor(pythonBridge, networkExecutor)
        val wirelessExecutor = WirelessToolsExecutor(this, permissionManager)
        val cryptoExecutor = CryptoToolsExecutor()
        val forensicsExecutor = ForensicsToolsExecutor(this, database.evidenceDao())
        val customExecutor = CustomToolsExecutor(webExecutor, osintExecutor, networkExecutor, pythonBridge)

        executionEngine = ExecutionEngine(
            capabilityManager = capabilityManager,
            preferencesRepository = preferencesRepository,
            scanDao = database.scanDao(),
            reportDao = database.reportDao(),
            activityDao = database.activityDao(),
            reportStorageManager = reportStorageManager,
            networkExecutor = networkExecutor,
            webExecutor = webExecutor,
            osintExecutor = osintExecutor,
            wirelessExecutor = wirelessExecutor,
            cryptoExecutor = cryptoExecutor,
            forensicsExecutor = forensicsExecutor,
            customExecutor = customExecutor
        )
    }
}
