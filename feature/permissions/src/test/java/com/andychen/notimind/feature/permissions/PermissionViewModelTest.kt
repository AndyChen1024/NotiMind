package com.andychen.notimind.feature.permissions

import android.app.Activity
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class PermissionViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = TestCoroutineDispatcher()
    private lateinit var permissionChecker: NotificationPermissionChecker
    private lateinit var viewModel: PermissionViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        permissionChecker = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `init should check notification permission`() = testDispatcher.runBlockingTest {
        // Given
        every { permissionChecker.hasNotificationPermission() } returns true

        // When
        viewModel = PermissionViewModel(permissionChecker)

        // Then
        assertTrue(viewModel.permissionState.hasNotificationPermission)
        verify { permissionChecker.hasNotificationPermission() }
    }

    @Test
    fun `checkNotificationPermission should update permission state`() = testDispatcher.runBlockingTest {
        // Given
        every { permissionChecker.hasNotificationPermission() } returns false
        viewModel = PermissionViewModel(permissionChecker)
        assertFalse(viewModel.permissionState.hasNotificationPermission)

        // When
        every { permissionChecker.hasNotificationPermission() } returns true
        viewModel.checkNotificationPermission()

        // Then
        assertTrue(viewModel.permissionState.hasNotificationPermission)
    }

    @Test
    fun `requestNotificationPermission should increment denied count`() {
        // Given
        every { permissionChecker.hasNotificationPermission() } returns false
        viewModel = PermissionViewModel(permissionChecker)
        assertEquals(0, viewModel.permissionState.permissionDeniedCount)

        // When
        val mockActivity = mockk<Activity>(relaxed = true)
        viewModel.requestNotificationPermission(mockActivity)

        // Then
        assertEquals(1, viewModel.permissionState.permissionDeniedCount)
        verify { permissionChecker.requestNotificationPermission(mockActivity) }
    }
}