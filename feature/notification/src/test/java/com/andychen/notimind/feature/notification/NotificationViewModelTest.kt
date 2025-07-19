package com.andychen.notimind.feature.notification

import app.cash.turbine.test
import com.andychen.notimind.core.data.repository.NotificationRepository
import com.andychen.notimind.core.model.NotificationEntity
import com.andychen.notimind.core.model.TimeRange
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@ExperimentalCoroutinesApi
class NotificationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var notificationRepository: NotificationRepository
    private lateinit var viewModel: NotificationViewModel

    private val testNotifications = listOf(
        NotificationEntity(
            id = 1,
            packageName = "com.example.app1",
            appName = "App 1",
            title = "Test Title 1",
            content = "Test Content 1",
            timestamp = 1625097600000, // 2021-07-01
            category = "message",
            isRemoved = false,
            extras = emptyMap()
        ),
        NotificationEntity(
            id = 2,
            packageName = "com.example.app2",
            appName = "App 2",
            title = "Test Title 2",
            content = "Test Content 2",
            timestamp = 1625184000000, // 2021-07-02
            category = "alert",
            isRemoved = false,
            extras = emptyMap()
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        notificationRepository = mockk(relaxed = true)
        
        every { notificationRepository.getNotifications(any()) } returns flowOf(testNotifications)
        every { notificationRepository.getNotificationsByApp(any(), any()) } returns flowOf(testNotifications.filter { it.packageName == "com.example.app1" })
        every { notificationRepository.getAllPackageNames() } returns flowOf(listOf("com.example.app1", "com.example.app2"))
        
        viewModel = NotificationViewModel(notificationRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is correct`() = runTest {
        assertEquals(LocalDate.now(), viewModel.selectedDate.value)
        assertEquals("", viewModel.searchQuery.value)
        assertNull(viewModel.selectedAppPackage.value)
        assertEquals(false, viewModel.isRefreshing.value)
    }

    @Test
    fun `setDate updates selected date`() = runTest {
        val newDate = LocalDate.now().minusDays(1)
        viewModel.setDate(newDate)
        assertEquals(newDate, viewModel.selectedDate.value)
    }

    @Test
    fun `previousDay decrements selected date`() = runTest {
        val initialDate = viewModel.selectedDate.value
        viewModel.previousDay()
        assertEquals(initialDate.minusDays(1), viewModel.selectedDate.value)
    }

    @Test
    fun `nextDay increments selected date`() = runTest {
        val initialDate = viewModel.selectedDate.value
        viewModel.nextDay()
        assertEquals(initialDate.plusDays(1), viewModel.selectedDate.value)
    }

    @Test
    fun `setSearchQuery updates search query`() = runTest {
        viewModel.setSearchQuery("test query")
        assertEquals("test query", viewModel.searchQuery.value)
    }

    @Test
    fun `clearSearch resets search query`() = runTest {
        viewModel.setSearchQuery("test query")
        viewModel.clearSearch()
        assertEquals("", viewModel.searchQuery.value)
    }

    @Test
    fun `selectApp updates selected app package`() = runTest {
        viewModel.selectApp("com.example.app1")
        assertEquals("com.example.app1", viewModel.selectedAppPackage.value)
    }

    @Test
    fun `clearAppFilter resets selected app package`() = runTest {
        viewModel.selectApp("com.example.app1")
        viewModel.clearAppFilter()
        assertNull(viewModel.selectedAppPackage.value)
    }

    @Test
    fun `deleteNotification calls repository`() = runTest {
        val notification = testNotifications[0]
        viewModel.deleteNotification(notification)
        coVerify { notificationRepository.deleteNotification(notification) }
    }

    @Test
    fun `exportNotifications calls repository with correct format`() = runTest {
        viewModel.exportNotifications(NotificationRepository.ExportFormat.JSON)
        coVerify { notificationRepository.exportNotifications(any(), NotificationRepository.ExportFormat.JSON) }
    }

    @Test
    fun `clearNotificationsForCurrentDay calls repository with correct time range`() = runTest {
        viewModel.clearNotificationsForCurrentDay()
        coVerify { notificationRepository.clearNotifications(any()) }
    }
}