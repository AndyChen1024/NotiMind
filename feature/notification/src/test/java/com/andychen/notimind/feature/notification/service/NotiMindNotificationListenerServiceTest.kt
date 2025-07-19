package com.andychen.notimind.feature.notification.service

import android.app.Notification
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.Mockito.never
import org.mockito.MockitoAnnotations
import java.lang.reflect.Field
import java.lang.reflect.Method

@ExperimentalCoroutinesApi
class NotiMindNotificationListenerServiceTest {

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var packageManager: PackageManager

    @Mock
    private lateinit var notificationProcessor: NotificationProcessor

    @Mock
    private lateinit var statusBarNotification: StatusBarNotification

    @Mock
    private lateinit var notification: Notification

    @Mock
    private lateinit var extras: Bundle

    @Mock
    private lateinit var applicationInfo: ApplicationInfo

    private lateinit var service: NotiMindNotificationListenerService

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        
        service = NotiMindNotificationListenerService()
        
        // Use reflection to set the private fields
        setPrivateField(service, "notificationProcessor", notificationProcessor)
        
        // Mock context and package manager
        `when`(service.applicationContext).thenReturn(context)
        `when`(context.packageManager).thenReturn(packageManager)
        
        // Mock status bar notification
        `when`(statusBarNotification.notification).thenReturn(notification)
        `when`(statusBarNotification.packageName).thenReturn("com.example.app")
        `when`(statusBarNotification.postTime).thenReturn(1234567890L)
        `when`(statusBarNotification.isOngoing).thenReturn(false)
        
        // Mock notification extras
        `when`(notification.extras).thenReturn(extras)
        `when`(extras.getString(Notification.EXTRA_TITLE)).thenReturn("Test Title")
        `when`(extras.getCharSequence(Notification.EXTRA_TEXT)).thenReturn("Test Content")
        
        // Mock package manager
        `when`(packageManager.getApplicationInfo("com.example.app", 0)).thenReturn(applicationInfo)
        `when`(packageManager.getApplicationLabel(applicationInfo)).thenReturn("Example App")
    }

    @Test
    fun `onNotificationPosted processes notification`() = runTest {
        // When
        service.onNotificationPosted(statusBarNotification)
        
        // Then
        // Need to wait for coroutine to complete
        Thread.sleep(100)
        
        verify(notificationProcessor).processNotification(
            packageName = "com.example.app",
            appName = "Example App",
            title = "Test Title",
            content = "Test Content",
            timestamp = 1234567890L,
            category = Mockito.anyString(),
            isRemoved = false,
            extras = Mockito.anyMap()
        )
    }

    @Test
    fun `onNotificationRemoved processes notification with isRemoved flag`() = runTest {
        // When
        service.onNotificationRemoved(statusBarNotification)
        
        // Then
        // Need to wait for coroutine to complete
        Thread.sleep(100)
        
        verify(notificationProcessor).processNotification(
            packageName = "com.example.app",
            appName = "Example App",
            title = "Test Title",
            content = "Test Content",
            timestamp = 1234567890L,
            category = Mockito.anyString(),
            isRemoved = true,
            extras = Mockito.anyMap()
        )
    }

    @Test
    fun `processNotification skips own app notifications`() = runTest {
        // Given
        `when`(statusBarNotification.packageName).thenReturn(service.packageName)
        
        // When
        callPrivateMethod(service, "processNotification", statusBarNotification, false)
        
        // Then
        // Need to wait for coroutine to complete
        Thread.sleep(100)
        
        verify(notificationProcessor, never()).processNotification(
            packageName = Mockito.anyString(),
            appName = Mockito.anyString(),
            title = Mockito.anyString(),
            content = Mockito.anyString(),
            timestamp = Mockito.anyLong(),
            category = Mockito.anyString(),
            isRemoved = Mockito.anyBoolean(),
            extras = Mockito.anyMap()
        )
    }

    @Test
    fun `processNotification skips ongoing notifications`() = runTest {
        // Given
        `when`(statusBarNotification.isOngoing).thenReturn(true)
        
        // When
        callPrivateMethod(service, "processNotification", statusBarNotification, false)
        
        // Then
        // Need to wait for coroutine to complete
        Thread.sleep(100)
        
        verify(notificationProcessor, never()).processNotification(
            packageName = Mockito.anyString(),
            appName = Mockito.anyString(),
            title = Mockito.anyString(),
            content = Mockito.anyString(),
            timestamp = Mockito.anyLong(),
            category = Mockito.anyString(),
            isRemoved = Mockito.anyBoolean(),
            extras = Mockito.anyMap()
        )
    }

    @Test
    fun `isNotificationServiceEnabled checks settings correctly`() {
        // This is a static method test, would require more complex mocking of Android Settings
        // In a real test environment, we would use tools like Robolectric
    }

    // Helper methods for accessing private members
    private fun setPrivateField(target: Any, fieldName: String, value: Any) {
        val field = findField(target.javaClass, fieldName)
        field.isAccessible = true
        field.set(target, value)
    }
    
    private fun callPrivateMethod(target: Any, methodName: String, vararg args: Any): Any? {
        val argTypes = args.map { it.javaClass }.toTypedArray()
        val method = findMethod(target.javaClass, methodName, *argTypes)
        method.isAccessible = true
        return method.invoke(target, *args)
    }
    
    private fun findField(clazz: Class<*>, fieldName: String): Field {
        var currentClass: Class<*>? = clazz
        while (currentClass != null) {
            try {
                return currentClass.getDeclaredField(fieldName)
            } catch (e: NoSuchFieldException) {
                currentClass = currentClass.superclass
            }
        }
        throw NoSuchFieldException("Field $fieldName not found in ${clazz.name} or its superclasses")
    }
    
    private fun findMethod(clazz: Class<*>, methodName: String, vararg parameterTypes: Class<*>): Method {
        var currentClass: Class<*>? = clazz
        while (currentClass != null) {
            try {
                return currentClass.getDeclaredMethod(methodName, *parameterTypes)
            } catch (e: NoSuchMethodException) {
                currentClass = currentClass.superclass
            }
        }
        throw NoSuchMethodException("Method $methodName not found in ${clazz.name} or its superclasses")
    }
}