package com.andychen.notimind.feature.permissions

import android.app.Activity
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class NotificationPermissionCheckerTest {

    private lateinit var context: Context
    private lateinit var contentResolver: ContentResolver
    private lateinit var notificationManager: NotificationManagerCompat
    private lateinit var permissionChecker: NotificationPermissionCheckerImpl

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        contentResolver = mockk(relaxed = true)
        notificationManager = mockk(relaxed = true)
        
        every { context.contentResolver } returns contentResolver
        every { context.packageName } returns "com.andychen.notimind"
        
        mockkStatic(NotificationManagerCompat::class)
        every { NotificationManagerCompat.from(any()) } returns notificationManager
        every { notificationManager.areNotificationsEnabled() } returns true
        
        permissionChecker = NotificationPermissionCheckerImpl(context)
    }

    @Test
    fun `hasNotificationPermission returns true when all permissions granted`() {
        // Given
        val componentName = ComponentName(
            "com.andychen.notimind",
            "com.andychen.notimind.feature.notification.service.NotiMindNotificationListenerService"
        )
        val enabledListeners = componentName.flattenToString()
        
        every { 
            Settings.Secure.getString(contentResolver, "enabled_notification_listeners") 
        } returns enabledListeners
        
        // When
        val result = permissionChecker.hasNotificationPermission()
        
        // Then
        assertTrue(result)
    }

    @Test
    fun `hasNotificationPermission returns false when listener permission not granted`() {
        // Given
        every { 
            Settings.Secure.getString(contentResolver, "enabled_notification_listeners") 
        } returns "com.another.app/service"
        
        // When
        val result = permissionChecker.hasNotificationPermission()
        
        // Then
        assertFalse(result)
    }

    @Test
    fun `hasNotificationPermission returns false when notification permission not granted`() {
        // Given
        val componentName = ComponentName(
            "com.andychen.notimind",
            "com.andychen.notimind.feature.notification.service.NotiMindNotificationListenerService"
        )
        val enabledListeners = componentName.flattenToString()
        
        every { 
            Settings.Secure.getString(contentResolver, "enabled_notification_listeners") 
        } returns enabledListeners
        
        every { notificationManager.areNotificationsEnabled() } returns false
        
        // When
        val result = permissionChecker.hasNotificationPermission()
        
        // Then
        assertFalse(result)
    }

    @Test
    fun `requestNotificationPermission starts notification listener settings activity`() {
        // Given
        val activity = mockk<Activity>(relaxed = true)
        val intentSlot = slot<Intent>()
        
        // When
        permissionChecker.requestNotificationPermission(activity)
        
        // Then
        verify { activity.startActivity(capture(intentSlot)) }
        assertTrue(intentSlot.captured.action == Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
    }
}