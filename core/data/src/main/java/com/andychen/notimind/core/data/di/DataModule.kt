package com.andychen.notimind.core.data.di

import com.andychen.notimind.core.data.repository.NotificationRepository
import com.andychen.notimind.core.data.repository.NotificationRepositoryImpl
import com.andychen.notimind.core.data.repository.SummaryRepository
import com.andychen.notimind.core.data.repository.SummaryRepositoryImpl
import com.andychen.notimind.core.data.repository.UserPreferencesRepository
import com.andychen.notimind.core.data.repository.UserPreferencesRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    
    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        notificationRepositoryImpl: NotificationRepositoryImpl
    ): NotificationRepository
    
    @Binds
    @Singleton
    abstract fun bindSummaryRepository(
        summaryRepositoryImpl: SummaryRepositoryImpl
    ): SummaryRepository
    
    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        userPreferencesRepositoryImpl: UserPreferencesRepositoryImpl
    ): UserPreferencesRepository
}