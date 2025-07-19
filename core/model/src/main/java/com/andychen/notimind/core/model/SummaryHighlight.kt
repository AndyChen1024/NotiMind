package com.andychen.notimind.core.model

data class SummaryHighlight(
    val title: String,
    val content: String,
    val category: NotificationCategory,
    val importance: HighlightImportance
)