package com.andychen.notimind.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.andychen.notimind.feature.permissions.PermissionRequestScreen
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Navigation destinations used in the app
 */
sealed class Screen(val route: String) {
    // Main destinations
    object Summary : Screen("summary")
    object Notifications : Screen("notifications")
    object Settings : Screen("settings")
    object PermissionRequest : Screen("permission_request")
    
    // Detail destinations with arguments
    object NotificationDetail : Screen("notification/{notificationId}") {
        fun createRoute(notificationId: String): String {
            return "notification/$notificationId"
        }
    }
    
    object AppSummary : Screen("app_summary/{packageName}") {
        fun createRoute(packageName: String): String {
            val encodedPackageName = URLEncoder.encode(packageName, StandardCharsets.UTF_8.toString())
            return "app_summary/$encodedPackageName"
        }
    }
    
    object TimePeriodSummary : Screen("time_summary/{period}") {
        fun createRoute(period: String): String {
            return "time_summary/$period"
        }
    }
}

/**
 * Main navigation host for the NotiMind app
 */
@Composable
fun NotiMindNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Summary.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Permission request screen
        composable(Screen.PermissionRequest.route) {
            PermissionRequestScreen(
                onPermissionGranted = {
                    navController.navigate(Screen.Summary.route) {
                        popUpTo(Screen.PermissionRequest.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Main screens
        summaryScreen(navController)
        notificationsScreen(navController)
        settingsScreen(navController)
        
        // Detail screens
        notificationDetailScreen(navController)
        appSummaryScreen(navController)
        timePeriodSummaryScreen(navController)
    }
}

/**
 * Summary screen navigation
 */
private fun NavGraphBuilder.summaryScreen(navController: NavHostController) {
    composable(
        route = Screen.Summary.route,
        deepLinks = listOf(
            navDeepLink { 
                uriPattern = "notimind://summary" 
            }
        )
    ) {
        com.andychen.notimind.feature.summary.ui.SummaryScreen(
            onAppClick = { packageName ->
                navController.navigate(Screen.AppSummary.createRoute(packageName))
            },
            onTimeClick = { period ->
                navController.navigate(Screen.TimePeriodSummary.createRoute(period))
            }
        )
    }
}

/**
 * Notifications screen navigation
 */
private fun NavGraphBuilder.notificationsScreen(navController: NavHostController) {
    composable(
        route = Screen.Notifications.route,
        deepLinks = listOf(
            navDeepLink { 
                uriPattern = "notimind://notifications" 
            }
        )
    ) {
        // Placeholder for NotificationListScreen
        // Temporarily removed due to build issues
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            androidx.compose.material3.Text("通知功能开发中...")
        }
    }
}

/**
 * Settings screen navigation
 */
private fun NavGraphBuilder.settingsScreen(navController: NavHostController) {
    composable(
        route = Screen.Settings.route,
        deepLinks = listOf(
            navDeepLink { 
                uriPattern = "notimind://settings" 
            }
        )
    ) {
        com.andychen.notimind.feature.settings.ui.SettingsScreen()
    }
}

/**
 * Notification detail screen navigation
 */
private fun NavGraphBuilder.notificationDetailScreen(navController: NavHostController) {
    composable(
        route = Screen.NotificationDetail.route,
        arguments = listOf(
            navArgument("notificationId") {
                type = NavType.StringType
                nullable = false
            }
        ),
        deepLinks = listOf(
            navDeepLink { 
                uriPattern = "notimind://notification/{notificationId}" 
            }
        )
    ) { backStackEntry ->
        val notificationId = backStackEntry.arguments?.getString("notificationId") ?: ""
        // Placeholder for NotificationDetailScreen
        // Temporarily removed due to build issues
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            androidx.compose.material3.Text("通知详情功能开发中...")
        }
    }
}

/**
 * App summary screen navigation
 */
private fun NavGraphBuilder.appSummaryScreen(navController: NavHostController) {
    composable(
        route = Screen.AppSummary.route,
        arguments = listOf(
            navArgument("packageName") {
                type = NavType.StringType
                nullable = false
            }
        ),
        deepLinks = listOf(
            navDeepLink { 
                uriPattern = "notimind://app_summary/{packageName}" 
            }
        )
    ) { backStackEntry ->
        val packageName = backStackEntry.arguments?.getString("packageName") ?: ""
        com.andychen.notimind.feature.summary.ui.AppDetailScreen(
            packageName = packageName,
            onBackClick = { navController.navigateUp() }
        )
    }
}

/**
 * Time period summary screen navigation
 */
private fun NavGraphBuilder.timePeriodSummaryScreen(navController: NavHostController) {
    composable(
        route = Screen.TimePeriodSummary.route,
        arguments = listOf(
            navArgument("period") {
                type = NavType.StringType
                nullable = false
            }
        ),
        deepLinks = listOf(
            navDeepLink { 
                uriPattern = "notimind://time_summary/{period}" 
            }
        )
    ) { backStackEntry ->
        val period = backStackEntry.arguments?.getString("period") ?: ""
        // 将period字符串转换为TimePeriod枚举
        val timePeriod = try {
            com.andychen.notimind.core.model.TimePeriod.valueOf(period)
        } catch (e: IllegalArgumentException) {
            com.andychen.notimind.core.model.TimePeriod.ALL_DAY
        }
        
        // 这里我们可以创建一个专门的时间段详情屏幕
        // 目前我们可以简单地返回到摘要屏幕
        navController.navigateUp()
    }
}