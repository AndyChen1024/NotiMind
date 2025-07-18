package com.andychen.notimind.ui.common.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoadingStateTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loadingState_isDisplayed() {
        // When
        composeTestRule.setContent {
            LoadingState()
        }

        // Then
        composeTestRule.onNodeWithTag("loading_indicator").assertIsDisplayed()
    }

    @Test
    fun loadingState_withCustomModifier_isDisplayed() {
        // When
        composeTestRule.setContent {
            LoadingState(isFullScreen = false)
        }

        // Then
        composeTestRule.onNodeWithTag("loading_indicator").assertIsDisplayed()
    }
}