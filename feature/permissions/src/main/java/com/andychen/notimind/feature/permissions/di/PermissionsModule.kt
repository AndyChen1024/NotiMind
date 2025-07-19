package com.andychen.notimind.feature.permissions.di

import com.andychen.notimind.feature.permissions.NotificationPermissionChecker
import com.andychen.notimind.feature.permissions.NotificationPermissionCheckerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PermissionsModule {
    
    @Binds
    @Singleton
    abstract fun bindNotificationPermissionChecker(
        impl: NotificationPermissionCheckerImpl
    ): NotificationPermissionChecker
}