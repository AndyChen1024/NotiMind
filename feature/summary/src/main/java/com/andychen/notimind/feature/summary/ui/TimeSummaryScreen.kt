package com.andychen.notimind.feature.summary.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.andychen.notimind.core.model.NotificationCategory
import com.andychen.notimind.core.model.NotificationSummary
import com.andychen.notimind.core.model.SummaryHighlight
import com.andychen.notimind.core.model.TimePeriod
import com.andychen.notimind.feature.summary.SummaryViewModel
import com.andychen.notimind.ui.common.components.ErrorState
import com.andychen.notimind.ui.common.components.LoadingState
import com.andychen.notimind.ui.common.components.SectionHeader
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSummaryScreen(
    modifier: Modifier = Modifier,
    viewModel: SummaryViewModel = hiltViewModel()
) {
    val summaryState by viewModel.summaryState.collectAsState()
    val timeSummaries by viewModel.timeSummaries.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("时间摘要") },
                actions = {
                    IconButton(onClick = { viewModel.refreshSummaries() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "刷新"
                        )
                    }
                    IconButton(onClick = { showDatePicker = true }) {
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
            DateNavigationBar(
                selectedDate = summaryState.selectedDate,
                onPreviousDay = { viewModel.previousDay() },
                onNextDay = { viewModel.nextDay() }
            )
            
            if (showDatePicker) {
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = summaryState.selectedDate
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()
                )
                
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val selectedDate = Instant.ofEpochMilli(millis)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                                viewModel.setDate(selectedDate)
                            }
                            showDatePicker = false
                        }) {
                            Text("确认")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("取消")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }
            
            if (summaryState.isRefreshing) {
                LoadingState()
            } else if (timeSummaries.isEmpty()) {
                EmptyTimeSummaryState(
                    date = summaryState.selectedDate,
                    onRefresh = { viewModel.refreshSummaries() }
                )
            } else {
                TimeSummaryContent(
                    summaries = timeSummaries,
                    excludedCategories = summaryState.excludedCategories,
                    onExcludeCategory = { viewModel.excludeCategory(it) },
                    onIncludeCategory = { viewModel.includeCategory(it) }
                )
            }
        }
    }
}

@Composable
fun DateNavigationBar(
    selectedDate: LocalDate,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val isToday = selectedDate.isEqual(today)
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日")
    val dayOfWeek = selectedDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.CHINESE)
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledIconButton(onClick = onPreviousDay) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "前一天"
            )
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = selectedDate.format(dateFormatter),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (isToday) "今天 · $dayOfWeek" else dayOfWeek,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        FilledIconButton(
            onClick = onNextDay,
            enabled = selectedDate.isBefore(today)
        ) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "后一天"
            )
        }
    }
}

@Composable
fun TimeSummaryContent(
    summaries: List<NotificationSummary>,
    excludedCategories: Set<NotificationCategory>,
    onExcludeCategory: (NotificationCategory) -> Unit,
    onIncludeCategory: (NotificationCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(summaries.sortedBy { it.period.ordinal }) { summary ->
            TimePeriodSummaryCard(
                summary = summary,
                excludedCategories = excludedCategories,
                onExcludeCategory = onExcludeCategory,
                onIncludeCategory = onIncludeCategory
            )
        }
    }
}

@Composable
fun TimePeriodSummaryCard(
    summary: NotificationSummary,
    excludedCategories: Set<NotificationCategory>,
    onExcludeCategory: (NotificationCategory) -> Unit,
    onIncludeCategory: (NotificationCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val periodName = when (summary.period) {
        TimePeriod.MORNING -> "早晨"
        TimePeriod.AFTERNOON -> "下午"
        TimePeriod.EVENING -> "晚上"
        TimePeriod.NIGHT -> "夜间"
        TimePeriod.ALL_DAY -> "全天"
    }
    
    val periodIcon = when (summary.period) {
        TimePeriod.MORNING -> Icons.Outlined.Alarm
        TimePeriod.AFTERNOON -> Icons.Outlined.Notifications
        TimePeriod.EVENING -> Icons.Outlined.Notifications
        TimePeriod.NIGHT -> Icons.Outlined.Alarm
        TimePeriod.ALL_DAY -> Icons.Outlined.Notifications
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = periodIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.size(8.dp))
                
                Text(
                    text = "$periodName · ${summary.totalCount}条通知",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            CategoryDistribution(
                categories = summary.categories,
                excludedCategories = excludedCategories,
                onExcludeCategory = onExcludeCategory,
                onIncludeCategory = onIncludeCategory
            )
            
            if (summary.highlights.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                
                SectionHeader(title = "重要内容")
                
                Spacer(modifier = Modifier.height(8.dp))
                
                summary.highlights.forEach { highlight ->
                    HighlightItem(highlight = highlight)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun CategoryDistribution(
    categories: Map<NotificationCategory, Int>,
    excludedCategories: Set<NotificationCategory>,
    onExcludeCategory: (NotificationCategory) -> Unit,
    onIncludeCategory: (NotificationCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SectionHeader(title = "通知类别")
        
        Spacer(modifier = Modifier.height(8.dp))
        
        categories.entries.sortedByDescending { it.value }.forEach { (category, count) ->
            val isExcluded = category in excludedCategories
            
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                onClick = { 
                    if (isExcluded) onIncludeCategory(category) else onExcludeCategory(category)
                },
                colors = CardDefaults.outlinedCardColors(
                    containerColor = if (isExcluded) 
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    else 
                        MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = getCategoryName(category),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isExcluded) 
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        else 
                            MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = "$count",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isExcluded) 
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        else 
                            MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun HighlightItem(
    highlight: SummaryHighlight,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = highlight.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = highlight.content,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun EmptyTimeSummaryState(
    date: LocalDate,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isToday = date.isEqual(LocalDate.now())
    val message = if (isToday) {
        "今天还没有收集到通知摘要"
    } else {
        "这一天没有通知摘要"
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
                imageVector = Icons.Outlined.Notifications,
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

@Composable
fun getCategoryName(category: NotificationCategory): String {
    return when (category) {
        NotificationCategory.PERSONAL_MESSAGE -> "个人消息"
        NotificationCategory.GROUP_MESSAGE -> "群组消息"
        NotificationCategory.EMAIL -> "电子邮件"
        NotificationCategory.SOCIAL_MEDIA -> "社交媒体"
        NotificationCategory.NEWS -> "新闻资讯"
        NotificationCategory.PROMOTION -> "促销广告"
        NotificationCategory.SYSTEM -> "系统通知"
        NotificationCategory.ALERT -> "重要提醒"
        NotificationCategory.OTHER -> "其他"
    }
}