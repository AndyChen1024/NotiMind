package com.andychen.notimind.core.data.repository

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.andychen.notimind.core.database.dao.NotificationDao
import com.andychen.notimind.core.database.entity.NotificationEntity as DbNotificationEntity
import com.andychen.notimind.core.database.entity.NotificationExtraEntity
import com.andychen.notimind.core.model.NotificationEntity
import com.andychen.notimind.core.model.TimeRange
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.File
import java.io.FileOutputStream
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@ExperimentalCoroutinesApi
class NotificationRepositoryImplTest {

    private lateinit var notificationDao: NotificationDao
    private lateinit var context: Context
    private lateinit var repository: NotificationRepositoryImpl
    private lateinit var mockFile: File
    private lateinit var mockUri: Uri
    private lateinit var mockFileOutputStream: FileOutputStream

    @Before
    fun setup() {
        notificationDao = mock()
        mockFile = mock()
        mockUri = mock()
        mockFileOutputStream = mock()
        
        context = mock {
            on { cacheDir } doReturn File("cache")
        }
        
        whenever(context.packageName).thenReturn("com.andychen.notimind")
        whenever(FileProvider.getUriForFile(any(), any(), any())).thenReturn(mockUri)
        
        repository = NotificationRepositoryImpl(notificationDao, context)
    }

    @Test
    fun `saveNotification should call dao insertNotificationWithExtras`() = runTest {
        val notification = NotificationEntity(
            packageName = "com.example.app",
            appName = "Example App",
            title = "Test Title",
            content = "Test Content",
            timestamp = 1625097600000,
            category = "PERSONAL_MESSAGE",
            extras = mapOf("key1" to "value1", "key2" to "value2")
        )
        
        whenever(notificationDao.insertNotificationWithExtras(any(), any())).thenReturn(1L)
        
        val result = repository.saveNotification(notification)
        
        assertEquals(1L, result)
        verify(notificationDao).insertNotificationWithExtras(any(), any())
    }

    @Test
    fun `getNotificationById should return null when notification not found`() = runTest {
        whenever(notificationDao.getNotificationById(1L)).thenReturn(null)
        
        val result = repository.getNotificationById(1L)
        
        assertNull(result)
    }

    @Test
    fun `getNotificationById should return notification with extras when found`() = runTest {
        val dbNotification = DbNotificationEntity(
            id = 1L,
            packageName = "com.example.app",
            appName = "Example App",
            title = "Test Title",
            content = "Test Content",
            timestamp = 1625097600000,
            category = "PERSONAL_MESSAGE"
        )
        
        val extras = listOf(
            NotificationExtraEntity(
                id = 1L,
                notificationId = 1L,
                key = "key1",
                value = "value1"
            ),
            NotificationExtraEntity(
                id = 2L,
                notificationId = 1L,
                key = "key2",
                value = "value2"
            )
        )
        
        whenever(notificationDao.getNotificationById(1L)).thenReturn(dbNotification)
        whenever(notificationDao.getNotificationExtras(1L)).thenReturn(extras)
        
        val result = repository.getNotificationById(1L)
        
        assertNotNull(result)
        assertEquals(1L, result.id)
        assertEquals("com.example.app", result.packageName)
        assertEquals("Example App", result.appName)
        assertEquals("Test Title", result.title)
        assertEquals("Test Content", result.content)
        assertEquals(1625097600000, result.timestamp)
        assertEquals("PERSONAL_MESSAGE", result.category)
        assertEquals(2, result.extras.size)
        assertEquals("value1", result.extras["key1"])
        assertEquals("value2", result.extras["key2"])
    }

    @Test
    fun `getNotifications should return flow of notifications`() = runTest {
        val dbNotifications = listOf(
            DbNotificationEntity(
                id = 1L,
                packageName = "com.example.app",
                appName = "Example App",
                title = "Test Title 1",
                content = "Test Content 1",
                timestamp = 1625097600000,
                category = "PERSONAL_MESSAGE"
            ),
            DbNotificationEntity(
                id = 2L,
                packageName = "com.example.app",
                appName = "Example App",
                title = "Test Title 2",
                content = "Test Content 2",
                timestamp = 1625097700000,
                category = "PERSONAL_MESSAGE"
            )
        )
        
        val timeRange = TimeRange(1625097500000, 1625097800000)
        
        whenever(notificationDao.getNotificationsByTimeRange(timeRange.startTime, timeRange.endTime))
            .thenReturn(flowOf(dbNotifications))
        whenever(notificationDao.getNotificationExtras(1L)).thenReturn(emptyList())
        whenever(notificationDao.getNotificationExtras(2L)).thenReturn(emptyList())
        
        val result = repository.getNotifications(timeRange)
        
        result.collect { notifications ->
            assertEquals(2, notifications.size)
            assertEquals(1L, notifications[0].id)
            assertEquals(2L, notifications[1].id)
        }
    }

    @Test
    fun `getNotificationsByApp should return flow of notifications for specific app`() = runTest {
        val dbNotifications = listOf(
            DbNotificationEntity(
                id = 1L,
                packageName = "com.example.app",
                appName = "Example App",
                title = "Test Title 1",
                content = "Test Content 1",
                timestamp = 1625097600000,
                category = "PERSONAL_MESSAGE"
            ),
            DbNotificationEntity(
                id = 2L,
                packageName = "com.example.app",
                appName = "Example App",
                title = "Test Title 2",
                content = "Test Content 2",
                timestamp = 1625097700000,
                category = "PERSONAL_MESSAGE"
            )
        )
        
        val timeRange = TimeRange(1625097500000, 1625097800000)
        val packageName = "com.example.app"
        
        whenever(notificationDao.getNotificationsByPackageAndTimeRange(
            packageName, timeRange.startTime, timeRange.endTime
        )).thenReturn(flowOf(dbNotifications))
        whenever(notificationDao.getNotificationExtras(1L)).thenReturn(emptyList())
        whenever(notificationDao.getNotificationExtras(2L)).thenReturn(emptyList())
        
        val result = repository.getNotificationsByApp(packageName, timeRange)
        
        result.collect { notifications ->
            assertEquals(2, notifications.size)
            assertEquals(packageName, notifications[0].packageName)
            assertEquals(packageName, notifications[1].packageName)
        }
    }

    @Test
    fun `getNotificationCount should return flow of count`() = runTest {
        whenever(notificationDao.getNotificationCount()).thenReturn(flowOf(10))
        
        val result = repository.getNotificationCount()
        
        result.collect { count ->
            assertEquals(10, count)
        }
    }

    @Test
    fun `getNotificationCountByApp should return flow of count for specific app`() = runTest {
        val packageName = "com.example.app"
        whenever(notificationDao.getNotificationCountByPackage(packageName)).thenReturn(flowOf(5))
        
        val result = repository.getNotificationCountByApp(packageName)
        
        result.collect { count ->
            assertEquals(5, count)
        }
    }

    @Test
    fun `getAllPackageNames should return flow of package names`() = runTest {
        val packageNames = listOf("com.example.app1", "com.example.app2")
        whenever(notificationDao.getAllPackageNames()).thenReturn(flowOf(packageNames))
        
        val result = repository.getAllPackageNames()
        
        result.collect { names ->
            assertEquals(packageNames, names)
        }
    }

    @Test
    fun `clearNotifications should call dao deleteNotificationsOlderThan`() = runTest {
        val timeRange = TimeRange(1625097500000, 1625097800000)
        
        whenever(notificationDao.deleteNotificationsOlderThan(timeRange.endTime)).thenReturn(10)
        whenever(notificationDao.deleteNotificationsOlderThan(timeRange.startTime)).thenReturn(5)
        
        val result = repository.clearNotifications(timeRange)
        
        assertEquals(5, result)
        verify(notificationDao).deleteNotificationsOlderThan(timeRange.endTime)
        verify(notificationDao).deleteNotificationsOlderThan(timeRange.startTime)
    }

    @Test
    fun `clearAllNotifications should call dao deleteAllNotifications`() = runTest {
        repository.clearAllNotifications()
        
        verify(notificationDao).deleteAllNotifications()
    }

    @Test
    fun `clearNotificationsOlderThan should call dao deleteNotificationsOlderThan`() = runTest {
        whenever(notificationDao.deleteNotificationsOlderThan(any())).thenReturn(5)
        
        val result = repository.clearNotificationsOlderThan(30)
        
        assertEquals(5, result)
        verify(notificationDao).deleteNotificationsOlderThan(any())
    }

    @Test
    fun `updateNotification should call dao updateNotification`() = runTest {
        val notification = NotificationEntity(
            id = 1L,
            packageName = "com.example.app",
            appName = "Example App",
            title = "Updated Title",
            content = "Updated Content",
            timestamp = 1625097600000,
            category = "PERSONAL_MESSAGE"
        )
        
        repository.updateNotification(notification)
        
        verify(notificationDao).updateNotification(any())
    }

    @Test
    fun `deleteNotification should call dao deleteNotification`() = runTest {
        val notification = NotificationEntity(
            id = 1L,
            packageName = "com.example.app",
            appName = "Example App",
            title = "Test Title",
            content = "Test Content",
            timestamp = 1625097600000,
            category = "PERSONAL_MESSAGE"
        )
        
        repository.deleteNotification(notification)
        
        verify(notificationDao).deleteNotification(any())
    }
}