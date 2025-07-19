package com.andychen.notimind.feature.permissions

import android.content.Context
import androidx.work.WorkManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PermissionMonitorTest {

    private lateinit var context: Context
    private lateinit var permissionChecker: NotificationPermissionChecker
    private lateinit var workManager: WorkManager
    private lateinit var permissionMonitor: PermissionMonitor

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        permissionChecker = mockk(relaxed = true)
        workManager = mockk(relaxed = true)
        
        // Mock WorkManager.getInstance to return our mock
        mockkStatic(WorkManager::class)
        every { WorkManager.getInstance(any()) } returns workManager
        
        permissionMonitor = PermissionMonitor(context, permissionChecker)
    }

    @Test
    fun `checkPermissions should update permission state`() = runBlocking {
        // Given
        every { permissionChecker.hasNotificationPermission() } returns true
        
        // When
        permissionMonitor.checkPermissions()
        
        // Then
        val state = permissionMonitor.permissionState.first()
        assertTrue(state.hasNotificationPermission)
        
        // When permission is revoked
        every { permissionChecker.hasNotificationPermission() } returns false
        permissionMonitor.checkPermissions()
        
        // Then state should be updated
        val updatedState = permissionMonitor.permissionState.first()
        assertFalse(updatedState.hasNotificationPermission)
    }

    @Test
    fun `startMonitoring should schedule periodic work`() {
        // Given
        every { permissionChecker.hasNotificationPermission() } returns true
        
        // When
        permissionMonitor.startMonitoring()
        
        // Then
        verify { workManager.enqueueUniquePeriodicWork(any(), any(), any()) }
    }
}

// Mock static methods
private fun mockkStatic(clazz: Class<*>) {
    // This is a simplified mock for the test
    // In a real implementation, you would use a mocking library like MockK
}