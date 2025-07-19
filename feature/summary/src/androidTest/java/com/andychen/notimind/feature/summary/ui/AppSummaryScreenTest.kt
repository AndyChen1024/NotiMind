package com.andychen.notimind.feature.summary.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.andychen.notimind.core.model.AppNotificationSummary
import com.andychen.notimind.core.model.HighlightImportance
import com.andychen.notimind.core.model.NotificationCategory
import com.andychen.notimind.core.model.SummaryHighlight
import com.andychen.notimind.feature.summary.SummaryViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate

class AppSummaryScreenTest {

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
        val appSummaries = MutableStateFlow(emptyList<AppNotificationSummary>())
        
        whenever(mockViewModel.summaryState).thenReturn(summaryState)
        whenever(mockViewModel.appSummaries).thenReturn(appSummaries)
        
        // When
        composeTestRule.setContent {
            AppSummaryScreen(viewModel = mockViewModel)
        }
        
        // Then
        composeTestRule.onNodeWithText("没有应用通知摘要").assertIsDisplayed()
    }

    @Test
    fun loadedState_displaysAppSummaries() {
        // Given
        val summaryState = MutableStateFlow(
            com.andychen.notimind.feature.summary.SummaryUiState(
                selectedDate = LocalDate.now(),
                isRefreshing = false
            )
        )
        
        val wechatSummary = AppNotificationSummary(
            packageName = "com.tencent.mm",
            appName = "微信",
            appIcon = null,
            notificationCount = 15,
            categories = mapOf(
                NotificationCategory.PERSONAL_MESSAGE to 10,
                NotificationCategory.GROUP_MESSAGE to 5
            ),
            highlights = listOf(
                SummaryHighlight(
                    title = "张三",
                    content = "你好，我们今天下午开会",
                    category = NotificationCategory.PERSONAL_MESSAGE,
                    importance = HighlightImportance.HIGH
                )
            )
        )
        
        val mailSummary = AppNotificationSummary(
            packageName = "com.google.android.gm",
            appName = "Gmail",
            appIcon = null,
            notificationCount = 8,
            categories = mapOf(
                NotificationCategory.EMAIL to 8
            ),
            highlights = listOf(
                SummaryHighlight(
                    title = "工作邮件",
                    content = "请查看附件中的项目报告",
                    category = NotificationCategory.EMAIL,
                    importance = HighlightImportance.MEDIUM
                )
            )
        )
        
        val appSummaries = MutableStateFlow(listOf(wechatSummary, mailSummary))
        
        whenever(mockViewModel.summaryState).thenReturn(summaryState)
        whenever(mockViewModel.appSummaries).thenReturn(appSummaries)
        
        // When
        composeTestRule.setContent {
            AppSummaryScreen(viewModel = mockViewModel)
        }
        
        // Then
        composeTestRule.onNodeWithText("微信").assertIsDisplayed()
        composeTestRule.onNodeWithText("15条通知").assertIsDisplayed()
        composeTestRule.onNodeWithText("个人消息").assertIsDisplayed()
        composeTestRule.onNodeWithText("10").assertIsDisplayed()
        composeTestRule.onNodeWithText("群组消息").assertIsDisplayed()
        composeTestRule.onNodeWithText("5").assertIsDisplayed()
        composeTestRule.onNodeWithText("张三").assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Gmail").assertIsDisplayed()
        composeTestRule.onNodeWithText("8条通知").assertIsDisplayed()
        composeTestRule.onNodeWithText("电子邮件").assertIsDisplayed()
        composeTestRule.onNodeWithText("8").assertIsDisplayed()
        composeTestRule.onNodeWithText("工作邮件").assertIsDisplayed()
    }
}