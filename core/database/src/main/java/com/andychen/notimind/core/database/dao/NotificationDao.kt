package com.andychen.notimind.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.andychen.notimind.core.database.entity.NotificationEntity
import com.andychen.notimind.core.database.entity.NotificationExtraEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificationExtras(extras: List<NotificationExtraEntity>)
    
    @Transaction
    suspend fun insertNotificationWithExtras(
        notification: NotificationEntity,
        extras: List<Pair<String, String>>
    ): Long {
        val notificationId = insertNotification(notification)
        if (extras.isNotEmpty()) {
            val extraEntities = extras.map { (key, value) ->
                NotificationExtraEntity(
                    notificationId = notificationId,
                    key = key,
                    value = value
                )
            }
            insertNotificationExtras(extraEntities)
        }
        return notificationId
    }
    
    @Update
    suspend fun updateNotification(notification: NotificationEntity)
    
    @Delete
    suspend fun deleteNotification(notification: NotificationEntity)
    
    @Query("SELECT * FROM notifications WHERE id = :id")
    suspend fun getNotificationById(id: Long): NotificationEntity?
    
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>
    
    @Query("SELECT * FROM notifications WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getNotificationsByTimeRange(startTime: Long, endTime: Long): Flow<List<NotificationEntity>>
    
    @Query("SELECT * FROM notifications WHERE packageName = :packageName ORDER BY timestamp DESC")
    fun getNotificationsByPackage(packageName: String): Flow<List<NotificationEntity>>
    
    @Query("SELECT * FROM notifications WHERE packageName = :packageName AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getNotificationsByPackageAndTimeRange(
        packageName: String,
        startTime: Long,
        endTime: Long
    ): Flow<List<NotificationEntity>>
    
    @Query("SELECT * FROM notification_extras WHERE notificationId = :notificationId")
    suspend fun getNotificationExtras(notificationId: Long): List<NotificationExtraEntity>
    
    @Query("DELETE FROM notifications WHERE timestamp < :timestamp")
    suspend fun deleteNotificationsOlderThan(timestamp: Long): Int
    
    @Query("DELETE FROM notifications")
    suspend fun deleteAllNotifications()
    
    @Query("SELECT DISTINCT packageName FROM notifications")
    fun getAllPackageNames(): Flow<List<String>>
    
    @Query("SELECT COUNT(*) FROM notifications")
    fun getNotificationCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM notifications WHERE packageName = :packageName")
    fun getNotificationCountByPackage(packageName: String): Flow<Int>
}