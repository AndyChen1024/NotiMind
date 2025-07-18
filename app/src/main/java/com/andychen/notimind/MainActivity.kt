package com.andychen.notimind

import android.os.Bundle
import androidx.activity.ComponentActivity
import dagger.hilt.android.AndroidEntryPoint
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.andychen.notimind.feature.permissions.NotificationPermissionChecker
import com.andychen.notimind.navigation.NotiMindNavHost
import com.andychen.notimind.navigation.Screen
import com.andychen.notimind.ui.theme.NotiMindTheme
import com.andychen.notimind.ui.theme.ThemeViewModel
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var permissionChecker: NotificationPermissionChecker
    
    private val themeViewModel: ThemeViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeState by themeViewModel.themeState.collectAsState()
            
            NotiMindTheme(
                darkTheme = themeState.isDarkTheme,
                dynamicColor = themeState.useDynamicColor
            ) {
                val navController = rememberNavController()
                
                // Check for notification permission on app start
                LaunchedEffect(Unit) {
                    if (!permissionChecker.hasNotificationPermission()) {
                        navController.navigate(Screen.PermissionRequest.route) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                    }
                }
                
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NotiMindNavHost(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NotiMindTheme {
        Greeting("Android")
    }
}