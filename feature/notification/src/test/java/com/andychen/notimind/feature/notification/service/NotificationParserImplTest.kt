package com.andychen.notimind.feature.notification.service

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.andychen.notimind.core.database.dao.NotificationDao
import com.andychen.notimind.core.database.entity.NotificationEntity
import com.andychen.notimind.core.model.NotificationCategory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class NotificationParserImplTest {

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var notificationDao: NotificationDao

    @Mock
    private lateinit var packageManager: PackageManager

    @Mock
    private lateinit var applicationInfo: ApplicationInfo

    @Captor
    private lateinit var notificationEntityCaptor: ArgumentCaptor<NotificationEntity>

    @Captor
    private lateinit var extrasCaptor: ArgumentCaptor<List<Pair<String, String>>>

    private lateinit var notificationParser: NotificationParserImpl

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        `when`(context.packageManager).thenReturn(packageManager)
        notificationParser = NotificationParserImpl(context, notificationDao)
    }

    @Test
    fun `processNotification stores notification in database`() = runTest {
        // Given
        val packageName = "com.example.app"
        val appName = "Example App"
        val title = "Test Title"
        val content = "Test Content"
        val timestamp = 1234567890L
        val category = NotificationCategory.PERSONAL_MESSAGE.name
        val isRemoved = false
        val extras = mapOf("key1" to "value1", "key2" to "value2")

        // When
        notificationParser.processNotification(
            packageName = packageName,
            appName = appName,
            title = title,
            content = content,
            timestamp = timestamp,
            category = category,
            isRemoved = isRemoved,
            extras = extras
        )

        // Then
        verify(notificationDao).insertNotificationWithExtras(
            Mockito.any(NotificationEntity::class.java),
            Mockito.anyList()
        )
    }

    @Test
    fun `resolveAppName returns app name from package manager`() {
        // Given
        val packageName = "com.example.app"
        val expectedAppName = "Example App"
        
        `when`(packageManager.getApplicationInfo(packageName, 0)).thenReturn(applicationInfo)
        `when`(packageManager.getApplicationLabel(applicationInfo)).thenReturn(expectedAppName)

        // When
        val result = notificationParser.resolveAppName(packageName)

        // Then
        assert(result == expectedAppName)
    }

    @Test
    fun `resolveAppName returns package name when app not found`() {
        // Given
        val packageName = "com.unknown.app"
        
        `when`(packageManager.getApplicationInfo(packageName, 0))
            .thenThrow(PackageManager.NameNotFoundException())

        // When
        val result = notificationParser.resolveAppName(packageName)

        // Then
        assert(result == packageName)
    }

    @Test
    fun `resolveAppName uses cache for repeated calls`() {
        // Given
        val packageName = "com.example.app"
        val expectedAppName = "Example App"
        
        `when`(packageManager.getApplicationInfo(packageName, 0)).thenReturn(applicationInfo)
        `when`(packageManager.getApplicationLabel(applicationInfo)).thenReturn(expectedAppName)

        // When
        val result1 = notificationParser.resolveAppName(packageName)
        
        // Change the mock behavior to verify cache is used
        `when`(packageManager.getApplicationLabel(applicationInfo)).thenReturn("Different Name")
        
        val result2 = notificationParser.resolveAppName(packageName)

        // Then
        assert(result1 == expectedAppName)
        assert(result2 == expectedAppName) // Should still return the cached value
    }

    @Test
    fun `determineNotificationCategory returns correct category`() {
        // Test cases for different notification types
        val testCases = listOf(
            Triple("com.whatsapp", "Chat message", "Hello") to NotificationCategory.PERSONAL_MESSAGE,
            Triple("com.gmail", "New email", "Work update") to NotificationCategory.EMAIL,
            Triple("com.facebook", "New post", "Check this out") to NotificationCategory.SOCIAL_MEDIA,
            Triple("com.nytimes", "Breaking news", "Latest update") to NotificationCategory.NEWS,
            Triple("com.amazon", "50% off sale", "Discount") to NotificationCategory.PROMOTION,
            Triple("com.android.system", "System update", "Available") to NotificationCategory.SYSTEM,
            Triple("com.example", "Urgent alert", "Action required") to NotificationCategory.ALERT,
            Triple("com.unknown", "Random title", "Random content") to NotificationCategory.OTHER
        )

        for ((input, expectedCategory) in testCases) {
            val (packageName, title, content) = input
            val result = notificationParser.determineNotificationCategory(packageName, title, content)
            assert(result == expectedCategory) {
                "Expected $expectedCategory for ($packageName, $title, $content) but got $result"
            }
        }
    }
}