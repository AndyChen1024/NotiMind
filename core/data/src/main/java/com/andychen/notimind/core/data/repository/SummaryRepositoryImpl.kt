package com.andychen.notimind.core.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import com.andychen.notimind.core.data.util.SummarySerializer
import com.andychen.notimind.core.database.dao.NotificationDao
import com.andychen.notimind.core.database.dao.SummaryDao
import com.andychen.notimind.core.database.entity.SummaryEntity
import com.andychen.notimind.core.model.AppNotificationSummary
import com.andychen.notimind.core.model.HighlightImportance
import com.andychen.notimind.core.model.NotificationCategory
import com.andychen.notimind.core.model.NotificationEntity
import com.andychen.notimind.core.model.NotificationMapper
import com.andychen.notimind.core.model.NotificationSummary
import com.andychen.notimind.core.model.SummaryHighlight
import com.andychen.notimind.core.model.TimePeriod
import com.andychen.notimind.core.model.TimeRange
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SummaryRepositoryImpl @Inject constructor(
    private val summaryDao: SummaryDao,
    private val notificationDao: NotificationDao,
    @ApplicationContext private val context: Context
) : SummaryRepository {

    override fun getTimeSummaries(timeRange: TimeRange): Flow<List<NotificationSummary>> {
        return summaryDao.getSummariesByDateRange(
            timeRange.startTime,
            timeRange.endTime
        ).map { summaryEntities ->
            summaryEntities.map { entity ->
                SummarySerializer.deserializeNotificationSummary(entity.summaryJson)
            }
        }
    }

    override fun getTimeSummariesByDate(date: LocalDate): Flow<List<NotificationSummary>> {
        val timestamp = SummarySerializer.dateToTimestamp(date)
        return summaryDao.getSummariesByDate(timestamp).map { summaryEntities ->
            summaryEntities.map { entity ->
                SummarySerializer.deserializeNotificationSummary(entity.summaryJson)
            }
        }
    }

    override fun getAppSummaries(timeRange: TimeRange): Flow<List<AppNotificationSummary>> {
        return flow {
            val packageNames = notificationDao.getAllPackageNames().first()
            val appSummaries = packageNames.map { packageName ->
                getAppSummary(packageName, timeRange).first()
            }.filterNotNull()
            emit(appSummaries)
        }
    }

    override fun getAppSummary(packageName: String, timeRange: TimeRange): Flow<AppNotificationSummary?> {
        return notificationDao.getNotificationsByPackageAndTimeRange(
            packageName,
            timeRange.startTime,
            timeRange.endTime
        ).map { notifications ->
            if (notifications.isEmpty()) {
                null
            } else {
                createAppSummary(packageName, notifications.map { it.toModelEntity() })
            }
        }
    }

    override suspend fun generateSummaries(timeRange: TimeRange) {
        withContext(Dispatchers.IO) {
            val startDate = SummarySerializer.timestampToDate(timeRange.startTime)
            val endDate = SummarySerializer.timestampToDate(timeRange.endTime)
            
            var currentDate = startDate
            while (!currentDate.isAfter(endDate)) {
                generateSummariesForDate(currentDate)
                currentDate = currentDate.plusDays(1)
            }
        }
    }

    override suspend fun generateSummariesForDate(date: LocalDate) {
        withContext(Dispatchers.IO) {
            val dateTimestamp = SummarySerializer.dateToTimestamp(date)
            val nextDayTimestamp = SummarySerializer.dateToTimestamp(date.plusDays(1))
            
            // 获取当天的所有通知
            val notifications = notificationDao.getNotificationsByTimeRange(
                dateTimestamp,
                nextDayTimestamp - 1
            ).first().map { it.toModelEntity() }
            
            if (notifications.isEmpty()) {
                return@withContext
            }
            
            // 为每个时间段生成摘要
            TimePeriod.values().forEach { period ->
                if (period != TimePeriod.ALL_DAY) {
                    val periodNotifications = notifications.filter { notification ->
                        NotificationMapper.determineTimePeriod(notification.timestamp) == period
                    }
                    
                    if (periodNotifications.isNotEmpty()) {
                        val summary = generateTimePeriodSummary(date, period, periodNotifications)
                        saveSummary(summary)
                    }
                }
            }
            
            // 生成全天摘要
            val allDaySummary = generateTimePeriodSummary(date, TimePeriod.ALL_DAY, notifications)
            saveSummary(allDaySummary)
        }
    }

    override suspend fun clearSummariesOlderThan(date: LocalDate): Int {
        return withContext(Dispatchers.IO) {
            val timestamp = SummarySerializer.dateToTimestamp(date)
            summaryDao.deleteSummariesOlderThan(timestamp)
        }
    }

    override suspend fun clearAllSummaries() {
        withContext(Dispatchers.IO) {
            summaryDao.deleteAllSummaries()
        }
    }

    private suspend fun saveSummary(summary: NotificationSummary) {
        val summaryJson = SummarySerializer.serializeNotificationSummary(summary)
        val summaryEntity = SummaryEntity(
            id = summary.id,
            period = summary.period.name,
            date = SummarySerializer.dateToTimestamp(summary.date),
            summaryJson = summaryJson,
            generatedAt = System.currentTimeMillis()
        )
        summaryDao.insertSummary(summaryEntity)
    }

    private fun generateTimePeriodSummary(
        date: LocalDate,
        period: TimePeriod,
        notifications: List<NotificationEntity>
    ): NotificationSummary {
        // 计算每个类别的通知数量
        val categoryCounts = mutableMapOf<NotificationCategory, Int>()
        notifications.forEach { notification ->
            val category = notification.category?.let {
                try {
                    NotificationCategory.valueOf(it)
                } catch (e: IllegalArgumentException) {
                    NotificationCategory.OTHER
                }
            } ?: NotificationCategory.OTHER
            
            categoryCounts[category] = (categoryCounts[category] ?: 0) + 1
        }
        
        // 生成亮点
        val highlights = generateHighlights(notifications)
        
        return NotificationSummary(
            id = SummarySerializer.generateSummaryId(date, period),
            period = period,
            date = date,
            categories = categoryCounts,
            highlights = highlights,
            totalCount = notifications.size
        )
    }

    private fun generateHighlights(notifications: List<NotificationEntity>): List<SummaryHighlight> {
        // 简单的亮点生成算法，选择每个类别中最重要的通知
        val categoryHighlights = mutableMapOf<NotificationCategory, MutableList<SummaryHighlight>>()
        
        notifications.forEach { notification ->
            val category = notification.category?.let {
                try {
                    NotificationCategory.valueOf(it)
                } catch (e: IllegalArgumentException) {
                    NotificationCategory.OTHER
                }
            } ?: NotificationCategory.OTHER
            
            val importance = NotificationMapper.determineHighlightImportance(
                category,
                notification.title,
                notification.content
            )
            
            val highlight = SummaryHighlight(
                title = notification.title ?: notification.appName,
                content = notification.content ?: "",
                category = category,
                importance = importance
            )
            
            if (!categoryHighlights.containsKey(category)) {
                categoryHighlights[category] = mutableListOf()
            }
            categoryHighlights[category]?.add(highlight)
        }
        
        // 从每个类别中选择最多3个最重要的亮点
        val result = mutableListOf<SummaryHighlight>()
        categoryHighlights.forEach { (_, highlights) ->
            highlights.sortByDescending { it.importance }
            result.addAll(highlights.take(3))
        }
        
        // 最多返回10个亮点
        return result.sortedByDescending { it.importance }.take(10)
    }

    private fun createAppSummary(
        packageName: String,
        notifications: List<NotificationEntity>
    ): AppNotificationSummary {
        // 获取应用名称和图标
        val appName = notifications.firstOrNull()?.appName ?: packageName
        val appIcon = try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
        
        // 计算每个类别的通知数量
        val categoryCounts = mutableMapOf<NotificationCategory, Int>()
        notifications.forEach { notification ->
            val category = notification.category?.let {
                try {
                    NotificationCategory.valueOf(it)
                } catch (e: IllegalArgumentException) {
                    NotificationCategory.OTHER
                }
            } ?: NotificationCategory.OTHER
            
            categoryCounts[category] = (categoryCounts[category] ?: 0) + 1
        }
        
        // 生成亮点
        val highlights = generateHighlights(notifications)
        
        return AppNotificationSummary(
            packageName = packageName,
            appName = appName,
            appIcon = appIcon,
            notificationCount = notifications.size,
            categories = categoryCounts,
            highlights = highlights
        )
    }
    
    private fun com.andychen.notimind.core.database.entity.NotificationEntity.toModelEntity(): NotificationEntity {
        return NotificationEntity(
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
}