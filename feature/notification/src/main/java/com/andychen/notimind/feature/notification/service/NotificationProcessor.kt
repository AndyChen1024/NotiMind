package com.andychen.notimind.feature.notification.service

interface NotificationProcessor {
    suspend fun processNotification(
        packageName: String,
        appName: String,
        title: String?,
        content: String?,
        timestamp: Long,
        category: String,
        isRemoved: Boolean,
        extras: Map<String, String>
    )
}