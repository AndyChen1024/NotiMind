package com.andychen.notimind.navigation

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()
    
    private lateinit var navController: TestNavHostController

    @Before
    fun setupNavHost() {
        composeTestRule.setContent {
            val context = LocalContext.current
            navController = remember {
                TestNavHostController(context).apply {
                    navigatorProvider.addNavigator(ComposeNavigator())
                }
            }
            NotiMindNavHost(navController = navController)
        }
    }

    @Test
    fun navHost_startsWithSummaryScreen() {
        assertEquals(Screen.Summary.route, navController.currentBackStackEntry?.destination?.route)
    }

    @Test
    fun navHost_verifyDeepLinkToSettings() {
        // When: Navigating via deep link
        val deepLinkIntent = TestNavigationUtils.createDeepLinkIntent("notimind://settings")
        navController.handleDeepLink(deepLinkIntent)

        // Then: Should navigate to settings screen
        assertEquals(Screen.Settings.route, navController.currentBackStackEntry?.destination?.route)
    }

    @Test
    fun navHost_verifyDeepLinkToNotificationDetail() {
        // When: Navigating via deep link with parameter
        val notificationId = "test_notification_123"
        val deepLinkIntent = TestNavigationUtils.createDeepLinkIntent("notimind://notification/$notificationId")
        navController.handleDeepLink(deepLinkIntent)

        // Then: Should navigate to notification detail screen with correct parameter
        val route = navController.currentBackStackEntry?.destination?.route
        assertEquals(Screen.NotificationDetail.route, route)
        
        val actualNotificationId = navController.currentBackStackEntry?.arguments?.getString("notificationId")
        assertEquals(notificationId, actualNotificationId)
    }

    @Test
    fun navHost_verifyDeepLinkToAppSummary() {
        // When: Navigating via deep link with parameter
        val packageName = "com.example.app"
        val deepLinkIntent = TestNavigationUtils.createDeepLinkIntent("notimind://app_summary/$packageName")
        navController.handleDeepLink(deepLinkIntent)

        // Then: Should navigate to app summary screen with correct parameter
        val route = navController.currentBackStackEntry?.destination?.route
        assertEquals(Screen.AppSummary.route, route)
        
        val actualPackageName = navController.currentBackStackEntry?.arguments?.getString("packageName")
        assertEquals(packageName, actualPackageName)
    }
    
    @Test
    fun navHost_verifyDeepLinkToTimePeriodSummary() {
        // When: Navigating via deep link with parameter
        val period = "daily"
        val deepLinkIntent = TestNavigationUtils.createDeepLinkIntent("notimind://time_summary/$period")
        navController.handleDeepLink(deepLinkIntent)

        // Then: Should navigate to time period summary screen with correct parameter
        val route = navController.currentBackStackEntry?.destination?.route
        assertEquals(Screen.TimePeriodSummary.route, route)
        
        val actualPeriod = navController.currentBackStackEntry?.arguments?.getString("period")
        assertEquals(period, actualPeriod)
    }
    
    @Test
    fun navHost_navigateToNotificationDetail() {
        // When: Navigating to notification detail
        val notificationId = "test_notification_123"
        navController.navigate(Screen.NotificationDetail.createRoute(notificationId))

        // Then: Should navigate to notification detail screen with correct parameter
        val route = navController.currentBackStackEntry?.destination?.route
        assertEquals(Screen.NotificationDetail.route, route)
        
        val actualNotificationId = navController.currentBackStackEntry?.arguments?.getString("notificationId")
        assertEquals(notificationId, actualNotificationId)
    }
    
    @Test
    fun navHost_navigateToAppSummary() {
        // When: Navigating to app summary
        val packageName = "com.example.app"
        navController.navigate(Screen.AppSummary.createRoute(packageName))

        // Then: Should navigate to app summary screen with correct parameter
        val route = navController.currentBackStackEntry?.destination?.route
        assertEquals(Screen.AppSummary.route, route)
        
        val actualPackageName = navController.currentBackStackEntry?.arguments?.getString("packageName")
        assertEquals(packageName, actualPackageName)
    }
    
    @Test
    fun navHost_navigateToTimePeriodSummary() {
        // When: Navigating to time period summary
        val period = "weekly"
        navController.navigate(Screen.TimePeriodSummary.createRoute(period))

        // Then: Should navigate to time period summary screen with correct parameter
        val route = navController.currentBackStackEntry?.destination?.route
        assertEquals(Screen.TimePeriodSummary.route, route)
        
        val actualPeriod = navController.currentBackStackEntry?.arguments?.getString("period")
        assertEquals(period, actualPeriod)
    }
}

@RunWith(AndroidJUnit4::class)
class NavigationActionsTest {

    @get:Rule
    val composeTestRule = createComposeRule()
    
    private lateinit var navController: NavController
    private lateinit var navigationActions: NotiMindNavigationActions

    @Before
    fun setup() {
        navController = mockk(relaxed = true)
        navigationActions = NotiMindNavigationActions(navController)
    }

    @Test
    fun navigationActions_navigateToSummary() {
        // When
        navigationActions.navigateToSummary()
        
        // Then
        verify { navController.navigate(Screen.Summary.route, null) }
    }

    @Test
    fun navigationActions_navigateToNotifications() {
        // When
        navigationActions.navigateToNotifications()
        
        // Then
        verify { navController.navigate(Screen.Notifications.route, null) }
    }

    @Test
    fun navigationActions_navigateToSettings() {
        // When
        navigationActions.navigateToSettings()
        
        // Then
        verify { navController.navigate(Screen.Settings.route, null) }
    }

    @Test
    fun navigationActions_navigateToPermissionRequest() {
        // When
        navigationActions.navigateToPermissionRequest()
        
        // Then
        verify { navController.navigate(Screen.PermissionRequest.route, null) }
    }

    @Test
    fun navigationActions_navigateToNotificationDetail() {
        // When
        val notificationId = "test_notification_123"
        navigationActions.navigateToNotificationDetail(notificationId)
        
        // Then
        verify { navController.navigate(Screen.NotificationDetail.createRoute(notificationId), null) }
    }

    @Test
    fun navigationActions_navigateToAppSummary() {
        // When
        val packageName = "com.example.app"
        navigationActions.navigateToAppSummary(packageName)
        
        // Then
        verify { navController.navigate(Screen.AppSummary.createRoute(packageName), null) }
    }

    @Test
    fun navigationActions_navigateToTimePeriodSummary() {
        // When
        val period = "monthly"
        navigationActions.navigateToTimePeriodSummary(period)
        
        // Then
        verify { navController.navigate(Screen.TimePeriodSummary.createRoute(period), null) }
    }

    @Test
    fun navigationActions_navigateBack() {
        // When
        navigationActions.navigateBack()
        
        // Then
        verify { navController.popBackStack() }
    }

    @Test
    fun navigationActions_navigateBackTo() {
        // When
        navigationActions.navigateBackTo(Screen.Summary.route, true)
        
        // Then
        verify { navController.popBackStack(Screen.Summary.route, true) }
    }
}