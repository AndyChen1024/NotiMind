package com.andychen.notimind.core.model

data class NotificationEntity(
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val title: String?,
    val content: String?,
    val timestamp: Long,
    val category: String?,
    val isRemoved: Boolean = false,
    val extras: Map<String, String> = emptyMap()
)