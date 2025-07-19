package com.andychen.notimind.core.model

data class UserPreferences(
    val summaryStyle: SummaryStyle = SummaryStyle.TIME_BASED,
    val isDarkTheme: Boolean = false,
    val dataRetentionPeriod: Int = 30, // days
    val notificationCategoriesToExclude: Set<NotificationCategory> = emptySet()
)