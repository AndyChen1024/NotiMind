package com.andychen.notimind.navigation

import android.content.Intent
import android.net.Uri

/**
 * 用于导航测试的工具类
 */
object TestNavigationUtils {
    
    /**
     * 创建用于测试深层链接的Intent
     * 
     * @param deepLink 深层链接URI字符串
     * @return 包含深层链接的Intent
     */
    fun createDeepLinkIntent(deepLink: String): Intent {
        return Intent(
            Intent.ACTION_VIEW,
            Uri.parse(deepLink)
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }
}