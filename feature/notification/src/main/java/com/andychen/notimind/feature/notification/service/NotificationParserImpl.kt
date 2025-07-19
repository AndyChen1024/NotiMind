package com.andychen.notimind.feature.notification.service

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.andychen.notimind.core.database.dao.NotificationDao
import com.andychen.notimind.core.database.entity.NotificationEntity
import com.andychen.notimind.core.model.NotificationCategory
import com.andychen.notimind.core.model.NotificationMapper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationParserImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationDao: NotificationDao
) : NotificationProcessor {

    private val TAG = "NotificationParser"
    private val appNameCache = mutableMapOf<String, String>()

    override suspend fun processNotification(
        packageName: String,
        appName: String,
        title: String?,
        content: String?,
        timestamp: Long,
        category: String,
        isRemoved: Boolean,
        extras: Map<String, String>
    ) {
        withContext(Dispatchers.IO) {
            try {
                // Cache the app name for future use
                if (!appNameCache.containsKey(packageName)) {
                    appNameCache[packageName] = appName
                }

                // Create notification entity
                val notificationEntity = NotificationEntity(
                    packageName = packageName,
                    appName = appName,
                    title = sanitizeText(title),
                    content = sanitizeText(content),
                    timestamp = timestamp,
                    category = category,
                    isRemoved = isRemoved
                )

                // Convert extras to pairs for database storage
                val extraPairs = extras.map { it.key to it.value }

                // Store in database
                notificationDao.insertNotificationWithExtras(notificationEntity, extraPairs)
                
                Log.d(TAG, "Processed notification from $appName: $title")
            } catch (e: Exception) {
                Log.e(TAG, "Error processing notification", e)
            }
        }
    }

    /**
     * Resolves an app name from a package name, using cache when available
     */
    fun resolveAppName(packageName: String): String {
        // Return from cache if available
        appNameCache[packageName]?.let { return it }

        return try {
            val packageManager = context.packageManager
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            val appName = packageManager.getApplicationLabel(appInfo).toString()
            
            // Cache for future use
            appNameCache[packageName] = appName
            
            appName
        } catch (e: PackageManager.NameNotFoundException) {
            // If package not found, use package name as fallback
            Log.w(TAG, "Could not resolve app name for package: $packageName")
            packageName
        }
    }

    /**
     * Sanitizes text content to prevent issues with database storage
     */
    private fun sanitizeText(text: String?): String? {
        if (text == null) return null
        
        // Limit text length to prevent database issues
        val maxLength = 1000
        return if (text.length > maxLength) {
            text.substring(0, maxLength) + "..."
        } else {
            text
        }
    }

    /**
     * Determines the appropriate notification category based on content and package
     */
    fun determineNotificationCategory(
        packageName: String,
        title: String?,
        content: String?
    ): NotificationCategory {
        return NotificationMapper.determineCategory(packageName, title, content)
    }
}