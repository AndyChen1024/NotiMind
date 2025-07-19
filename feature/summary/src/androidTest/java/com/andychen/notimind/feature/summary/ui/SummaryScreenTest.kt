package com.andychen.notimind.feature.summary.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.andychen.notimind.core.model.AppNotificationSummary
import com.andychen.notimind.core.model.NotificationCategory
import com.andychen.notimind.core.model.NotificationSummary
import com.andychen.notimind.core.model.SummaryHighlight
import com.andychen.notimind.core.model.SummaryStyle
import com.andychen.notimind.core.model.TimePeriod
import com.andychen.notimind.feature.summary.SummaryUiState
import com.andychen.notimind.feature.summary.SummaryViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate

class SummaryScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockViewModel = mock<SummaryViewModel>()

    @Test
    fun summaryScreen_showsTimeSummaryByDefault() {
        // Given
        val summaryState = MutableStateFlow(
            SummaryUiState(
                summaryStyle = SummaryStyle.TIME_BASED,
                selectedDate = LocalDate.now(),
                isRefreshing = false
            )
        )
        val timeSummaries = MutableStateFlow(emptyList<NotificationSummary>())
        val appSummaries = MutableStateFlow(emptyList<AppNotificationSummary>())
        
        whenever(mockViewModel.summaryState).thenReturn(summaryState)
        whenever(mockViewModel.timeSummaries).thenReturn(timeSummaries)
        whenever(mockViewModel.appSummaries).thenReturn(appSummaries)
        
        // When
        composeTestRule.setContent {
            SummaryScreen(viewModel = mockViewModel)
        }
        
        // Then
        composeTestRule.onNodeWithText("时间").assertIsDisplayed()
        composeTestRule.onNodeWithText("应用").assertIsDisplayed()
        composeTestRule.onNodeWithText("时间摘要").assertIsDisplayed()
    }

    @Test
    fun summaryScreen_switchesToAppSummary() {
        // Given
        val summaryState = MutableStateFlow(
            SummaryUiState(
                summaryStyle = SummaryStyle.TIME_BASED,
                selectedDate = LocalDate.now(),
                isRefreshing = false
            )
        )
        val timeSummaries = MutableStateFlow(emptyList<NotificationSummary>())
        val appSummaries = MutableStateFlow(emptyList<AppNotificationSummary>())
        
        whenever(mockViewModel.summaryState).thenReturn(summaryState)
        whenever(mockViewModel.timeSummaries).thenReturn(timeSummaries)
        whenever(mockViewModel.appSummaries).thenReturn(appSummaries)
        
        // When
        composeTestRule.setContent {
            SummaryScreen(viewModel = mockViewModel)
        }
        
        // Click on App tab
        composeTestRule.onNodeWithText("应用").performClick()
        
        // Then
        composeTestRule.onNodeWithText("应用摘要").assertIsDisplayed()
        
        // Verify that the view model was called to update the summary style
        verify(mockViewModel).setSummaryStyle(SummaryStyle.APP_BASED)
    }

    @Test
    fun summaryScreen_respectsUserPreference() {
        // Given
        val summaryState = MutableStateFlow(
            SummaryUiState(
                summaryStyle = SummaryStyle.APP_BASED,
                selectedDate = LocalDate.now(),
                isRefreshing = false
            )
        )
        val timeSummaries = MutableStateFlow(emptyList<NotificationSummary>())
        val appSummaries = MutableStateFlow(emptyList<AppNotificationSummary>())
        
        whenever(mockViewModel.summaryState).thenReturn(summaryState)
        whenever(mockViewModel.timeSummaries).thenReturn(timeSummaries)
        whenever(mockViewModel.appSummaries).thenReturn(appSummaries)
        
        // When
        composeTestRule.setContent {
            SummaryScreen(viewModel = mockViewModel)
        }
        
        // Then
        composeTestRule.onNodeWithText("应用摘要").assertIsDisplayed()
    }
}