package com.andychen.notimind.core.data.repository

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.andychen.notimind.core.database.dao.NotificationDao
import com.andychen.notimind.core.database.entity.NotificationExtraEntity
import com.andychen.notimind.core.model.NotificationEntity as ModelNotificationEntity
import com.andychen.notimind.core.database.entity.NotificationEntity as DbNotificationEntity
import com.andychen.notimind.core.model.TimeRange
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val notificationDao: NotificationDao,
    @ApplicationContext private val context: Context
) : NotificationRepository {

    override suspend fun saveNotification(notification: ModelNotificationEntity): Long {
        return withContext(Dispatchers.IO) {
            try {
                val dbNotification = notification.toDbEntity()
                val extras = notification.extras.toList()
                notificationDao.insertNotificationWithExtras(dbNotification, extras)
            } catch (e: Exception) {
                throw RepositoryException("Failed to save notification", e)
            }
        }
    }

    override suspend fun getNotificationById(id: Long): ModelNotificationEntity? {
        return withContext(Dispatchers.IO) {
            try {
                val notification = notificationDao.getNotificationById(id) ?: return@withContext null
                val extras = notificationDao.getNotificationExtras(id)
                notification.toModelEntity(extras)
            } catch (e: Exception) {
                throw RepositoryException("Failed to get notification by ID", e)
            }
        }
    }

    override fun getNotifications(timeRange: TimeRange): Flow<List<ModelNotificationEntity>> {
        return try {
            notificationDao.getNotificationsByTimeRange(timeRange.startTime, timeRange.endTime)
                .map { notifications ->
                    notifications.map { notification ->
                        withContext(Dispatchers.IO) {
                            val extras = notificationDao.getNotificationExtras(notification.id)
                            notification.toModelEntity(extras)
                        }
                    }
                }
        } catch (e: Exception) {
            throw RepositoryException("Failed to get notifications", e)
        }
    }

    override fun getNotificationsByApp(
        appPackage: String,
        timeRange: TimeRange
    ): Flow<List<ModelNotificationEntity>> {
        return try {
            notificationDao.getNotificationsByPackageAndTimeRange(
                appPackage,
                timeRange.startTime,
                timeRange.endTime
            ).map { notifications ->
                notifications.map { notification ->
                    withContext(Dispatchers.IO) {
                        val extras = notificationDao.getNotificationExtras(notification.id)
                        notification.toModelEntity(extras)
                    }
                }
            }
        } catch (e: Exception) {
            throw RepositoryException("Failed to get notifications by app", e)
        }
    }

    override fun getNotificationCount(): Flow<Int> {
        return notificationDao.getNotificationCount()
    }

    override fun getNotificationCountByApp(appPackage: String): Flow<Int> {
        return notificationDao.getNotificationCountByPackage(appPackage)
    }

    override fun getAllPackageNames(): Flow<List<String>> {
        return notificationDao.getAllPackageNames()
    }

    override suspend fun clearNotifications(timeRange: TimeRange): Int {
        return withContext(Dispatchers.IO) {
            try {
                notificationDao.deleteNotificationsOlderThan(timeRange.endTime) -
                        notificationDao.deleteNotificationsOlderThan(timeRange.startTime)
            } catch (e: Exception) {
                throw RepositoryException("Failed to clear notifications", e)
            }
        }
    }

    override suspend fun clearAllNotifications() {
        withContext(Dispatchers.IO) {
            try {
                notificationDao.deleteAllNotifications()
            } catch (e: Exception) {
                throw RepositoryException("Failed to clear all notifications", e)
            }
        }
    }

    override suspend fun clearNotificationsOlderThan(days: Int): Int {
        return withContext(Dispatchers.IO) {
            try {
                val cutoffTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
                notificationDao.deleteNotificationsOlderThan(cutoffTime)
            } catch (e: Exception) {
                throw RepositoryException("Failed to clear old notifications", e)
            }
        }
    }

    override suspend fun exportNotifications(
        timeRange: TimeRange,
        format: NotificationRepository.ExportFormat
    ): Uri {
        return withContext(Dispatchers.IO) {
            try {
                val notificationsFlow = notificationDao.getNotificationsByTimeRange(
                    timeRange.startTime,
                    timeRange.endTime
                )
                val notifications = notificationsFlow.map { notifications ->
                    notifications.map { notification ->
                        val extras = notificationDao.getNotificationExtras(notification.id)
                        notification.toModelEntity(extras)
                    }
                }.first()

                val timestamp = DateTimeFormatter
                    .ofPattern("yyyyMMdd_HHmmss")
                    .format(LocalDateTime.now())
                
                val fileName = when (format) {
                    NotificationRepository.ExportFormat.JSON -> "notifications_$timestamp.json"
                    NotificationRepository.ExportFormat.CSV -> "notifications_$timestamp.csv"
                }

                val file = File(context.cacheDir, fileName)
                FileOutputStream(file).use { outputStream ->
                    when (format) {
                        NotificationRepository.ExportFormat.JSON -> {
                            val jsonArray = JSONArray()
                            notifications.forEach { notification ->
                                val jsonObject = JSONObject().apply {
                                    put("id", notification.id)
                                    put("packageName", notification.packageName)
                                    put("appName", notification.appName)
                                    put("title", notification.title ?: "")
                                    put("content", notification.content ?: "")
                                    put("timestamp", notification.timestamp)
                                    put("category", notification.category ?: "")
                                    put("isRemoved", notification.isRemoved)
                                    
                                    val extrasObject = JSONObject()
                                    notification.extras.forEach { (key, value) ->
                                        extrasObject.put(key, value)
                                    }
                                    put("extras", extrasObject)
                                }
                                jsonArray.put(jsonObject)
                            }
                            outputStream.write(jsonArray.toString(2).toByteArray())
                        }
                        NotificationRepository.ExportFormat.CSV -> {
                            val header = "id,packageName,appName,title,content,timestamp,category,isRemoved\n"
                            outputStream.write(header.toByteArray())
                            
                            notifications.forEach { notification ->
                                val line = "${notification.id}," +
                                        "\"${notification.packageName}\"," +
                                        "\"${notification.appName}\"," +
                                        "\"${notification.title?.replace("\"", "\"\"")}\"," +
                                        "\"${notification.content?.replace("\"", "\"\"")}\"," +
                                        "${notification.timestamp}," +
                                        "\"${notification.category}\"," +
                                        "${notification.isRemoved}\n"
                                outputStream.write(line.toByteArray())
                            }
                        }
                    }
                }

                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            } catch (e: Exception) {
                throw RepositoryException("Failed to export notifications", e)
            }
        }
    }

    override suspend fun updateNotification(notification: ModelNotificationEntity) {
        withContext(Dispatchers.IO) {
            try {
                val dbNotification = notification.toDbEntity()
                notificationDao.updateNotification(dbNotification)
                
                // Handle extras update if needed
                // This is a simplified implementation; a more complete one would update extras too
            } catch (e: Exception) {
                throw RepositoryException("Failed to update notification", e)
            }
        }
    }

    override suspend fun deleteNotification(notification: ModelNotificationEntity) {
        withContext(Dispatchers.IO) {
            try {
                val dbNotification = notification.toDbEntity()
                notificationDao.deleteNotification(dbNotification)
            } catch (e: Exception) {
                throw RepositoryException("Failed to delete notification", e)
            }
        }
    }

    private fun DbNotificationEntity.toModelEntity(extras: List<NotificationExtraEntity>): ModelNotificationEntity {
        val extrasMap = extras.associate { it.key to it.value }
        return ModelNotificationEntity(
            id = id,
            packageName = packageName,
            appName = appName,
            title = title,
            content = content,
            timestamp = timestamp,
            category = category,
            isRemoved = isRemoved,
            extras = extrasMap
        )
    }

    private fun ModelNotificationEntity.toDbEntity(): DbNotificationEntity {
        return DbNotificationEntity(
            id = id,
            packageName = packageName,
            appName = appName,
            title = title,
            content = content,
            timestamp = timestamp,
            category = category,
            isRemoved = isRemoved
        )
    }

    class RepositoryException(message: String, cause: Throwable? = null) : Exception(message, cause)
}