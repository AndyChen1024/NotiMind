package com.andychen.notimind.core.data.repository

import com.andychen.notimind.core.model.NotificationCategory
import com.andychen.notimind.core.model.SummaryStyle
import com.andychen.notimind.core.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    /**
     * 获取用户偏好设置的 Flow
     */
    val userPreferencesFlow: Flow<UserPreferences>
    
    /**
     * 更新摘要样式
     */
    suspend fun updateSummaryStyle(style: SummaryStyle)
    
    /**
     * 切换深色主题
     */
    suspend fun toggleDarkTheme(enabled: Boolean)
    
    /**
     * 设置数据保留期限（天数）
     */
    suspend fun setDataRetentionPeriod(days: Int)
    
    /**
     * 更新要排除的通知类别
     */
    suspend fun updateExcludedCategories(categories: Set<NotificationCategory>)
    
    /**
     * 添加要排除的通知类别
     */
    suspend fun addExcludedCategory(category: NotificationCategory)
    
    /**
     * 移除要排除的通知类别
     */
    suspend fun removeExcludedCategory(category: NotificationCategory)
    
    /**
     * 重置所有偏好设置为默认值
     */
    suspend fun resetToDefaults()
}