package com.andychen.notimind.core.database.di

import android.content.Context
import androidx.room.Room
import com.andychen.notimind.core.database.NotiMindDatabase
import com.andychen.notimind.core.database.dao.NotificationDao
import com.andychen.notimind.core.database.dao.SummaryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideNotiMindDatabase(
        @ApplicationContext context: Context
    ): NotiMindDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            NotiMindDatabase::class.java,
            "notimind-db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    
    @Provides
    fun provideNotificationDao(database: NotiMindDatabase): NotificationDao {
        return database.notificationDao()
    }
    
    @Provides
    fun provideSummaryDao(database: NotiMindDatabase): SummaryDao {
        return database.summaryDao()
    }
}