package com.andychen.notimind.feature.summary.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import com.andychen.notimind.core.model.SummaryStyle
import com.andychen.notimind.feature.summary.SummaryViewModel

enum class SummaryTab(val title: String, val icon: ImageVector) {
    TIME("时间", Icons.Default.Schedule),
    APP("应用", Icons.Default.Apps)
}

@Composable
fun SummaryScreen(
    modifier: Modifier = Modifier,
    viewModel: SummaryViewModel = hiltViewModel(),
    onAppClick: (String) -> Unit = {},
    onTimeClick: (String) -> Unit = {}
) {
    val summaryState by viewModel.summaryState.collectAsState()
    
    // 根据用户偏好设置初始选中的标签
    var selectedTab by remember(summaryState.summaryStyle) { 
        mutableStateOf(
            when (summaryState.summaryStyle) {
                SummaryStyle.TIME_BASED -> SummaryTab.TIME
                SummaryStyle.APP_BASED -> SummaryTab.APP
            }
        ) 
    }
    
    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                SummaryTab.values().forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { 
                            selectedTab = tab
                            // 更新用户偏好
                            viewModel.setSummaryStyle(
                                when (tab) {
                                    SummaryTab.TIME -> SummaryStyle.TIME_BASED
                                    SummaryTab.APP -> SummaryStyle.APP_BASED
                                }
                            )
                        },
                        text = { Text(tab.title) },
                        icon = { Icon(imageVector = tab.icon, contentDescription = null) }
                    )
                }
            }
            
            when (selectedTab) {
                SummaryTab.TIME -> TimeSummaryScreen(
                    viewModel = viewModel,
                    modifier = Modifier.weight(1f)
                )
                SummaryTab.APP -> AppSummaryScreen(
                    viewModel = viewModel,
                    modifier = Modifier.weight(1f),
                    onAppClick = onAppClick
                )
            }
        }
    }
}