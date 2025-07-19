package com.andychen.notimind.feature.notification.service

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.andychen.notimind.core.model.NotificationMapper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotiMindNotificationListenerService : NotificationListenerService() {
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    @Inject
    lateinit var notificationProcessor: NotificationProcessor
    
    companion object {
        private const val TAG = "NotiMindNotificationListenerService"
        
        fun isNotificationServiceEnabled(context: Context): Boolean {
            val packageName = context.packageName
            val flat = android.provider.Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            )
            
            return flat?.contains("$packageName/${NotiMindNotificationListenerService::class.java.name}") == true
        }
        
        fun restartService(context: Context) {
            val componentName = ComponentName(context, NotiMindNotificationListenerService::class.java)
            
            context.packageManager.setComponentEnabledSetting(
                componentName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
            
            context.packageManager.setComponentEnabledSetting(
                componentName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Notification listener service created")
    }
    
    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "Notification listener connected")
        
        serviceScope.launch {
            try {
                val activeNotifications = activeNotifications ?: return@launch
                for (sbn in activeNotifications) {
                    processNotification(sbn, false)
                }
                Log.d(TAG, "Processed ${activeNotifications.size} existing notifications")
            } catch (e: Exception) {
                Log.e(TAG, "Error processing existing notifications", e)
            }
        }
    }
    
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        Log.d(TAG, "Notification posted: ${sbn.packageName}")
        processNotification(sbn, false)
    }
    
    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        Log.d(TAG, "Notification removed: ${sbn.packageName}")
        processNotification(sbn, true)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Notification listener service destroyed")
        
        serviceScope.launch {
            try {
                restartService(applicationContext)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to restart service", e)
            }
        }
    }
    
    private fun processNotification(sbn: StatusBarNotification, isRemoved: Boolean) {
        if (sbn.packageName == packageName) return
        if (sbn.isOngoing && !isRemoved) return
        
        serviceScope.launch {
            try {
                val notification = sbn.notification ?: return@launch
                val extras = notification.extras
                
                val title = extras.getString(Notification.EXTRA_TITLE)
                val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
                val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
                val summaryText = extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT)?.toString()
                
                val packageManager = applicationContext.packageManager
                val appInfo = packageManager.getApplicationInfo(sbn.packageName, 0)
                val appName = packageManager.getApplicationLabel(appInfo).toString()
                
                val content = buildString {
                    text?.let { append(it) }
                    if (!subText.isNullOrBlank()) {
                        if (isNotEmpty()) append(" - ")
                        append(subText)
                    }
                    if (!summaryText.isNullOrBlank() && summaryText != subText) {
                        if (isNotEmpty()) append(" - ")
                        append(summaryText)
                    }
                }
                
                val category = NotificationMapper.determineCategory(sbn.packageName, title, content)
                
                val extraMap = mutableMapOf<String, String>()
                notification.category?.let { extraMap["android_category"] = it }
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    notification.channelId?.let { extraMap["channel_id"] = it }
                }
                
                notification.group?.let { extraMap["group"] = it }
                extraMap["when"] = notification.`when`.toString()
                
                notificationProcessor.processNotification(
                    packageName = sbn.packageName,
                    appName = appName,
                    title = title,
                    content = content.ifEmpty { null },
                    timestamp = sbn.postTime,
                    category = category.name,
                    isRemoved = isRemoved,
                    extras = extraMap
                )
                
            } catch (e: Exception) {
                Log.e(TAG, "Error processing notification", e)
            }
        }
    }
    
    override fun onLowMemory() {
        super.onLowMemory()
        Log.d(TAG, "System is low on memory")
    }
    
    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        Log.d(TAG, "Service rebound")
    }
    
    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "Service unbound")
        return true
    }
}