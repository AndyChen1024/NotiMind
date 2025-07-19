package com.andychen.notimind.feature.summary

import app.cash.turbine.test
import com.andychen.notimind.core.data.repository.SummaryRepository
import com.andychen.notimind.core.data.repository.UserPreferencesRepository
import com.andychen.notimind.core.model.AppNotificationSummary
import com.andychen.notimind.core.model.NotificationCategory
import com.andychen.notimind.core.model.NotificationSummary
import com.andychen.notimind.core.model.SummaryHighlight
import com.andychen.notimind.core.model.SummaryStyle
import com.andychen.notimind.core.model.TimePeriod
import com.andychen.notimind.core.model.TimeRange
import com.andychen.notimind.core.model.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@ExperimentalCoroutinesApi
class SummaryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var summaryRepository: FakeSummaryRepository
    private lateinit var userPreferencesRepository: FakeUserPreferencesRepository
    private lateinit var viewModel: SummaryViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        summaryRepository = FakeSummaryRepository()
        userPreferencesRepository = FakeUserPreferencesRepository()
        viewModel = SummaryViewModel(summaryRepository, userPreferencesRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has correct values`() = runTest {
        val today = LocalDate.now()
        viewModel.summaryState.test {
            val initialState = awaitItem()
            assertEquals(SummaryStyle.TIME_BASED, initialState.summaryStyle)
            assertEquals(today, initialState.selectedDate)
            assertEquals(false, initialState.isRefreshing)
            assertEquals(emptySet<NotificationCategory>(), initialState.excludedCategories)
        }
    }

    @Test
    fun `setDate updates selectedDate and refreshes summaries`() = runTest {
        val newDate = LocalDate.now().minusDays(1)
        viewModel.setDate(newDate)
        
        viewModel.selectedDate.test {
            assertEquals(newDate, awaitItem())
        }
        
        assertEquals(1, summaryRepository.generateSummariesForDateCallCount)
        assertEquals(newDate, summaryRepository.lastGeneratedDate)
    }

    @Test
    fun `toggleSummaryStyle switches between TIME_BASED and APP_BASED`() = runTest {
        // Initial state is TIME_BASED
        viewModel.toggleSummaryStyle()
        
        assertEquals(SummaryStyle.APP_BASED, userPreferencesRepository.lastSummaryStyle)
        
        // Update the preferences to simulate the flow update
        userPreferencesRepository.updatePreferences(
            userPreferencesRepository.userPreferences.copy(summaryStyle = SummaryStyle.APP_BASED)
        )
        
        viewModel.toggleSummaryStyle()
        
        assertEquals(SummaryStyle.TIME_BASED, userPreferencesRepository.lastSummaryStyle)
    }

    @Test
    fun `refreshSummaries generates summaries for selected date`() = runTest {
        val today = LocalDate.now()
        viewModel.refreshSummaries()
        
        assertEquals(1, summaryRepository.generateSummariesForDateCallCount)
        assertEquals(today, summaryRepository.lastGeneratedDate)
    }

    @Test
    fun `excludeCategory adds category to excluded list`() = runTest {
        viewModel.excludeCategory(NotificationCategory.SOCIAL_MEDIA)
        
        assertEquals(NotificationCategory.SOCIAL_MEDIA, userPreferencesRepository.lastAddedCategory)
    }

    @Test
    fun `includeCategory removes category from excluded list`() = runTest {
        viewModel.includeCategory(NotificationCategory.PROMOTION)
        
        assertEquals(NotificationCategory.PROMOTION, userPreferencesRepository.lastRemovedCategory)
    }
}

class FakeSummaryRepository : SummaryRepository {
    var generateSummariesForDateCallCount = 0
    var lastGeneratedDate: LocalDate? = null
    
    private val timeSummaries = listOf(
        NotificationSummary(
            id = "morning-today",
            period = TimePeriod.MORNING,
            date = LocalDate.now(),
            categories = mapOf(NotificationCategory.PERSONAL_MESSAGE to 3),
            highlights = listOf(),
            totalCount = 3
        )
    )
    
    private val appSummaries = listOf(
        AppNotificationSummary(
            packageName = "com.example.app",
            appName = "Example App",
            appIcon = null,
            notificationCount = 5,
            categories = mapOf(NotificationCategory.SOCIAL_MEDIA to 5),
            highlights = listOf()
        )
    )

    override fun getTimeSummaries(timeRange: TimeRange): Flow<List<NotificationSummary>> {
        return flowOf(timeSummaries)
    }

    override fun getTimeSummariesByDate(date: LocalDate): Flow<List<NotificationSummary>> {
        return flowOf(timeSummaries)
    }

    override fun getAppSummaries(timeRange: TimeRange): Flow<List<AppNotificationSummary>> {
        return flowOf(appSummaries)
    }

    override fun getAppSummary(packageName: String, timeRange: TimeRange): Flow<AppNotificationSummary?> {
        return flowOf(appSummaries.firstOrNull { it.packageName == packageName })
    }

    override suspend fun generateSummaries(timeRange: TimeRange) {
        // No-op in test
    }

    override suspend fun generateSummariesForDate(date: LocalDate) {
        generateSummariesForDateCallCount++
        lastGeneratedDate = date
    }

    override suspend fun clearSummariesOlderThan(date: LocalDate): Int {
        return 0
    }

    override suspend fun clearAllSummaries() {
        // No-op in test
    }
}

class FakeUserPreferencesRepository : UserPreferencesRepository {
    var userPreferences = UserPreferences()
    var lastSummaryStyle: SummaryStyle? = null
    var lastDarkThemeEnabled: Boolean? = null
    var lastRetentionPeriod: Int? = null
    var lastExcludedCategories: Set<NotificationCategory>? = null
    var lastAddedCategory: NotificationCategory? = null
    var lastRemovedCategory: NotificationCategory? = null
    
    private val _userPreferencesFlow = MutableStateFlow(userPreferences)
    override val userPreferencesFlow: Flow<UserPreferences> = _userPreferencesFlow

    fun updatePreferences(preferences: UserPreferences) {
        userPreferences = preferences
        _userPreferencesFlow.value = preferences
    }

    override suspend fun updateSummaryStyle(style: SummaryStyle) {
        lastSummaryStyle = style
        userPreferences = userPreferences.copy(summaryStyle = style)
        _userPreferencesFlow.value = userPreferences
    }

    override suspend fun toggleDarkTheme(enabled: Boolean) {
        lastDarkThemeEnabled = enabled
        userPreferences = userPreferences.copy(isDarkTheme = enabled)
        _userPreferencesFlow.value = userPreferences
    }

    override suspend fun setDataRetentionPeriod(days: Int) {
        lastRetentionPeriod = days
        userPreferences = userPreferences.copy(dataRetentionPeriod = days)
        _userPreferencesFlow.value = userPreferences
    }

    override suspend fun updateExcludedCategories(categories: Set<NotificationCategory>) {
        lastExcludedCategories = categories
        userPreferences = userPreferences.copy(notificationCategoriesToExclude = categories)
        _userPreferencesFlow.value = userPreferences
    }

    override suspend fun addExcludedCategory(category: NotificationCategory) {
        lastAddedCategory = category
        val updatedCategories = userPreferences.notificationCategoriesToExclude + category
        userPreferences = userPreferences.copy(notificationCategoriesToExclude = updatedCategories)
        _userPreferencesFlow.value = userPreferences
    }

    override suspend fun removeExcludedCategory(category: NotificationCategory) {
        lastRemovedCategory = category
        val updatedCategories = userPreferences.notificationCategoriesToExclude - category
        userPreferences = userPreferences.copy(notificationCategoriesToExclude = updatedCategories)
        _userPreferencesFlow.value = userPreferences
    }

    override suspend fun resetToDefaults() {
        userPreferences = UserPreferences()
        _userPreferencesFlow.value = userPreferences
    }
}