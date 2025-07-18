package com.andychen.notimind.ui.theme

import com.andychen.notimind.core.data.repository.UserPreferencesRepository
import com.andychen.notimind.core.model.NotificationCategory
import com.andychen.notimind.core.model.SummaryStyle
import com.andychen.notimind.core.model.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class ThemeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var viewModel: ThemeViewModel
    private val preferencesFlow = MutableStateFlow(UserPreferences())

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        userPreferencesRepository = mock()
        whenever(userPreferencesRepository.userPreferencesFlow).thenReturn(preferencesFlow)
        viewModel = ThemeViewModel(userPreferencesRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial theme state should match user preferences`() = runTest {
        // Given
        val initialPreferences = UserPreferences(isDarkTheme = false)
        preferencesFlow.value = initialPreferences
        
        // When
        val themeState = viewModel.themeState.first()
        
        // Then
        assertFalse(themeState.isDarkTheme)
        assertTrue(themeState.useDynamicColor)
    }

    @Test
    fun `toggleDarkTheme should toggle theme state`() = runTest {
        // Given
        val initialPreferences = UserPreferences(isDarkTheme = false)
        preferencesFlow.value = initialPreferences
        
        // When
        viewModel.toggleDarkTheme()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        verify(userPreferencesRepository).toggleDarkTheme(true)
    }

    @Test
    fun `setDarkTheme should set theme state explicitly`() = runTest {
        // Given
        val initialPreferences = UserPreferences(isDarkTheme = false)
        preferencesFlow.value = initialPreferences
        
        // When
        viewModel.setDarkTheme(true)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        verify(userPreferencesRepository).toggleDarkTheme(true)
    }

    @Test
    fun `theme state should update when preferences change`() = runTest {
        // Given
        val initialPreferences = UserPreferences(isDarkTheme = false)
        preferencesFlow.value = initialPreferences
        
        // When
        preferencesFlow.value = UserPreferences(isDarkTheme = true)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val themeState = viewModel.themeState.first()
        assertTrue(themeState.isDarkTheme)
    }
}