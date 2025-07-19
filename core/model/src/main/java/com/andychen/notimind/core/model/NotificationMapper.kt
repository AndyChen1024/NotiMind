package com.andychen.notimind.core.model

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

object NotificationMapper {
    fun determineTimePeriod(timestamp: Long): TimePeriod {
        val dateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(timestamp),
            ZoneId.systemDefault()
        )
        
        return when (dateTime.hour) {
            in 5..11 -> TimePeriod.MORNING
            in 12..16 -> TimePeriod.AFTERNOON
            in 17..21 -> TimePeriod.EVENING
            else -> TimePeriod.NIGHT
        }
    }
    
    fun determineCategory(packageName: String, title: String?, content: String?): NotificationCategory {
        // Simple rule-based categorization as fallback for AI categorization
        return when {
            packageName.contains("message") || packageName.contains("sms") || 
            packageName.contains("chat") || packageName.contains("talk") -> {
                if (title?.contains("group", ignoreCase = true) == true || 
                    content?.contains("group", ignoreCase = true) == true) {
                    NotificationCategory.GROUP_MESSAGE
                } else {
                    NotificationCategory.PERSONAL_MESSAGE
                }
            }
            packageName.contains("mail") || packageName.contains("gmail") || 
            packageName.contains("outlook") || packageName.contains("yahoo") -> {
                NotificationCategory.EMAIL
            }
            packageName.contains("facebook") || packageName.contains("instagram") || 
            packageName.contains("twitter") || packageName.contains("tiktok") || 
            packageName.contains("linkedin") || packageName.contains("weibo") || 
            packageName.contains("wechat") -> {
                NotificationCategory.SOCIAL_MEDIA
            }
            packageName.contains("news") || packageName.contains("nytimes") || 
            packageName.contains("bbc") || packageName.contains("cnn") -> {
                NotificationCategory.NEWS
            }
            title?.contains("off", ignoreCase = true) == true || 
            title?.contains("sale", ignoreCase = true) == true || 
            title?.contains("discount", ignoreCase = true) == true || 
            content?.contains("off", ignoreCase = true) == true || 
            content?.contains("sale", ignoreCase = true) == true || 
            content?.contains("discount", ignoreCase = true) == true -> {
                NotificationCategory.PROMOTION
            }
            packageName.contains("android") || packageName.contains("google") || 
            packageName.contains("system") || packageName.contains("settings") -> {
                NotificationCategory.SYSTEM
            }
            title?.contains("alert", ignoreCase = true) == true || 
            title?.contains("warning", ignoreCase = true) == true || 
            title?.contains("urgent", ignoreCase = true) == true || 
            content?.contains("alert", ignoreCase = true) == true || 
            content?.contains("warning", ignoreCase = true) == true || 
            content?.contains("urgent", ignoreCase = true) == true -> {
                NotificationCategory.ALERT
            }
            else -> NotificationCategory.OTHER
        }
    }
    
    fun determineHighlightImportance(
        category: NotificationCategory, 
        title: String?, 
        content: String?
    ): HighlightImportance {
        // Simple rule-based importance determination
        return when {
            category == NotificationCategory.ALERT -> HighlightImportance.CRITICAL
            category == NotificationCategory.PERSONAL_MESSAGE || 
            category == NotificationCategory.EMAIL -> HighlightImportance.HIGH
            category == NotificationCategory.GROUP_MESSAGE || 
            category == NotificationCategory.SOCIAL_MEDIA -> HighlightImportance.MEDIUM
            else -> HighlightImportance.LOW
        }
    }
    
    fun getDateFromTimestamp(timestamp: Long): LocalDate {
        return Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }
}