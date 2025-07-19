package com.andychen.notimind.core.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import com.andychen.notimind.core.data.util.SummarySerializer
import com.andychen.notimind.core.database.dao.NotificationDao
import com.andychen.notimind.core.database.dao.SummaryDao
import com.andychen.notimind.core.database.entity.NotificationEntity as DbNotificationEntity
import com.andychen.notimind.core.database.entity.SummaryEntity
import com.andychen.notimind.core.model.AppNotificationSummary
import com.andychen.notimind.core.model.NotificationCategory
import com.andychen.notimind.core.model.NotificationSummary
import com.andychen.notimind.core.model.TimePeriod
import com.andychen.notimind.core.model.TimeRange
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class SummaryRepositoryImplTest {

    private lateinit var summaryDao: SummaryDao
    private lateinit var notificationDao: NotificationDao
    private lateinit var context: Context
    private lateinit var packageManager: PackageManager
    private lateinit var repository: SummaryRepositoryImpl
    private lateinit var mockDrawable: Drawable

    @Before
    fun setup() {
        summaryDao = mock()
        notificationDao = mock()
        packageManager = mock()
        mockDrawable = mock()
        
        context = mock {
            on { packageManager } doReturn packageManager
        }
        
        repository = SummaryRepositoryImpl(summaryDao, notificationDao, context)
    }

    @Test
    fun `getTimeSummaries should return deserialized summaries`() = runTest {
        val date = LocalDate.now()
        val timestamp = SummarySerializer.dateToTimestamp(date)
        val timeRange = TimeRange(timestamp, timestamp + 86400000)
        
        val summary1 = createSummaryEntity(
            id = "2023-01-01_MORNING",
            period = "MORNING",
            date = timestamp
        )
        val summary2 = createSummaryEntity(
            id = "2023-01-01_AFTERNOON",
            period = "AFTERNOON",
            date = timestamp
        )
        
        whenever(summaryDao.getSummariesByDateRange(timeRange.startTime, timeRange.endTime))
            .thenReturn(flowOf(listOf(summary1, summary2)))
        
        val result = repository.getTimeSummaries(timeRange)
        
        result.collect { summaries ->
            assertEquals(2, summaries.size)
            assertEquals("2023-01-01_MORNING", summaries[0].id)
            assertEquals(TimePeriod.MORNING, summaries[0].period)
            assertEquals("2023-01-01_AFTERNOON", summaries[1].id)
            assertEquals(TimePeriod.AFTERNOON, summaries[1].period)
        }
    }

    @Test
    fun `getTimeSummariesByDate should return summaries for specific date`() = runTest {
        val date = LocalDate.now()
        val timestamp = SummarySerializer.dateToTimestamp(date)
        
        val summary1 = createSummaryEntity(
            id = "2023-01-01_MORNING",
            period = "MORNING",
            date = timestamp
        )
        val summary2 = createSummaryEntity(
            id = "2023-01-01_AFTERNOON",
            period = "AFTERNOON",
            date = timestamp
        )
        
        whenever(summaryDao.getSummariesByDate(timestamp))
            .thenReturn(flowOf(listOf(summary1, summary2)))
        
        val result = repository.getTimeSummariesByDate(date)
        
        result.collect { summaries ->
            assertEquals(2, summaries.size)
            assertEquals("2023-01-01_MORNING", summaries[0].id)
            assertEquals(TimePeriod.MORNING, summaries[0].period)
            assertEquals("2023-01-01_AFTERNOON", summaries[1].id)
            assertEquals(TimePeriod.AFTERNOON, summaries[1].period)
        }
    }

    @Test
    fun `getAppSummaries should return app summaries for all packages`() = runTest {
        val timeRange = TimeRange(1625097500000, 1625097800000)
        val packageNames = listOf("com.example.app1", "com.example.app2")
        
        val notifications1 = listOf(
            createDbNotification(
                id = 1,
                packageName = "com.example.app1",
                timestamp = 1625097600000,
                category = "PERSONAL_MESSAGE"
            )
        )
        
        val notifications2 = listOf(
            createDbNotification(
                id = 2,
                packageName = "com.example.app2",
                timestamp = 1625097700000,
                category = "EMAIL"
            )
        )
        
        whenever(notificationDao.getAllPackageNames()).thenReturn(flowOf(packageNames))
        whenever(notificationDao.getNotificationsByPackageAndTimeRange(
            "com.example.app1", timeRange.startTime, timeRange.endTime
        )).thenReturn(flowOf(notifications1))
        whenever(notificationDao.getNotificationsByPackageAndTimeRange(
            "com.example.app2", timeRange.startTime, timeRange.endTime
        )).thenReturn(flowOf(notifications2))
        
        try {
            whenever(packageManager.getApplicationIcon("com.example.app1")).thenReturn(mockDrawable)
            whenever(packageManager.getApplicationIcon("com.example.app2")).thenReturn(mockDrawable)
        } catch (e: PackageManager.NameNotFoundException) {
            // Ignore in test
        }
        
        val result = repository.getAppSummaries(timeRange)
        
        val summaries = result.first()
        assertEquals(2, summaries.size)
        assertEquals("com.example.app1", summaries[0].packageName)
        assertEquals("com.example.app2", summaries[1].packageName)
    }

    @Test
    fun `getAppSummary should return app summary for specific package`() = runTest {
        val timeRange = TimeRange(1625097500000, 1625097800000)
        val packageName = "com.example.app1"
        
        val notifications = listOf(
            createDbNotification(
                id = 1,
                packageName = packageName,
                timestamp = 1625097600000,
                category = "PERSONAL_MESSAGE"
            ),
            createDbNotification(
                id = 2,
                packageName = packageName,
                timestamp = 1625097700000,
                category = "EMAIL"
            )
        )
        
        whenever(notificationDao.getNotificationsByPackageAndTimeRange(
            packageName, timeRange.startTime, timeRange.endTime
        )).thenReturn(flowOf(notifications))
        
        try {
            whenever(packageManager.getApplicationIcon(packageName)).thenReturn(mockDrawable)
        } catch (e: PackageManager.NameNotFoundException) {
            // Ignore in test
        }
        
        val result = repository.getAppSummary(packageName, timeRange)
        
        val summary = result.first()
        assertNotNull(summary)
        assertEquals(packageName, summary.packageName)
        assertEquals(2, summary.notificationCount)
        assertTrue(summary.categories.containsKey(NotificationCategory.PERSONAL_MESSAGE))
        assertTrue(summary.categories.containsKey(NotificationCategory.EMAIL))
    }

    @Test
    fun `generateSummaries should generate summaries for date range`() = runTest {
        val startDate = LocalDate.of(2023, 1, 1)
        val endDate = LocalDate.of(2023, 1, 3)
        val timeRange = TimeRange(
            SummarySerializer.dateToTimestamp(startDate),
            SummarySerializer.dateToTimestamp(endDate.plusDays(1)) - 1
        )
        
        // Mock notifications for each day
        for (date in startDate..endDate) {
            val timestamp = SummarySerializer.dateToTimestamp(date)
            val nextDayTimestamp = SummarySerializer.dateToTimestamp(date.plusDays(1))
            
            val notifications = listOf(
                createDbNotification(
                    id = 1,
                    packageName = "com.example.app1",
                    timestamp = timestamp + 8 * 3600000,
                    category = "PERSONAL_MESSAGE"
                )
            )
            
            whenever(notificationDao.getNotificationsByTimeRange(timestamp, nextDayTimestamp - 1))
                .thenReturn(flowOf(notifications))
        }
        
        repository.generateSummaries(timeRange)
        
        // Verify summaries were generated for each day (3 days)
        verify(summaryDao, times(3)).insertSummary(any())
    }

    @Test
    fun `generateSummariesForDate should create summaries for each time period`() = runTest {
        val date = LocalDate.now()
        val timestamp = SummarySerializer.dateToTimestamp(date)
        val nextDayTimestamp = SummarySerializer.dateToTimestamp(date.plusDays(1))
        
        val notifications = listOf(
            createDbNotification(
                id = 1,
                packageName = "com.example.app1",
                timestamp = timestamp + 8 * 3600000, // Morning
                category = "PERSONAL_MESSAGE"
            ),
            createDbNotification(
                id = 2,
                packageName = "com.example.app2",
                timestamp = timestamp + 14 * 3600000, // Afternoon
                category = "EMAIL"
            ),
            createDbNotification(
                id = 3,
                packageName = "com.example.app1",
                timestamp = timestamp + 20 * 3600000, // Evening
                category = "SOCIAL_MEDIA"
            )
        )
        
        whenever(notificationDao.getNotificationsByTimeRange(timestamp, nextDayTimestamp - 1))
            .thenReturn(flowOf(notifications))
        
        repository.generateSummariesForDate(date)
        
        // Verify summaries were inserted (at least 4 times: morning, afternoon, evening, all_day)
        verify(summaryDao, times(4)).insertSummary(any())
    }

    @Test
    fun `clearSummariesOlderThan should call dao deleteSummariesOlderThan`() = runTest {
        val date = LocalDate.now()
        val timestamp = SummarySerializer.dateToTimestamp(date)
        
        whenever(summaryDao.deleteSummariesOlderThan(timestamp)).thenReturn(5)
        
        val result = repository.clearSummariesOlderThan(date)
        
        assertEquals(5, result)
        verify(summaryDao).deleteSummariesOlderThan(timestamp)
    }

    @Test
    fun `clearAllSummaries should call dao deleteAllSummaries`() = runTest {
        repository.clearAllSummaries()
        
        verify(summaryDao).deleteAllSummaries()
    }

    private fun createSummaryEntity(
        id: String,
        period: String,
        date: Long
    ): SummaryEntity {
        val summary = NotificationSummary(
            id = id,
            period = TimePeriod.valueOf(period),
            date = SummarySerializer.timestampToDate(date),
            categories = mapOf(NotificationCategory.PERSONAL_MESSAGE to 1),
            highlights = emptyList(),
            totalCount = 1
        )
        
        return SummaryEntity(
            id = id,
            period = period,
            date = date,
            summaryJson = SummarySerializer.serializeNotificationSummary(summary),
            generatedAt = System.currentTimeMillis()
        )
    }
    
    private fun createDbNotification(
        id: Long,
        packageName: String,
        timestamp: Long,
        category: String
    ): DbNotificationEntity {
        return DbNotificationEntity(
            id = id,
            packageName = packageName,
            appName = "App $id",
            title = "Title $id",
            content = "Content $id",
            timestamp = timestamp,
            category = category,
            isRemoved = false
        )
    }
    
    // Extension function to create a range of LocalDates
    private operator fun LocalDate.rangeTo(other: LocalDate): List<LocalDate> {
        val result = mutableListOf<LocalDate>()
        var current = this
        while (!current.isAfter(other)) {
            result.add(current)
            current = current.plusDays(1)
        }
        return result
    }
}