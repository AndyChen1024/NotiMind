package com.andychen.notimind.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptions

/**
 * 导航操作类，提供应用内导航的便捷方法
 */
class NotiMindNavigationActions(private val navController: NavController) {

    /**
     * 导航到摘要屏幕
     */
    fun navigateToSummary(navOptions: NavOptions? = null) {
        navController.navigate(Screen.Summary.route, navOptions)
    }

    /**
     * 导航到通知列表屏幕
     */
    fun navigateToNotifications(navOptions: NavOptions? = null) {
        navController.navigate(Screen.Notifications.route, navOptions)
    }

    /**
     * 导航到设置屏幕
     */
    fun navigateToSettings(navOptions: NavOptions? = null) {
        navController.navigate(Screen.Settings.route, navOptions)
    }

    /**
     * 导航到权限请求屏幕
     */
    fun navigateToPermissionRequest(navOptions: NavOptions? = null) {
        navController.navigate(Screen.PermissionRequest.route, navOptions)
    }

    /**
     * 导航到通知详情屏幕
     */
    fun navigateToNotificationDetail(notificationId: String, navOptions: NavOptions? = null) {
        navController.navigate(Screen.NotificationDetail.createRoute(notificationId), navOptions)
    }

    /**
     * 导航到应用摘要屏幕
     */
    fun navigateToAppSummary(packageName: String, navOptions: NavOptions? = null) {
        navController.navigate(Screen.AppSummary.createRoute(packageName), navOptions)
    }

    /**
     * 导航到时间段摘要屏幕
     */
    fun navigateToTimePeriodSummary(period: String, navOptions: NavOptions? = null) {
        navController.navigate(Screen.TimePeriodSummary.createRoute(period), navOptions)
    }

    /**
     * 返回上一个屏幕
     */
    fun navigateBack() {
        navController.popBackStack()
    }

    /**
     * 返回到指定目的地
     */
    fun navigateBackTo(route: String, inclusive: Boolean = false) {
        navController.popBackStack(route, inclusive)
    }
}