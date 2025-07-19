package com.andychen.notimind.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Summarize
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.andychen.notimind.R
import com.andychen.notimind.navigation.Screen
import com.andychen.notimind.ui.theme.NotiMindTheme

/**
 * 底部导航项数据类
 */
data class NotiMindBottomNavigationItem(
    val route: String,
    val selectedIcon: @Composable () -> Unit,
    val unselectedIcon: @Composable () -> Unit,
    val label: String
)

/**
 * 应用底部导航栏
 */
@Composable
fun NotiMindBottomNavigation(
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

    NavigationBar(modifier = modifier) {
        navigationItems.forEach { item ->
            val selected = currentRoute.startsWith(item.route)
            
            NavigationBarItem(
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

@Preview
@Composable
fun NotiMindBottomNavigationPreview() {
    NotiMindTheme {
        NotiMindBottomNavigation(
            currentRoute = Screen.Summary.route,
            onNavigateToRoute = {}
        )
    }
}