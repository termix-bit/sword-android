package com.sword.security.android.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanDao {
    @Query("SELECT * FROM scan_records ORDER BY timestampEpochMs DESC")
    fun observeAllScans(): Flow<List<ScanRecordEntity>>

    @Query("SELECT * FROM scan_records ORDER BY timestampEpochMs DESC LIMIT :limit")
    fun observeRecentScans(limit: Int): Flow<List<ScanRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(record: ScanRecordEntity): Long

    @Query("DELETE FROM scan_records WHERE id = :id")
    suspend fun deleteScanById(id: Long)

    @Query("DELETE FROM scan_records")
    suspend fun deleteAllScans()
}

@Dao
interface ReportDao {
    @Query("SELECT * FROM report_metadata ORDER BY createdEpochMs DESC")
    fun observeAllReports(): Flow<List<ReportMetadataEntity>>

    @Query("SELECT * FROM report_metadata ORDER BY createdEpochMs DESC LIMIT :limit")
    fun observeRecentReports(limit: Int): Flow<List<ReportMetadataEntity>>

    @Query("SELECT * FROM report_metadata WHERE id = :id LIMIT 1")
    suspend fun getReportById(id: Long): ReportMetadataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportMetadataEntity): Long

    @Query("DELETE FROM report_metadata WHERE id = :id")
    suspend fun deleteReportById(id: Long)

    @Query("DELETE FROM report_metadata")
    suspend fun deleteAllReports()
}

@Dao
interface EvidenceDao {
    @Query("SELECT * FROM evidence_records ORDER BY recordedEpochMs DESC")
    fun observeAllEvidence(): Flow<List<EvidenceRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvidence(evidence: EvidenceRecordEntity): Long

    @Query("DELETE FROM evidence_records")
    suspend fun deleteAllEvidence()
}

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activity_logs ORDER BY timestampEpochMs DESC LIMIT :limit")
    fun observeRecentActivity(limit: Int): Flow<List<ActivityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: ActivityLogEntity): Long

    @Query("DELETE FROM activity_logs")
    suspend fun deleteAllActivity()
}
