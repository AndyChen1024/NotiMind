package com.andychen.notimind.feature.permissions

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

interface NotificationPermissionChecker {
    fun hasNotificationPermission(): Boolean
    fun requestNotificationPermission(activity: Activity)
}

@Singleton
class NotificationPermissionCheckerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : NotificationPermissionChecker {

    override fun hasNotificationPermission(): Boolean {
        val notificationManager = NotificationManagerCompat.from(context)
        
        // Check if notification listener service is enabled
        val enabledListeners = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )
        
        val myNotificationListenerComponent = ComponentName(
            context.packageName,
            "com.andychen.notimind.feature.notification.service.NotiMindNotificationListenerService"
        )
        
        val hasListenerPermission = enabledListeners?.contains(myNotificationListenerComponent.flattenToString()) ?: false
        
        // For Android 13+, also check notification permission
        val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationManager.areNotificationsEnabled()
        } else {
            true
        }
        
        return hasListenerPermission && hasNotificationPermission
    }

    override fun requestNotificationPermission(activity: Activity) {
        // Open notification listener settings
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        activity.startActivity(intent)
    }
}