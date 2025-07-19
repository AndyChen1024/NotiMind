package com.andychen.notimind.feature.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andychen.notimind.core.data.repository.NotificationRepository
import com.andychen.notimind.core.model.NotificationEntity
import com.andychen.notimind.core.model.TimeRange
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedAppPackage = MutableStateFlow<String?>(null)
    val selectedAppPackage: StateFlow<String?> = _selectedAppPackage

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    val uiState: StateFlow<NotificationUiState> = combine(
        _selectedDate,
        _searchQuery,
        _selectedAppPackage,
        _isRefreshing
    ) { date, query, appPackage, refreshing ->
        NotificationUiState(
            selectedDate = date,
            searchQuery = query,
            selectedAppPackage = appPackage,
            isRefreshing = refreshing
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NotificationUiState()
    )

    val notifications: StateFlow<List<NotificationEntity>> = combine(
        _selectedDate,
        _searchQuery,
        _selectedAppPackage
    ) { date, query, appPackage ->
        val timeRange = TimeRange.forDay(date)
        
        val baseFlow = if (appPackage != null) {
            notificationRepository.getNotificationsByApp(appPackage, timeRange)
        } else {
            notificationRepository.getNotifications(timeRange)
        }
        
        baseFlow.map { notifications ->
            if (query.isBlank()) {
                notifications
            } else {
                notifications.filter { notification ->
                    notification.title?.contains(query, ignoreCase = true) == true ||
                    notification.content?.contains(query, ignoreCase = true) == true ||
                    notification.appName.contains(query, ignoreCase = true)
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        ).value
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val availableApps: StateFlow<List<String>> = notificationRepository.getAllPackageNames()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun previousDay() {
        _selectedDate.value = _selectedDate.value.minusDays(1)
    }

    fun nextDay() {
        _selectedDate.value = _selectedDate.value.plusDays(1)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    fun selectApp(packageName: String?) {
        _selectedAppPackage.value = packageName
    }

    fun clearAppFilter() {
        _selectedAppPackage.value = null
    }

    fun deleteNotification(notification: NotificationEntity) {
        viewModelScope.launch {
            notificationRepository.deleteNotification(notification)
        }
    }

    fun exportNotifications(format: NotificationRepository.ExportFormat = NotificationRepository.ExportFormat.JSON) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val timeRange = TimeRange.forDay(_selectedDate.value)
                notificationRepository.exportNotifications(timeRange, format)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun clearNotificationsForCurrentDay() {
        viewModelScope.launch {
            val timeRange = TimeRange.forDay(_selectedDate.value)
            notificationRepository.clearNotifications(timeRange)
        }
    }
}

data class NotificationUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val searchQuery: String = "",
    val selectedAppPackage: String? = null,
    val isRefreshing: Boolean = false
)