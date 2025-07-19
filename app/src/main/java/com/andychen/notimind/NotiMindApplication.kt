package com.andychen.notimind

import android.app.Application
import com.andychen.notimind.feature.permissions.PermissionMonitor
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class NotiMindApplication : Application() {
    
    @Inject
    lateinit var permissionMonitor: PermissionMonitor
    
    override fun onCreate() {
        super.onCreate()
        
        // Start permission monitoring
        permissionMonitor.startMonitoring()
    }
}