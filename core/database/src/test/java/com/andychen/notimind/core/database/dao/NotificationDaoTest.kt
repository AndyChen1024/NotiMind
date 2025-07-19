package com.andychen.notimind.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.andychen.notimind.core.database.NotiMindDatabase
import com.andychen.notimind.core.database.entity.NotificationEntity
import com.andychen.notimind.core.database.entity.NotificationExtraEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class NotificationDaoTest {
    private lateinit var notificationDao: NotificationDao
    private lateinit var db: NotiMindDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, NotiMindDatabase::class.java).build()
        notificationDao = db.notificationDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetNotification() = runBlocking {
        val notification = NotificationEntity(
            packageName = "com.example.app",
            appName = "Example App",
            title = "Test Title",
            content = "Test Content",
            timestamp = System.currentTimeMillis(),
            category = "PERSONAL_MESSAGE"
        )

        val id = notificationDao.insertNotification(notification)
        val retrievedNotification = notificationDao.getNotificationById(id)

        assertNotNull(retrievedNotification)
        assertEquals(notification.packageName, retrievedNotification.packageName)
        assertEquals(notification.title, retrievedNotification.title)
        assertEquals(notification.content, retrievedNotification.content)
    }

    @Test
    fun insertNotificationWithExtras() = runBlocking {
        val notification = NotificationEntity(
            packageName = "com.example.app",
            appName = "Example App",
            title = "Test Title",
            content = "Test Content",
            timestamp = System.currentTimeMillis(),
            category = "PERSONAL_MESSAGE"
        )

        val extras = listOf(
            "key1" to "value1",
            "key2" to "value2"
        )

        val id = notificationDao.insertNotificationWithExtras(notification, extras)
        val retrievedExtras = notificationDao.getNotificationExtras(id)

        assertEquals(2, retrievedExtras.size)
        assertEquals("key1", retrievedExtras[0].key)
        assertEquals("value1", retrievedExtras[0].value)
        assertEquals("key2", retrievedExtras[1].key)
        assertEquals("value2", retrievedExtras[1].value)
    }

    @Test
    fun getNotificationsByTimeRange() = runBlocking {
        val now = System.currentTimeMillis()
        val hourInMillis = 60 * 60 * 1000L

        val notification1 = NotificationEntity(
            packageName = "com.example.app1",
            appName = "Example App 1",
            title = "Title 1",
            content = "Content 1",
            timestamp = now - hourInMillis,
            category = "PERSONAL_MESSAGE"
        )

        val notification2 = NotificationEntity(
            packageName = "com.example.app2",
            appName = "Example App 2",
            title = "Title 2",
            content = "Content 2",
            timestamp = now,
            category = "EMAIL"
        )

        val notification3 = NotificationEntity(
            packageName = "com.example.app3",
            appName = "Example App 3",
            title = "Title 3",
            content = "Content 3",
            timestamp = now + hourInMillis,
            category = "SYSTEM"
        )

        notificationDao.insertNotification(notification1)
        notificationDao.insertNotification(notification2)
        notificationDao.insertNotification(notification3)

        val notifications = notificationDao.getNotificationsByTimeRange(
            now - hourInMillis / 2,
            now + hourInMillis / 2
        ).first()

        assertEquals(1, notifications.size)
        assertEquals("Title 2", notifications[0].title)
    }

    @Test
    fun deleteNotificationsOlderThan() = runBlocking {
        val now = System.currentTimeMillis()
        val dayInMillis = 24 * 60 * 60 * 1000L

        val notification1 = NotificationEntity(
            packageName = "com.example.app1",
            appName = "Example App 1",
            title = "Old Title",
            content = "Old Content",
            timestamp = now - dayInMillis,
            category = "PERSONAL_MESSAGE"
        )

        val notification2 = NotificationEntity(
            packageName = "com.example.app2",
            appName = "Example App 2",
            title = "New Title",
            content = "New Content",
            timestamp = now,
            category = "EMAIL"
        )

        notificationDao.insertNotification(notification1)
        notificationDao.insertNotification(notification2)

        val deletedCount = notificationDao.deleteNotificationsOlderThan(now - dayInMillis / 2)
        assertEquals(1, deletedCount)

        val remainingNotifications = notificationDao.getAllNotifications().first()
        assertEquals(1, remainingNotifications.size)
        assertEquals("New Title", remainingNotifications[0].title)
    }

    @Test
    fun getNotificationsByPackage() = runBlocking {
        val notification1 = NotificationEntity(
            packageName = "com.example.app1",
            appName = "Example App 1",
            title = "Title 1",
            content = "Content 1",
            timestamp = System.currentTimeMillis(),
            category = "PERSONAL_MESSAGE"
        )

        val notification2 = NotificationEntity(
            packageName = "com.example.app2",
            appName = "Example App 2",
            title = "Title 2",
            content = "Content 2",
            timestamp = System.currentTimeMillis(),
            category = "EMAIL"
        )

        val notification3 = NotificationEntity(
            packageName = "com.example.app1",
            appName = "Example App 1",
            title = "Title 3",
            content = "Content 3",
            timestamp = System.currentTimeMillis(),
            category = "SYSTEM"
        )

        notificationDao.insertNotification(notification1)
        notificationDao.insertNotification(notification2)
        notificationDao.insertNotification(notification3)

        val app1Notifications = notificationDao.getNotificationsByPackage("com.example.app1").first()
        assertEquals(2, app1Notifications.size)
    }
}