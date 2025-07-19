package com.andychen.notimind.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andychen.notimind.core.data.repository.NotificationRepository
import com.andychen.notimind.core.data.repository.UserPreferencesRepository
import com.andychen.notimind.core.model.SummaryStyle
import com.andychen.notimind.core.model.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _showDataClearDialog = MutableStateFlow(false)
    val showDataClearDialog: StateFlow<Boolean> = _showDataClearDialog

    private val _showExportDialog = MutableStateFlow(false)
    val showExportDialog: StateFlow<Boolean> = _showExportDialog

    val userPreferences: StateFlow<UserPreferences> = userPreferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    val notificationCount: StateFlow<Int> = notificationRepository.getNotificationCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val uiState: StateFlow<SettingsUiState> = combine(
        userPreferences,
        notificationCount,
        _isLoading,
        _showDataClearDialog,
        _showExportDialog
    ) { preferences, count, loading, showClearDialog, showExportDialog ->
        SettingsUiState(
            userPreferences = preferences,
            notificationCount = count,
            isLoading = loading,
            showDataClearDialog = showClearDialog,
            showExportDialog = showExportDialog
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun updateSummaryStyle(style: SummaryStyle) {
        viewModelScope.launch {
            userPreferencesRepository.updateSummaryStyle(style)
        }
    }

    fun toggleDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.toggleDarkTheme(enabled)
        }
    }

    fun updateDataRetentionPeriod(days: Int) {
        viewModelScope.launch {
            userPreferencesRepository.setDataRetentionPeriod(days)
        }
    }

    fun showDataClearDialog() {
        _showDataClearDialog.value = true
    }

    fun hideDataClearDialog() {
        _showDataClearDialog.value = false
    }

    fun showExportDialog() {
        _showExportDialog.value = true
    }

    fun hideExportDialog() {
        _showExportDialog.value = false
    }

    fun clearAllData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                notificationRepository.clearAllNotifications()
                _showDataClearDialog.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearOldData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val retentionPeriod = userPreferences.value.dataRetentionPeriod
                notificationRepository.clearNotificationsOlderThan(retentionPeriod)
                _showDataClearDialog.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun exportData(format: NotificationRepository.ExportFormat) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Export all data by using a very wide time range
                val timeRange = com.andychen.notimind.core.model.TimeRange.allTime()
                notificationRepository.exportNotifications(timeRange, format)
                _showExportDialog.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun generateSampleData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val fakeNotifications = com.andychen.notimind.core.data.util.FakeDataGenerator.generateFakeNotifications(50)
                fakeNotifications.forEach { notification ->
                    notificationRepository.saveNotification(notification)
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
}

data class SettingsUiState(
    val userPreferences: UserPreferences = UserPreferences(),
    val notificationCount: Int = 0,
    val isLoading: Boolean = false,
    val showDataClearDialog: Boolean = false,
    val showExportDialog: Boolean = false
)