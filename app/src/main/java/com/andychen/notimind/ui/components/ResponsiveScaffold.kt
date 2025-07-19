package com.andychen.notimind.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Summarize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.andychen.notimind.navigation.Screen
import com.andychen.notimind.ui.theme.NotiMindTheme

/**
 * 响应式布局脚手架，根据屏幕尺寸调整导航和内容的布局
 */
@Composable
fun ResponsiveScaffold(
    windowWidthSizeClass: WindowWidthSizeClass,
    currentRoute: String,
    onNavigateToRoute: (String) -> Unit,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    when (windowWidthSizeClass) {
        WindowWidthSizeClass.Expanded -> {
            // 在大屏幕上使用侧边导航栏
            Row(modifier = modifier.fillMaxSize()) {
                NotiMindNavigationRail(
                    currentRoute = currentRoute,
                    onNavigateToRoute = onNavigateToRoute,
                    modifier = Modifier.fillMaxHeight()
                )
                
                // 主内容区域
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    content()
                }
            }
        }
        else -> {
            // 在中小屏幕上，内容占据整个区域（底部导航在MainActivity中处理）
            Surface(
                modifier = modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                content()
            }
        }
    }
}

/**
 * 侧边导航栏，用于大屏幕设备
 */
@Composable
fun NotiMindNavigationRail(
    currentRoute: String,
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val navigationItems = listOf(
        NotiMindBottomNavigationItem(
            route = Screen.Summary.route,
            selectedIcon = { Icon(Icons.Filled.Summarize, contentDescription = null) },
            unselectedIcon = { Icon(Icons.Outlined.Summarize, contentDescription = null) },
            label = "摘要"
        ),
        NotiMindBottomNavigationItem(
            route = Screen.Notifications.route,
            selectedIcon = { Icon(Icons.Filled.Notifications, contentDescription = null) },
            unselectedIcon = { Icon(Icons.Outlined.Notifications, contentDescription = null) },
            label = "通知"
        ),
        NotiMindBottomNavigationItem(
            route = Screen.Settings.route,
            selectedIcon = { Icon(Icons.Filled.Settings, contentDescription = null) },
            unselectedIcon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
            label = "设置"
        )
    )

    Surface(
        modifier = modifier.width(80.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        NavigationRail {
            navigationItems.forEach { item ->
                val selected = currentRoute.startsWith(item.route)
                
                NavigationRailItem(
                    icon = {
                        if (selected) {
                            item.selectedIcon()
                        } else {
                            item.unselectedIcon()
                        }
                    },
                    label = { Text(item.label) },
                    selected = selected,
                    onClick = { onNavigateToRoute(item.route) }
                )
            }
        }
    }
}

@Preview(widthDp = 1200)
@Composable
fun ResponsiveScaffoldExpandedPreview() {
    NotiMindTheme {
        ResponsiveScaffold(
            windowWidthSizeClass = WindowWidthSizeClass.Expanded,
            currentRoute = Screen.Summary.route,
            onNavigateToRoute = {},
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text("大屏幕内容示例", style = MaterialTheme.typography.headlineMedium)
                }
            }
        )
    }
}

@Preview(widthDp = 400)
@Composable
fun ResponsiveScaffoldCompactPreview() {
    NotiMindTheme {
        ResponsiveScaffold(
            windowWidthSizeClass = WindowWidthSizeClass.Compact,
            currentRoute = Screen.Summary.route,
            onNavigateToRoute = {},
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text("小屏幕内容示例", style = MaterialTheme.typography.headlineMedium)
                }
            }
        )
    }
}