package com.andychen.notimind.feature.permissions

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PermissionRequestScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun permissionRequestScreen_displaysCorrectContent() {
        // Given
        val viewModel = mockk<PermissionViewModel>(relaxed = true)
        every { viewModel.permissionState } returns PermissionState(hasNotificationPermission = false)
        
        // When
        composeTestRule.setContent {
            PermissionRequestScreen(
                viewModel = viewModel,
                onPermissionGranted = {}
            )
        }
        
        // Then
        composeTestRule.onNodeWithText("通知权限").assertIsDisplayed()
        composeTestRule.onNodeWithText("NotiMind 需要访问您的通知以提供摘要和分析功能。").assertIsDisplayed()
        composeTestRule.onNodeWithText("为什么需要通知权限？").assertIsDisplayed()
        composeTestRule.onNodeWithText("授予通知权限").assertIsDisplayed()
    }

    @Test
    fun permissionRequestScreen_showsGrantedStateWhenPermissionGranted() {
        // Given
        val viewModel = mockk<PermissionViewModel>(relaxed = true)
        every { viewModel.permissionState } returns PermissionState(hasNotificationPermission = true)
        
        // When
        composeTestRule.setContent {
            PermissionRequestScreen(
                viewModel = viewModel,
                onPermissionGranted = {}
            )
        }
        
        // Then
        composeTestRule.onNodeWithText("权限已授予！").assertIsDisplayed()
        composeTestRule.onNodeWithText("继续").assertIsDisplayed()
    }

    @Test
    fun permissionRequestScreen_callsOnPermissionGrantedWhenContinueClicked() {
        // Given
        val viewModel = mockk<PermissionViewModel>(relaxed = true)
        every { viewModel.permissionState } returns PermissionState(hasNotificationPermission = true)
        val onPermissionGranted = mockk<() -> Unit>(relaxed = true)
        
        // When
        composeTestRule.setContent {
            PermissionRequestScreen(
                viewModel = viewModel,
                onPermissionGranted = onPermissionGranted
            )
        }
        
        // Then
        composeTestRule.onNodeWithText("继续").performClick()
        verify { onPermissionGranted() }
    }

    @Test
    fun permissionRequestScreen_showsSettingsButtonAfterDenial() {
        // Given
        val viewModel = mockk<PermissionViewModel>(relaxed = true)
        every { viewModel.permissionState } returns PermissionState(
            hasNotificationPermission = false,
            permissionDeniedCount = 1
        )
        
        // When
        composeTestRule.setContent {
            PermissionRequestScreen(
                viewModel = viewModel,
                onPermissionGranted = {}
            )
        }
        
        // Then
        composeTestRule.onNodeWithText("在系统设置中开启").assertIsDisplayed()
    }
}