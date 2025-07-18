package com.andychen.notimind.ui.common.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ErrorStateTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun errorState_displaysMessage() {
        // Given
        val errorMessage = "Test error message"
        var retryClicked = false
        
        // When
        composeTestRule.setContent {
            ErrorState(
                message = errorMessage,
                onRetry = { retryClicked = true }
            )
        }

        // Then
        composeTestRule.onNodeWithTag("error_state_container").assertIsDisplayed()
        composeTestRule.onNodeWithTag("error_icon").assertIsDisplayed()
        composeTestRule.onNodeWithTag("error_message").assertIsDisplayed()
        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
        composeTestRule.onNodeWithTag("retry_button").assertIsDisplayed()
    }

    @Test
    fun errorState_clickRetry_callsCallback() {
        // Given
        var retryClicked = false
        
        // When
        composeTestRule.setContent {
            ErrorState(
                message = "Error message",
                onRetry = { retryClicked = true }
            )
        }
        
        // Then
        composeTestRule.onNodeWithTag("retry_button").performClick()
        assert(retryClicked) { "Retry button click callback was not called" }
    }
}