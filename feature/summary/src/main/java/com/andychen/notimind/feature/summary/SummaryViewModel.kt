package com.andychen.notimind.feature.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andychen.notimind.core.data.repository.SummaryRepository
import com.andychen.notimind.core.data.repository.UserPreferencesRepository
import com.andychen.notimind.core.model.AppNotificationSummary
import com.andychen.notimind.core.model.NotificationSummary
import com.andychen.notimind.core.model.SummaryStyle
import com.andychen.notimind.core.model.TimeRange
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class SummaryViewModel @Inject constructor(
    private val summaryRepository: SummaryRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    val summaryState: StateFlow<SummaryUiState> = combine(
        userPreferencesRepository.userPreferencesFlow,
        _selectedDate,
        _isRefreshing
    ) { preferences, date, isRefreshing ->
        SummaryUiState(
            summaryStyle = preferences.summaryStyle,
            selectedDate = date,
            isRefreshing = isRefreshing,
            excludedCategories = preferences.notificationCategoriesToExclude
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SummaryUiState()
    )

    val timeSummaries: StateFlow<List<NotificationSummary>> = combine(
        _selectedDate,
        userPreferencesRepository.userPreferencesFlow
    ) { date, preferences ->
        summaryRepository.getTimeSummariesByDate(date).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        ).value
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val appSummaries: StateFlow<List<AppNotificationSummary>> = combine(
        _selectedDate,
        userPreferencesRepository.userPreferencesFlow
    ) { date, preferences ->
        val timeRange = TimeRange.forDay(date)
        summaryRepository.getAppSummaries(timeRange).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        ).value
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        refreshSummaries()
    }

    fun setDate(date: LocalDate) {
        _selectedDate.value = date
        refreshSummaries()
    }

    fun previousDay() {
        _selectedDate.value = _selectedDate.value.minusDays(1)
        refreshSummaries()
    }

    fun nextDay() {
        _selectedDate.value = _selectedDate.value.plusDays(1)
        refreshSummaries()
    }

    fun toggleSummaryStyle() {
        viewModelScope.launch {
            val currentStyle = summaryState.value.summaryStyle
            val newStyle = if (currentStyle == SummaryStyle.TIME_BASED) {
                SummaryStyle.APP_BASED
            } else {
                SummaryStyle.TIME_BASED
            }
            userPreferencesRepository.updateSummaryStyle(newStyle)
        }
    }

    fun setSummaryStyle(style: SummaryStyle) {
        viewModelScope.launch {
            userPreferencesRepository.updateSummaryStyle(style)
        }
    }

    fun refreshSummaries() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                summaryRepository.generateSummariesForDate(_selectedDate.value)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun excludeCategory(category: com.andychen.notimind.core.model.NotificationCategory) {
        viewModelScope.launch {
            userPreferencesRepository.addExcludedCategory(category)
        }
    }

    fun includeCategory(category: com.andychen.notimind.core.model.NotificationCategory) {
        viewModelScope.launch {
            userPreferencesRepository.removeExcludedCategory(category)
        }
    }
}

data class SummaryUiState(
    val summaryStyle: SummaryStyle = SummaryStyle.TIME_BASED,
    val selectedDate: LocalDate = LocalDate.now(),
    val isRefreshing: Boolean = false,
    val excludedCategories: Set<com.andychen.notimind.core.model.NotificationCategory> = emptySet()
)