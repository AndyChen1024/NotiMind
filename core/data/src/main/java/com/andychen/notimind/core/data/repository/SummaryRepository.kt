package com.andychen.notimind.core.data.repository

import com.andychen.notimind.core.model.AppNotificationSummary
import com.andychen.notimind.core.model.NotificationSummary
import com.andychen.notimind.core.model.TimeRange
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface SummaryRepository {
    /**
     * 获取指定时间范围内的时间段摘要
     */
    fun getTimeSummaries(timeRange: TimeRange): Flow<List<NotificationSummary>>
    
    /**
     * 获取指定日期的时间段摘要
     */
    fun getTimeSummariesByDate(date: LocalDate): Flow<List<NotificationSummary>>
    
    /**
     * 获取指定时间范围内的应用摘要
     */
    fun getAppSummaries(timeRange: TimeRange): Flow<List<AppNotificationSummary>>
    
    /**
     * 获取指定应用在时间范围内的摘要
     */
    fun getAppSummary(packageName: String, timeRange: TimeRange): Flow<AppNotificationSummary?>
    
    /**
     * 为指定时间范围生成摘要
     */
    suspend fun generateSummaries(timeRange: TimeRange)
    
    /**
     * 为指定日期生成摘要
     */
    suspend fun generateSummariesForDate(date: LocalDate)
    
    /**
     * 清除指定日期之前的摘要
     */
    suspend fun clearSummariesOlderThan(date: LocalDate): Int
    
    /**
     * 清除所有摘要
     */
    suspend fun clearAllSummaries()
}