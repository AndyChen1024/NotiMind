package com.andychen.notimind.core.model

import java.time.LocalDate

data class NotificationSummary(
    val id: String,
    val period: TimePeriod,
    val date: LocalDate,
    val categories: Map<NotificationCategory, Int>,
    val highlights: List<SummaryHighlight>,
    val totalCount: Int
)