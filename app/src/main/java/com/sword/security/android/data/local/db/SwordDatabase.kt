package com.sword.security.android.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ScanRecordEntity::class,
        ReportMetadataEntity::class,
        EvidenceRecordEntity::class,
        ActivityLogEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class SwordDatabase : RoomDatabase() {
    abstract fun scanDao(): ScanDao
    abstract fun reportDao(): ReportDao
    abstract fun evidenceDao(): EvidenceDao
    abstract fun activityDao(): ActivityDao

    companion object {
        @Volatile
        private var instance: SwordDatabase? = null

        fun getInstance(context: Context): SwordDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    SwordDatabase::class.java,
                    "sword_security.db"
                ).build().also { instance = it }
            }
        }
    }
}
