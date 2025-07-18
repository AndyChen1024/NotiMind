package com.andychen.notimind.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andychen.notimind.core.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for managing theme-related state and user preferences
 */
@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    /**
     * StateFlow representing the current theme state
     */
    val themeState: StateFlow<ThemeState> = userPreferencesRepository.userPreferencesFlow
        .map { preferences ->
            ThemeState(
                isDarkTheme = preferences.isDarkTheme,
                useDynamicColor = true // Default to true, could be made configurable in the future
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeState()
        )

    /**
     * Toggle between light and dark theme
     */
    fun toggleDarkTheme() {
        viewModelScope.launch {
            userPreferencesRepository.toggleDarkTheme(!themeState.value.isDarkTheme)
        }
    }

    /**
     * Set dark theme explicitly
     */
    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.toggleDarkTheme(enabled)
        }
    }
}

/**
 * Data class representing the current theme state
 */
data class ThemeState(
    val isDarkTheme: Boolean = false,
    val useDynamicColor: Boolean = true
)