package com.andychen.notimind.feature.notification.di

import com.andychen.notimind.feature.notification.service.NotificationParserImpl
import com.andychen.notimind.feature.notification.service.NotificationProcessor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {
    
    @Binds
    @Singleton
    abstract fun bindNotificationProcessor(
        notificationParserImpl: NotificationParserImpl
    ): NotificationProcessor
}