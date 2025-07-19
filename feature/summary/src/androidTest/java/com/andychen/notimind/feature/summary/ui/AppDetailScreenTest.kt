package com.andychen.notimind.feature.summary.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.andychen.notimind.core.model.AppNotificationSummary
import com.andychen.notimind.core.model.HighlightImportance
import com.andychen.notimind.core.model.NotificationCategory
import com.andychen.notimind.core.model.SummaryHighlight
import com.andychen.notimind.feature.summary.SummaryUiState
import com.andychen.notimind.feature.summary.SummaryViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate

class AppDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockViewModel = mock<SummaryViewModel>()

    @Test
    fun appDetailScreen_showsEmptyStateWhenNoAppFound() {
        // Given
        val packageName = "com.example.app"
        val summaryState = MutableStateFlow(
            SummaryUiState(
                selectedDate = LocalDate.now(),
                isRefreshing = false
            )
        )
        val appSummaries = MutableStateFlow(emptyList<AppNotificationSummary>())
        
        whenever(mockViewModel.summaryState).thenReturn(summaryState)
        whenever(mockViewModel.appSummaries).thenReturn(appSummaries)
        
        // When
        composeTestRule.setContent {
            AppDetailScreen(
                packageName = packageName,
                onBackClick = {},
                viewModel = mockViewModel
            )
        }
        
        // Then
        composeTestRule.onNodeWithText("没有找到该应用的通知摘要").assertIsDisplayed()
        composeTestRule.onNodeWithText(packageName).assertIsDisplayed()
    }

    @Test
    fun appDetailScreen_showsAppDetails() {
        // Given
        val packageName = "com.example.app"
        val appName = "Example App"
        val summaryState = MutableStateFlow(
            SummaryUiState(
                selectedDate = LocalDate.now(),
                isRefreshing = false
            )
        )
        
        val appSummary = AppNotificationSummary(
            packageName = packageName,
            appName = appName,
            appIcon = null,
            notificationCount = 10,
            categories = mapOf(
                NotificationCategory.PERSONAL_MESSAGE to 5,
                NotificationCategory.GROUP_MESSAGE to 3,
                NotificationCategory.SYSTEM to 2
            ),
            highlights = listOf(
                SummaryHighlight(
                    title = "重要消息",
                    content = "这是一条重要的通知内容",
                    category = NotificationCategory.PERSONAL_MESSAGE,
                    importance = HighlightImportance.HIGH
                )
            )
        )
        
        val appSummaries = MutableStateFlow(listOf(appSummary))
        
        whenever(mockViewModel.summaryState).thenReturn(summaryState)
        whenever(mockViewModel.appSummaries).thenReturn(appSummaries)
        
        // When
        composeTestRule.setContent {
            AppDetailScreen(
                packageName = packageName,
                onBackClick = {},
                viewModel = mockViewModel
            )
        }
        
        // Then
        composeTestRule.onNodeWithText(appName).assertIsDisplayed()
        composeTestRule.onNodeWithText("共 10 条通知").assertIsDisplayed()
        composeTestRule.onNodeWithText("通知类别分布").assertIsDisplayed()
        composeTestRule.onNodeWithText("个人消息").assertIsDisplayed()
        composeTestRule.onNodeWithText("5").assertIsDisplayed()
        composeTestRule.onNodeWithText("群组消息").assertIsDisplayed()
        composeTestRule.onNodeWithText("3").assertIsDisplayed()
        composeTestRule.onNodeWithText("系统通知").assertIsDisplayed()
        composeTestRule.onNodeWithText("2").assertIsDisplayed()
        composeTestRule.onNodeWithText("重要内容").assertIsDisplayed()
        composeTestRule.onNodeWithText("重要消息").assertIsDisplayed()
        composeTestRule.onNodeWithText("这是一条重要的通知内容").assertIsDisplayed()
    }

    @Test
    fun appDetailScreen_callsBackClickWhenBackButtonPressed() {
        // Given
        val packageName = "com.example.app"
        val summaryState = MutableStateFlow(
            SummaryUiState(
                selectedDate = LocalDate.now(),
                isRefreshing = false
            )
        )
        val appSummaries = MutableStateFlow(emptyList<AppNotificationSummary>())
        var backClicked = false
        
        whenever(mockViewModel.summaryState).thenReturn(summaryState)
        whenever(mockViewModel.appSummaries).thenReturn(appSummaries)
        
        // When
        composeTestRule.setContent {
            AppDetailScreen(
                packageName = packageName,
                onBackClick = { backClicked = true },
                viewModel = mockViewModel
            )
        }
        
        // Click back button
        composeTestRule.onNodeWithContentDescription("返回").performClick()
        
        // Then
        assert(backClicked)
    }
}