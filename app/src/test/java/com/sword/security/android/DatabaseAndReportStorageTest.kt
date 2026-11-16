package com.sword.security.android

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.sword.security.android.core.model.ExecutionResult
import com.sword.security.android.core.model.ModuleCategory
import com.sword.security.android.core.model.SecurityFinding
import com.sword.security.android.core.model.SeverityLevel
import com.sword.security.android.data.local.db.ReportMetadataEntity
import com.sword.security.android.data.local.db.ScanRecordEntity
import com.sword.security.android.data.local.db.SwordDatabase
import com.sword.security.android.data.reports.ReportStorageManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
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
class DatabaseAndReportStorageTest {

    private lateinit var context: Context
    private lateinit var db: SwordDatabase
    private lateinit var reportStorage: ReportStorageManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, SwordDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        reportStorage = ReportStorageManager(context)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `room scan and report persistence and html generation work end to end`() = runTest {
        val result = ExecutionResult(
            taskId = "t12345",
            toolId = "web_header_audit",
            toolName = "HTTP Security Header Analyzer",
            category = ModuleCategory.WEB_SECURITY,
            targetSummary = "https://example.com",
            success = true,
            durationMs = 142L,
            timestampEpochMs = System.currentTimeMillis(),
            rawOutput = "HTTP Status: 200 OK",
            findings = listOf(
                SecurityFinding(
                    title = "Missing HSTS",
                    severity = "Medium",
                    detail = "HSTS header absent.",
                    recommendation = "Enable HSTS."
                )
            ),
            highestSeverity = SeverityLevel.MEDIUM
        )

        val (jsonName, htmlName) = reportStorage.saveReportFiles(result)
        assertTrue(jsonName.endsWith(".json"))
        assertTrue(htmlName.endsWith(".html"))

        val parsed = reportStorage.readSavedReport(jsonName)
        assertNotNull(parsed)
        assertEquals("https://example.com", parsed?.target)

        val scanId = db.scanDao().insertScan(
            ScanRecordEntity(
                taskId = result.taskId,
                toolId = result.toolId,
                toolName = result.toolName,
                categoryId = result.category.id,
                targetSummary = result.targetSummary,
                status = "COMPLETED",
                highestSeverity = result.highestSeverity.label,
                durationMs = result.durationMs,
                timestampEpochMs = result.timestampEpochMs,
                summaryOutput = result.rawOutput
            )
        )
        db.reportDao().insertReport(
            ReportMetadataEntity(
                scanRecordId = scanId,
                title = "Audit — example.com",
                toolId = result.toolId,
                toolName = result.toolName,
                categoryId = result.category.id,
                targetSummary = result.targetSummary,
                highestSeverity = result.highestSeverity.label,
                findingsCount = 1,
                jsonFileName = jsonName,
                htmlFileName = htmlName,
                createdEpochMs = result.timestampEpochMs
            )
        )

        val scans = db.scanDao().observeAllScans().first()
        val reports = db.reportDao().observeAllReports().first()
        assertEquals(1, scans.size)
        assertEquals(1, reports.size)

        reportStorage.deleteReportFiles(jsonName, htmlName)
        db.reportDao().deleteAllReports()
        assertEquals(0, db.reportDao().observeAllReports().first().size)
    }
}
