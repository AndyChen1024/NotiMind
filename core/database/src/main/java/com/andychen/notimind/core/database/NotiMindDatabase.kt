package com.andychen.notimind.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.andychen.notimind.core.database.dao.NotificationDao
import com.andychen.notimind.core.database.dao.SummaryDao
import com.andychen.notimind.core.database.entity.NotificationEntity
import com.andychen.notimind.core.database.entity.NotificationExtraEntity
import com.andychen.notimind.core.database.entity.SummaryEntity
import com.andychen.notimind.core.database.util.Converters

@Database(
    entities = [
        NotificationEntity::class,
        NotificationExtraEntity::class,
        SummaryEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class NotiMindDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
    abstract fun summaryDao(): SummaryDao
    
    companion object {
        private const val DATABASE_NAME = "notimind-db"
        
        @Volatile
        private var INSTANCE: NotiMindDatabase? = null
        
        fun getInstance(context: Context): NotiMindDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NotiMindDatabase::class.java,
                    DATABASE_NAME
                )
                .fallbackToDestructiveMigration() // For now, we'll replace this with proper migrations later
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}