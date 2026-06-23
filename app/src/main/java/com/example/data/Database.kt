package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        AppSettings::class,
        ContentBrief::class,
        SystemEvent::class,
        CostEntry::class,
        EngagementSnapshot::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appSettingsDao(): AppSettingsDao
    abstract fun contentBriefDao(): ContentBriefDao
    abstract fun systemEventDao(): SystemEventDao
    abstract fun costEntryDao(): CostEntryDao
    abstract fun engagementSnapshotDao(): EngagementSnapshotDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aether_api_auto_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
