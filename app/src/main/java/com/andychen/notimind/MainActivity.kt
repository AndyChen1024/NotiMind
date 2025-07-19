package com.andychen.notimind

import android.os.Bundle
import androidx.activity.ComponentActivity
import dagger.hilt.android.AndroidEntryPoint
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.andychen.notimind.feature.permissions.NotificationPermissionChecker
import com.andychen.notimind.navigation.NotiMindNavHost
import com.andychen.notimind.navigation.NotiMindNavigationActions
import com.andychen.notimind.navigation.Screen
import com.andychen.notimind.ui.components.NotiMindBottomNavigation
import com.andychen.notimind.ui.components.NotiMindTopAppBar
import com.andychen.notimind.ui.components.ResponsiveScaffold
import com.andychen.notimind.ui.theme.NotiMindTheme
import com.andychen.notimind.ui.theme.ThemeViewModel
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var permissionChecker: NotificationPermissionChecker
    
    private val themeViewModel: ThemeViewModel by viewModels()
    
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeState by themeViewModel.themeState.collectAsState()
            val windowSizeClass = calculateWindowSizeClass(this)
            
            NotiMindTheme(
                darkTheme = themeState.isDarkTheme,
                dynamicColor = themeState.useDynamicColor
            ) {
                NotiMindApp(
                    windowWidthSizeClass = windowSizeClass.widthSizeClass,
                    permissionChecker = permissionChecker
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotiMindApp(
    windowWidthSizeClass: WindowWidthSizeClass,
    permissionChecker: NotificationPermissionChecker
) {
    val navController = rememberNavController()
    val navigationActions = remember(navController) {
        NotiMindNavigationActions(navController)
    }
    
    // Get current route to determine active screen
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route ?: Screen.Summary.route
    
    // Determine if we should show the bottom navigation
    val shouldShowBottomBar by remember(currentDestination) {
        derivedStateOf {
            currentDestination?.route?.let { route ->
                // Show bottom bar only on main screens
                route == Screen.Summary.route ||
                route == Screen.Notifications.route ||
                route == Screen.Settings.route
            } ?: false
        }
    }
    
    // Determine if we can navigate back
    val canNavigateBack by remember(currentDestination) {
        derivedStateOf {
            navController.previousBackStackEntry != null
        }
    }
    
    // Get screen title based on current route
    val currentScreenTitle = remember(currentDestination) {
        getScreenTitle(currentDestination)
    }
    
    // Check for notification permission on app start
    LaunchedEffect(Unit) {
        if (!permissionChecker.hasNotificationPermission()) {
            navigationActions.navigateToPermissionRequest()
        }
    }
    
    // Set up scrolling behavior for the top app bar
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    
    // 使用响应式脚手架来处理不同屏幕尺寸
    if (windowWidthSizeClass == WindowWidthSizeClass.Expanded && shouldShowBottomBar) {
        // 大屏幕使用响应式布局，带侧边导航栏
        ResponsiveScaffold(
            windowWidthSizeClass = windowWidthSizeClass,
            currentRoute = currentRoute,
            onNavigateToRoute = { route ->
                navController.navigate(route) {
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            modifier = Modifier.fillMaxSize(),
            content = {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                    topBar = {
                        NotiMindTopAppBar(
                            title = currentScreenTitle,
                            canNavigateBack = canNavigateBack,
                            navigateUp = { navController.navigateUp() },
                            actions = {
                                IconButton(onClick = { /* TODO: Implement search */ }) {
                                    Icon(
                                        imageVector = Icons.Filled.Search,
                                        contentDescription = "搜索"
                                    )
                                }
                            },
                            scrollBehavior = scrollBehavior
                        )
                    }
                ) { innerPadding ->
                    NotiMindNavHost(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        )
    } else {
        // 中小屏幕使用标准布局，带底部导航栏
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                NotiMindTopAppBar(
                    title = currentScreenTitle,
                    canNavigateBack = canNavigateBack && !shouldShowBottomBar,
                    navigateUp = { navController.navigateUp() },
                    actions = {
                        // Only show search on main screens
                        if (shouldShowBottomBar) {
                            IconButton(onClick = { /* TODO: Implement search */ }) {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = "搜索"
                                )
                            }
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            },
            bottomBar = {
                // Show bottom navigation only on main screens
                if (shouldShowBottomBar) {
                    NotiMindBottomNavigation(
                        currentRoute = currentRoute,
                        onNavigateToRoute = { route ->
                            navController.navigate(route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            NotiMindNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

/**
 * 根据当前目的地获取屏幕标题
 */
private fun getScreenTitle(currentDestination: NavDestination?): String {
    return when {
        currentDestination?.hierarchy?.any { it.route == Screen.Summary.route } == true -> "通知摘要"
        currentDestination?.hierarchy?.any { it.route == Screen.Notifications.route } == true -> "通知列表"
        currentDestination?.hierarchy?.any { it.route == Screen.Settings.route } == true -> "设置"
        currentDestination?.hierarchy?.any { it.route == Screen.PermissionRequest.route } == true -> "权限请求"
        currentDestination?.hierarchy?.any { it.route?.startsWith("notification/") == true } == true -> "通知详情"
        currentDestination?.hierarchy?.any { it.route?.startsWith("app_summary/") == true } == true -> "应用摘要"
        currentDestination?.hierarchy?.any { it.route?.startsWith("time_summary/") == true } == true -> "时间段摘要"
        else -> "NotiMind"
    }
}