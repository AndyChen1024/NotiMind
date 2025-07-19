package com.andychen.notimind.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.andychen.notimind.core.model.NotificationCategory
import com.andychen.notimind.core.model.PreferencesSerializer
import com.andychen.notimind.core.model.SummaryStyle
import com.andychen.notimind.core.model.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UserPreferencesRepository {

    private object PreferencesKeys {
        val SUMMARY_STYLE = stringPreferencesKey("summary_style")
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val DATA_RETENTION_PERIOD = intPreferencesKey("data_retention_period")
        val EXCLUDED_CATEGORIES = stringPreferencesKey("excluded_categories")
    }

    override val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val summaryStyle = preferences[PreferencesKeys.SUMMARY_STYLE]?.let {
                try {
                    SummaryStyle.valueOf(it)
                } catch (e: IllegalArgumentException) {
                    SummaryStyle.TIME_BASED
                }
            } ?: SummaryStyle.TIME_BASED
            
            val isDarkTheme = preferences[PreferencesKeys.IS_DARK_THEME] ?: false
            val dataRetentionPeriod = preferences[PreferencesKeys.DATA_RETENTION_PERIOD] ?: 30
            
            val excludedCategories = preferences[PreferencesKeys.EXCLUDED_CATEGORIES]
                ?.split(",")
                ?.filter { it.isNotEmpty() }
                ?.mapNotNull {
                    try {
                        NotificationCategory.valueOf(it)
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                }?.toSet() ?: emptySet()
            
            UserPreferences(
                summaryStyle = summaryStyle,
                isDarkTheme = isDarkTheme,
                dataRetentionPeriod = dataRetentionPeriod,
                notificationCategoriesToExclude = excludedCategories
            )
        }

    override suspend fun updateSummaryStyle(style: SummaryStyle) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SUMMARY_STYLE] = style.name
        }
    }

    override suspend fun toggleDarkTheme(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_DARK_THEME] = enabled
        }
    }

    override suspend fun setDataRetentionPeriod(days: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DATA_RETENTION_PERIOD] = days
        }
    }

    override suspend fun updateExcludedCategories(categories: Set<NotificationCategory>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.EXCLUDED_CATEGORIES] = categories
                .joinToString(",") { it.name }
        }
    }

    override suspend fun addExcludedCategory(category: NotificationCategory) {
        context.dataStore.edit { preferences ->
            val currentCategories = preferences[PreferencesKeys.EXCLUDED_CATEGORIES]
                ?.split(",")
                ?.filter { it.isNotEmpty() }
                ?.toMutableSet() ?: mutableSetOf()
            
            currentCategories.add(category.name)
            
            preferences[PreferencesKeys.EXCLUDED_CATEGORIES] = currentCategories.joinToString(",")
        }
    }

    override suspend fun removeExcludedCategory(category: NotificationCategory) {
        context.dataStore.edit { preferences ->
            val currentCategories = preferences[PreferencesKeys.EXCLUDED_CATEGORIES]
                ?.split(",")
                ?.filter { it.isNotEmpty() }
                ?.toMutableSet() ?: mutableSetOf()
            
            currentCategories.remove(category.name)
            
            preferences[PreferencesKeys.EXCLUDED_CATEGORIES] = currentCategories.joinToString(",")
        }
    }

    override suspend fun resetToDefaults() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SUMMARY_STYLE] = SummaryStyle.TIME_BASED.name
            preferences[PreferencesKeys.IS_DARK_THEME] = false
            preferences[PreferencesKeys.DATA_RETENTION_PERIOD] = 30
            preferences[PreferencesKeys.EXCLUDED_CATEGORIES] = ""
        }
    }
}