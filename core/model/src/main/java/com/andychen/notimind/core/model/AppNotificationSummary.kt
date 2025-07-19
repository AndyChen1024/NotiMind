package com.andychen.notimind.core.model

import android.graphics.drawable.Drawable

data class AppNotificationSummary(
    val packageName: String,
    val appName: String,
    val appIcon: Drawable?,
    val notificationCount: Int,
    val categories: Map<NotificationCategory, Int>,
    val highlights: List<SummaryHighlight>
)