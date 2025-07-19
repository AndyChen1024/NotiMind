package com.andychen.notimind.core.model

/**
 * Utility class for serializing and deserializing UserPreferences
 * using simple string-based serialization.
 */
object PreferencesSerializer {
    
    fun serializePreferences(preferences: UserPreferences): String {
        return buildString {
            append("summaryStyle=${preferences.summaryStyle.name};")
            append("isDarkTheme=${preferences.isDarkTheme};")
            append("dataRetentionPeriod=${preferences.dataRetentionPeriod};")
            append("excludedCategories=${preferences.notificationCategoriesToExclude.joinToString(",") { it.name }}")
        }
    }
    
    fun deserializePreferences(serialized: String): UserPreferences {
        val pairs = serialized.split(";")
            .mapNotNull { pair ->
                val parts = pair.split("=", limit = 2)
                if (parts.size == 2) parts[0] to parts[1] else null
            }
            .toMap()
        
        val summaryStyle = try {
            SummaryStyle.valueOf(pairs["summaryStyle"] ?: SummaryStyle.TIME_BASED.name)
        } catch (e: Exception) {
            SummaryStyle.TIME_BASED
        }
        
        val isDarkTheme = pairs["isDarkTheme"]?.toBoolean() ?: false
        val dataRetentionPeriod = pairs["dataRetentionPeriod"]?.toIntOrNull() ?: 30
        
        val excludedCategories = pairs["excludedCategories"]
            ?.split(",")
            ?.filter { it.isNotEmpty() }
            ?.mapNotNull { categoryName ->
                try {
                    NotificationCategory.valueOf(categoryName)
                } catch (e: Exception) {
                    null
                }
            }?.toSet() ?: emptySet()
        
        return UserPreferences(
            summaryStyle = summaryStyle,
            isDarkTheme = isDarkTheme,
            dataRetentionPeriod = dataRetentionPeriod,
            notificationCategoriesToExclude = excludedCategories
        )
    }
}