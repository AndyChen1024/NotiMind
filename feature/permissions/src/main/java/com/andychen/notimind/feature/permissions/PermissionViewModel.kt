package com.andychen.notimind.feature.permissions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PermissionViewModel @Inject constructor(
    private val permissionChecker: NotificationPermissionChecker
) : ViewModel() {

    var permissionState by mutableStateOf(PermissionState())
        private set

    init {
        checkNotificationPermission()
    }

    fun checkNotificationPermission() {
        viewModelScope.launch {
            val hasPermission = permissionChecker.hasNotificationPermission()
            permissionState = permissionState.copy(hasNotificationPermission = hasPermission)
        }
    }

    fun requestNotificationPermission(context: Context) {
        if (context is Activity) {
            permissionChecker.requestNotificationPermission(context)
            permissionState = permissionState.copy(
                permissionDeniedCount = permissionState.permissionDeniedCount + 1
            )
        }
    }

    fun openNotificationSettings(context: Context) {
        val intent = Intent()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        } else {
            intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
            intent.putExtra("app_package", context.packageName)
            intent.putExtra("app_uid", context.applicationInfo.uid)
        }
        context.startActivity(intent)
    }
}

data class PermissionState(
    val hasNotificationPermission: Boolean = false,
    val permissionDeniedCount: Int = 0
)