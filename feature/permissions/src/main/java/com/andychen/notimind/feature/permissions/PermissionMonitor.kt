package com.andychen.notimind.feature.permissions

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Monitors notification permission status and provides updates when permissions change.
 */
@Singleton
class PermissionMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissionChecker: NotificationPermissionChecker
) {
    private val _permissionState = MutableStateFlow(PermissionState())
    val permissionState: StateFlow<PermissionState> = _permissionState.asStateFlow()

    /**
     * Starts periodic permission monitoring using WorkManager.
     */
    fun startMonitoring() {
        // Initial check
        checkPermissions()
        
        // Schedule periodic checks
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()
            
        val permissionCheckRequest = PeriodicWorkRequestBuilder<PermissionCheckWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()
            
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERMISSION_CHECK_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            permissionCheckRequest
        )
    }

    /**
     * Checks current permission status and updates the state flow.
     */
    fun checkPermissions() {
        val hasPermission = permissionChecker.hasNotificationPermission()
        _permissionState.value = _permissionState.value.copy(
            hasNotificationPermission = hasPermission
        )
    }

    companion object {
        private const val PERMISSION_CHECK_WORK_NAME = "permission_check_work"
    }

    /**
     * Worker class that performs periodic permission checks.
     */
    class PermissionCheckWorker(
        appContext: Context,
        workerParams: WorkerParameters
    ) : Worker(appContext, workerParams) {
        
        override fun doWork(): Result {
            val permissionChecker = NotificationPermissionCheckerImpl(applicationContext)
            val hasPermission = permissionChecker.hasNotificationPermission()
            
            if (!hasPermission) {
                // Show notification that permission has been revoked
                showPermissionRevokedNotification(applicationContext)
            }
            
            return Result.success()
        }
        
        private fun showPermissionRevokedNotification(context: Context) {
            // In a real implementation, this would show a notification
            // For now, we'll just create an intent that could be used
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            // In a real implementation, we would use NotificationCompat.Builder
            // to create and show a notification
        }
    }
}