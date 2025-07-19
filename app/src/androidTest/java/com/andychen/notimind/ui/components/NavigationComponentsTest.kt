package com.andychen.notimind.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.andychen.notimind.navigation.Screen
import com.andychen.notimind.ui.theme.NotiMindTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationComponentsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun bottomNavigation_displaysAllItems() {
        // Given
        composeTestRule.setContent {
            NotiMindTheme {
                NotiMindBottomNavigation(
                    currentRoute = Screen.Summary.route,
                    onNavigateToRoute = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("摘要").assertIsDisplayed()
        composeTestRule.onNodeWithText("通知").assertIsDisplayed()
        composeTestRule.onNodeWithText("设置").assertIsDisplayed()
    }

    @Test
    fun bottomNavigation_clickingItemCallsCallback() {
        // Given
        var navigatedRoute = ""
        
        composeTestRule.setContent {
            NotiMindTheme {
                NotiMindBottomNavigation(
                    currentRoute = Screen.Summary.route,
                    onNavigateToRoute = { route ->
                        navigatedRoute = route
                    }
                )
            }
        }

        // When
        composeTestRule.onNodeWithText("通知").performClick()

        // Then
        assert(navigatedRoute == Screen.Notifications.route)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun topAppBar_displaysTitle() {
        // Given
        val title = "测试标题"
        
        composeTestRule.setContent {
            NotiMindTheme {
                NotiMindTopAppBar(
                    title = title,
                    scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun topAppBar_backButtonShownWhenEnabled() {
        // Given
        var backPressed = false
        
        composeTestRule.setContent {
            NotiMindTheme {
                NotiMindTopAppBar(
                    title = "测试标题",
                    canNavigateBack = true,
                    navigateUp = { backPressed = true },
                    scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
                )
            }
        }

        // When
        composeTestRule.onNodeWithContentDescription("返回").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("返回").performClick()

        // Then
        assert(backPressed)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun topAppBar_backButtonHiddenWhenDisabled() {
        // Given
        composeTestRule.setContent {
            NotiMindTheme {
                NotiMindTopAppBar(
                    title = "测试标题",
                    canNavigateBack = false,
                    scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
                )
            }
        }

        // Then
        composeTestRule.onNodeWithContentDescription("返回").assertDoesNotExist()
    }
}