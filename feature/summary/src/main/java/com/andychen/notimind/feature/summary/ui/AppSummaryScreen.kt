package com.andychen.notimind.feature.summary.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import com.andychen.notimind.core.model.AppNotificationSummary
import com.andychen.notimind.core.model.NotificationCategory
import com.andychen.notimind.feature.summary.SummaryViewModel
import com.andychen.notimind.ui.common.components.LoadingState
import com.andychen.notimind.ui.common.components.SectionHeader
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

enum class AppSortOrder {
    NOTIFICATION_COUNT_DESC,
    NOTIFICATION_COUNT_ASC,
    APP_NAME_ASC,
    APP_NAME_DESC
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSummaryScreen(
    modifier: Modifier = Modifier,
    viewModel: SummaryViewModel = hiltViewModel(),
    onAppClick: (String) -> Unit = {}
) {
    val summaryState by viewModel.summaryState.collectAsState()
    val appSummaries by viewModel.appSummaries.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var sortOrder by remember { mutableStateOf(AppSortOrder.NOTIFICATION_COUNT_DESC) }
    var showSortMenu by remember { mutableStateOf(false) }
    
    val filteredSummaries = remember(appSummaries, searchQuery, sortOrder) {
        appSummaries
            .filter { summary ->
                searchQuery.isEmpty() || summary.appName.contains(searchQuery, ignoreCase = true)
            }
            .sortedWith(when (sortOrder) {
                AppSortOrder.NOTIFICATION_COUNT_DESC -> compareByDescending { it.notificationCount }
                AppSortOrder.NOTIFICATION_COUNT_ASC -> compareBy { it.notificationCount }
                AppSortOrder.APP_NAME_ASC -> compareBy { it.appName }
                AppSortOrder.APP_NAME_DESC -> compareByDescending { it.appName }
            })
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("应用摘要") },
                actions = {
                    IconButton(onClick = { viewModel.refreshSummaries() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "刷新"
                        )
                    }
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = "排序"
                            )
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("通知数量 (多到少)") },
                                onClick = {
                                    sortOrder = AppSortOrder.NOTIFICATION_COUNT_DESC
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("通知数量 (少到多)") },
                                onClick = {
                                    sortOrder = AppSortOrder.NOTIFICATION_COUNT_ASC
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("应用名称 (A-Z)") },
                                onClick = {
                                    sortOrder = AppSortOrder.APP_NAME_ASC
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("应用名称 (Z-A)") },
                                onClick = {
                                    sortOrder = AppSortOrder.APP_NAME_DESC
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                    IconButton(onClick = { /* 日历选择 */ }) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "选择日期"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 日期显示
            DateNavigationBar(
                selectedDate = summaryState.selectedDate,
                onPreviousDay = { viewModel.previousDay() },
                onNextDay = { viewModel.nextDay() }
            )
            
            // 搜索框
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("搜索应用") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                },
                singleLine = true
            )
            
            if (summaryState.isRefreshing) {
                LoadingState()
            } else if (filteredSummaries.isEmpty()) {
                EmptyAppSummaryState(
                    hasSearch = searchQuery.isNotEmpty(),
                    onRefresh = { viewModel.refreshSummaries() }
                )
            } else {
                AppSummaryContent(
                    appSummaries = filteredSummaries,
                    onAppClick = onAppClick
                )
            }
        }
    }
}

@Composable
fun AppSummaryContent(
    appSummaries: List<AppNotificationSummary>,
    onAppClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(appSummaries) { summary ->
            AppSummaryCard(
                summary = summary,
                onClick = { onAppClick(summary.packageName) }
            )
        }
    }
}

@Composable
fun AppSummaryCard(
    summary: AppNotificationSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 应用图标
                val appIcon = summary.appIcon
                if (appIcon != null) {
                    androidx.compose.foundation.Image(
                        bitmap = appIcon.toBitmap().asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(MaterialTheme.shapes.small)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(MaterialTheme.shapes.small)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Apps,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = summary.appName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Text(
                        text = "${summary.notificationCount}条通知",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 类别分布
            SectionHeader(title = "通知类别")
            
            Spacer(modifier = Modifier.height(8.dp))
            
            summary.categories.entries
                .sortedByDescending { it.value }
                .take(3)
                .forEach { (category, count) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = getCategoryName(category),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Text(
                            text = "$count",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            
            // 如果有更多类别，显示"更多"
            if (summary.categories.size > 3) {
                Text(
                    text = "还有${summary.categories.size - 3}个类别...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.End)
                )
            }
            
            // 重要内容
            if (summary.highlights.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                
                SectionHeader(title = "重要内容")
                
                Spacer(modifier = Modifier.height(8.dp))
                
                summary.highlights.take(1).forEach { highlight ->
                    HighlightItem(highlight = highlight)
                }
                
                if (summary.highlights.size > 1) {
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "还有${summary.highlights.size - 1}条重要内容...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyAppSummaryState(
    hasSearch: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val message = if (hasSearch) {
        "没有找到匹配的应用"
    } else {
        "没有应用通知摘要"
    }
    
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Apps,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (!hasSearch) {
                Spacer(modifier = Modifier.height(16.dp))
                
                FilledIconButton(onClick = onRefresh) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "刷新"
                    )
                }
            }
        }
    }
}