package com.andychen.notimind.core.data.repository

import android.net.Uri
import com.andychen.notimind.core.model.NotificationEntity
import com.andychen.notimind.core.model.TimeRange
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    suspend fun saveNotification(notification: NotificationEntity): Long
    
    suspend fun getNotificationById(id: Long): NotificationEntity?
    
    fun getNotifications(timeRange: TimeRange): Flow<List<NotificationEntity>>
    
    fun getNotificationsByApp(appPackage: String, timeRange: TimeRange): Flow<List<NotificationEntity>>
    
    fun getNotificationCount(): Flow<Int>
    
    fun getNotificationCountByApp(appPackage: String): Flow<Int>
    
    fun getAllPackageNames(): Flow<List<String>>
    
    suspend fun clearNotifications(timeRange: TimeRange): Int
    
    suspend fun clearAllNotifications()
    
    suspend fun clearNotificationsOlderThan(days: Int): Int
    
    suspend fun exportNotifications(timeRange: TimeRange, format: ExportFormat = ExportFormat.JSON): Uri
    
    suspend fun updateNotification(notification: NotificationEntity)
    
    suspend fun deleteNotification(notification: NotificationEntity)
    
    enum class ExportFormat {
        JSON,
        CSV
    }
}