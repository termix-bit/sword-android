package com.sword.security.android.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_records")
data class ScanRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: String,
    val toolId: String,
    val toolName: String,
    val categoryId: String,
    val targetSummary: String,
    val status: String,
    val highestSeverity: String,
    val durationMs: Long,
    val timestampEpochMs: Long,
    val summaryOutput: String,
    val errorMessage: String? = null
)

@Entity(tableName = "report_metadata")
data class ReportMetadataEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val scanRecordId: Long,
    val title: String,
    val toolId: String,
    val toolName: String,
    val categoryId: String,
    val targetSummary: String,
    val highestSeverity: String,
    val findingsCount: Int,
    val jsonFileName: String,
    val htmlFileName: String,
    val createdEpochMs: Long
)

@Entity(tableName = "evidence_records")
data class EvidenceRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val detectedSignature: String,
    val sha256: String,
    val sha512: String,
    val recordedEpochMs: Long
)

@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: String,
    val title: String,
    val detail: String,
    val severity: String,
    val timestampEpochMs: Long
)
