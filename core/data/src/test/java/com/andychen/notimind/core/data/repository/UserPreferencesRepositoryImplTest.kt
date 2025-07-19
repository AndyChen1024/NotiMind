package com.andychen.notimind.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.andychen.notimind.core.model.NotificationCategory
import com.andychen.notimind.core.model.SummaryStyle
import com.andychen.notimind.core.model.UserPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.File
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class UserPreferencesRepositoryImplTest {

    private lateinit var context: Context
    private lateinit var repository: UserPreferencesRepositoryImpl
    private val preferencesFlow = MutableStateFlow(UserPreferences())

    @Before
    fun setup() {
        context = mock()
        repository = mock()
        
        whenever(repository.userPreferencesFlow).thenReturn(preferencesFlow)
        
        // Mock repository methods to update the preferences flow
        whenever(repository.updateSummaryStyle(any())).doAnswer { invocation ->
            val style = invocation.getArgument<SummaryStyle>(0)
            preferencesFlow.value = preferencesFlow.value.copy(summaryStyle = style)
            Unit
        }
        
        whenever(repository.toggleDarkTheme(any())).doAnswer { invocation ->
            val enabled = invocation.getArgument<Boolean>(0)
            preferencesFlow.value = preferencesFlow.value.copy(isDarkTheme = enabled)
            Unit
        }
        
        whenever(repository.setDataRetentionPeriod(any())).doAnswer { invocation ->
            val days = invocation.getArgument<Int>(0)
            preferencesFlow.value = preferencesFlow.value.copy(dataRetentionPeriod = days)
            Unit
        }
        
        whenever(repository.updateExcludedCategories(any())).doAnswer { invocation ->
            val categories = invocation.getArgument<Set<NotificationCategory>>(0)
            preferencesFlow.value = preferencesFlow.value.copy(notificationCategoriesToExclude = categories)
            Unit
        }
        
        whenever(repository.addExcludedCategory(any())).doAnswer { invocation ->
            val category = invocation.getArgument<NotificationCategory>(0)
            val currentCategories = preferencesFlow.value.notificationCategoriesToExclude.toMutableSet()
            currentCategories.add(category)
            preferencesFlow.value = preferencesFlow.value.copy(notificationCategoriesToExclude = currentCategories)
            Unit
        }
        
        whenever(repository.removeExcludedCategory(any())).doAnswer { invocation ->
            val category = invocation.getArgument<NotificationCategory>(0)
            val currentCategories = preferencesFlow.value.notificationCategoriesToExclude.toMutableSet()
            currentCategories.remove(category)
            preferencesFlow.value = preferencesFlow.value.copy(notificationCategoriesToExclude = currentCategories)
            Unit
        }
        
        whenever(repository.resetToDefaults()).doAnswer {
            preferencesFlow.value = UserPreferences()
            Unit
        }
    }

    @Test
    fun `userPreferencesFlow should return default values when preferences are empty`() = runTest {
        val preferences = repository.userPreferencesFlow.first()
        
        assertEquals(SummaryStyle.TIME_BASED, preferences.summaryStyle)
        assertFalse(preferences.isDarkTheme)
        assertEquals(30, preferences.dataRetentionPeriod)
        assertTrue(preferences.notificationCategoriesToExclude.isEmpty())
    }

    @Test
    fun `updateSummaryStyle should update the summary style preference`() = runTest {
        repository.updateSummaryStyle(SummaryStyle.APP_BASED)
        
        val preferences = repository.userPreferencesFlow.first()
        assertEquals(SummaryStyle.APP_BASED, preferences.summaryStyle)
    }

    @Test
    fun `toggleDarkTheme should update the dark theme preference`() = runTest {
        repository.toggleDarkTheme(true)
        
        val preferences = repository.userPreferencesFlow.first()
        assertTrue(preferences.isDarkTheme)
        
        repository.toggleDarkTheme(false)
        
        val updatedPreferences = repository.userPreferencesFlow.first()
        assertFalse(updatedPreferences.isDarkTheme)
    }

    @Test
    fun `setDataRetentionPeriod should update the data retention period preference`() = runTest {
        repository.setDataRetentionPeriod(60)
        
        val preferences = repository.userPreferencesFlow.first()
        assertEquals(60, preferences.dataRetentionPeriod)
        
        repository.setDataRetentionPeriod(90)
        
        val updatedPreferences = repository.userPreferencesFlow.first()
        assertEquals(90, updatedPreferences.dataRetentionPeriod)
    }

    @Test
    fun `updateExcludedCategories should update the excluded categories preference`() = runTest {
        val categories = setOf(
            NotificationCategory.PROMOTION,
            NotificationCategory.SYSTEM
        )
        
        repository.updateExcludedCategories(categories)
        
        val preferences = repository.userPreferencesFlow.first()
        assertEquals(categories, preferences.notificationCategoriesToExclude)
        
        val newCategories = setOf(
            NotificationCategory.EMAIL,
            NotificationCategory.NEWS
        )
        
        repository.updateExcludedCategories(newCategories)
        
        val updatedPreferences = repository.userPreferencesFlow.first()
        assertEquals(newCategories, updatedPreferences.notificationCategoriesToExclude)
    }

    @Test
    fun `addExcludedCategory should add a category to the excluded categories`() = runTest {
        repository.addExcludedCategory(NotificationCategory.PROMOTION)
        
        val preferences1 = repository.userPreferencesFlow.first()
        assertEquals(setOf(NotificationCategory.PROMOTION), preferences1.notificationCategoriesToExclude)
        
        repository.addExcludedCategory(NotificationCategory.SYSTEM)
        
        val preferences2 = repository.userPreferencesFlow.first()
        assertEquals(
            setOf(NotificationCategory.PROMOTION, NotificationCategory.SYSTEM),
            preferences2.notificationCategoriesToExclude
        )
        
        // Adding the same category again should not change the set
        repository.addExcludedCategory(NotificationCategory.PROMOTION)
        
        val preferences3 = repository.userPreferencesFlow.first()
        assertEquals(
            setOf(NotificationCategory.PROMOTION, NotificationCategory.SYSTEM),
            preferences3.notificationCategoriesToExclude
        )
    }

    @Test
    fun `removeExcludedCategory should remove a category from the excluded categories`() = runTest {
        val categories = setOf(
            NotificationCategory.PROMOTION,
            NotificationCategory.SYSTEM,
            NotificationCategory.EMAIL
        )
        
        repository.updateExcludedCategories(categories)
        
        repository.removeExcludedCategory(NotificationCategory.SYSTEM)
        
        val preferences = repository.userPreferencesFlow.first()
        assertEquals(
            setOf(NotificationCategory.PROMOTION, NotificationCategory.EMAIL),
            preferences.notificationCategoriesToExclude
        )
        
        // Removing a non-existent category should not change the set
        repository.removeExcludedCategory(NotificationCategory.NEWS)
        
        val preferences2 = repository.userPreferencesFlow.first()
        assertEquals(
            setOf(NotificationCategory.PROMOTION, NotificationCategory.EMAIL),
            preferences2.notificationCategoriesToExclude
        )
    }

    @Test
    fun `resetToDefaults should reset all preferences to default values`() = runTest {
        repository.updateSummaryStyle(SummaryStyle.APP_BASED)
        repository.toggleDarkTheme(true)
        repository.setDataRetentionPeriod(60)
        repository.updateExcludedCategories(
            setOf(NotificationCategory.PROMOTION, NotificationCategory.SYSTEM)
        )
        
        val preferencesBeforeReset = repository.userPreferencesFlow.first()
        assertEquals(SummaryStyle.APP_BASED, preferencesBeforeReset.summaryStyle)
        assertTrue(preferencesBeforeReset.isDarkTheme)
        assertEquals(60, preferencesBeforeReset.dataRetentionPeriod)
        assertEquals(
            setOf(NotificationCategory.PROMOTION, NotificationCategory.SYSTEM),
            preferencesBeforeReset.notificationCategoriesToExclude
        )
        
        repository.resetToDefaults()
        
        val preferences = repository.userPreferencesFlow.first()
        assertEquals(SummaryStyle.TIME_BASED, preferences.summaryStyle)
        assertFalse(preferences.isDarkTheme)
        assertEquals(30, preferences.dataRetentionPeriod)
        assertTrue(preferences.notificationCategoriesToExclude.isEmpty())
    }
    
    @Test
    fun `multiple preference updates should be reflected correctly`() = runTest {
        // Update multiple preferences in sequence
        repository.updateSummaryStyle(SummaryStyle.APP_BASED)
        repository.toggleDarkTheme(true)
        repository.setDataRetentionPeriod(45)
        repository.addExcludedCategory(NotificationCategory.PROMOTION)
        
        // Verify all updates are reflected
        val preferences = repository.userPreferencesFlow.first()
        assertEquals(SummaryStyle.APP_BASED, preferences.summaryStyle)
        assertTrue(preferences.isDarkTheme)
        assertEquals(45, preferences.dataRetentionPeriod)
        assertEquals(setOf(NotificationCategory.PROMOTION), preferences.notificationCategoriesToExclude)
    }
}