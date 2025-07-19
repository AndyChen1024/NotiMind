package com.andychen.notimind.feature.summary.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.andychen.notimind.core.model.HighlightImportance
import com.andychen.notimind.core.model.NotificationCategory
import com.andychen.notimind.core.model.NotificationSummary
import com.andychen.notimind.core.model.SummaryHighlight
import com.andychen.notimind.core.model.TimePeriod
import com.andychen.notimind.feature.summary.SummaryViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate

class TimeSummaryScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockViewModel = mock<SummaryViewModel>()

    @Test
    fun emptyState_displaysCorrectMessage() {
        // Given
        val summaryState = MutableStateFlow(
            com.andychen.notimind.feature.summary.SummaryUiState(
                selectedDate = LocalDate.now(),
                isRefreshing = false
            )
        )
        val timeSummaries = MutableStateFlow(emptyList<NotificationSummary>())
        
        whenever(mockViewModel.summaryState).thenReturn(summaryState)
        whenever(mockViewModel.timeSummaries).thenReturn(timeSummaries)
        
        // When
        composeTestRule.setContent {
            TimeSummaryScreen(viewModel = mockViewModel)
        }
        
        // Then
        composeTestRule.onNodeWithText("今天还没有收集到通知摘要").assertIsDisplayed()
    }

    @Test
    fun loadedState_displaysTimePeriodSummaries() {
        // Given
        val today = LocalDate.now()
        val summaryState = MutableStateFlow(
            com.andychen.notimind.feature.summary.SummaryUiState(
                selectedDate = today,
                isRefreshing = false
            )
        )
        
        val morningSummary = NotificationSummary(
            id = "morning-today",
            period = TimePeriod.MORNING,
            date = today,
            categories = mapOf(
                NotificationCategory.PERSONAL_MESSAGE to 3,
                NotificationCategory.EMAIL to 2
            ),
            highlights = listOf(
                SummaryHighlight(
                    title = "重要邮件",
                    content = "您有一封来自工作邮箱的重要邮件",
                    category = NotificationCategory.EMAIL,
                    importance = HighlightImportance.HIGH
                )
            ),
            totalCount = 5
        )
        
        val afternoonSummary = NotificationSummary(
            id = "afternoon-today",
            period = TimePeriod.AFTERNOON,
            date = today,
            categories = mapOf(
                NotificationCategory.SOCIAL_MEDIA to 4,
                NotificationCategory.NEWS to 2
            ),
            highlights = listOf(
                SummaryHighlight(
                    title = "热门新闻",
                    content = "今日头条：最新科技动态",
                    category = NotificationCategory.NEWS,
                    importance = HighlightImportance.MEDIUM
                )
            ),
            totalCount = 6
        )
        
        val timeSummaries = MutableStateFlow(listOf(morningSummary, afternoonSummary))
        
        whenever(mockViewModel.summaryState).thenReturn(summaryState)
        whenever(mockViewModel.timeSummaries).thenReturn(timeSummaries)
        
        // When
        composeTestRule.setContent {
            TimeSummaryScreen(viewModel = mockViewModel)
        }
        
        // Then
        composeTestRule.onNodeWithText("早晨 · 5条通知").assertIsDisplayed()
        composeTestRule.onNodeWithText("下午 · 6条通知").assertIsDisplayed()
        composeTestRule.onNodeWithText("个人消息").assertIsDisplayed()
        composeTestRule.onNodeWithText("电子邮件").assertIsDisplayed()
        composeTestRule.onNodeWithText("社交媒体").assertIsDisplayed()
        composeTestRule.onNodeWithText("新闻资讯").assertIsDisplayed()
        composeTestRule.onNodeWithText("重要邮件").assertIsDisplayed()
        composeTestRule.onNodeWithText("热门新闻").assertIsDisplayed()
    }
}