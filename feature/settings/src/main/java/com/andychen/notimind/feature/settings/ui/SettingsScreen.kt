package com.andychen.notimind.feature.settings.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andychen.notimind.core.data.repository.NotificationRepository
import com.andychen.notimind.core.model.SummaryStyle
import com.andychen.notimind.feature.settings.SettingsViewModel
// import com.andychen.notimind.ui.theme.NotiMindTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Top App Bar
        TopAppBar(
            title = { 
                Text(
                    text = "设置",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        )
        
        // Settings content
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Display preferences section
            SettingsSection(title = "显示偏好") {
                SummaryStylePreference(
                    currentStyle = uiState.userPreferences.summaryStyle,
                    onStyleChange = viewModel::updateSummaryStyle
                )
                
                ThemePreference(
                    isDarkTheme = uiState.userPreferences.isDarkTheme,
                    onThemeChange = viewModel::toggleDarkTheme
                )
            }
            
            // Data management section
            SettingsSection(title = "数据管理") {
                DataRetentionPreference(
                    currentDays = uiState.userPreferences.dataRetentionPeriod,
                    onDaysChange = viewModel::updateDataRetentionPeriod
                )
                
                DataInfoCard(notificationCount = uiState.notificationCount)
                
                // Generate sample data button for testing
                OutlinedButton(
                    onClick = viewModel::generateSampleData,
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("生成示例数据（用于测试）")
                }
                
                DataActionButtons(
                    onExportClick = viewModel::showExportDialog,
                    onClearClick = viewModel::showDataClearDialog,
                    isLoading = uiState.isLoading
                )
            }
            
            // App info section
            SettingsSection(title = "应用信息") {
                AppInfoCard()
            }
        }
    }
    
    // Dialogs
    if (uiState.showDataClearDialog) {
        DataClearDialog(
            onDismiss = viewModel::hideDataClearDialog,
            onClearAll = viewModel::clearAllData,
            onClearOld = viewModel::clearOldData,
            retentionDays = uiState.userPreferences.dataRetentionPeriod
        )
    }
    
    if (uiState.showExportDialog) {
        ExportDialog(
            onDismiss = viewModel::hideExportDialog,
            onExportJson = { viewModel.exportData(NotificationRepository.ExportFormat.JSON) },
            onExportCsv = { viewModel.exportData(NotificationRepository.ExportFormat.CSV) }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            content()
        }
    }
}

@Composable
private fun SummaryStylePreference(
    currentStyle: SummaryStyle,
    onStyleChange: (SummaryStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "摘要显示方式",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                onClick = { onStyleChange(SummaryStyle.TIME_BASED) },
                label = { Text("按时间") },
                selected = currentStyle == SummaryStyle.TIME_BASED
            )
            
            FilterChip(
                onClick = { onStyleChange(SummaryStyle.APP_BASED) },
                label = { Text("按应用") },
                selected = currentStyle == SummaryStyle.APP_BASED
            )
        }
    }
}

@Composable
private fun ThemePreference(
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "深色主题",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Switch(
            checked = isDarkTheme,
            onCheckedChange = onThemeChange
        )
    }
}

@Composable
private fun DataRetentionPreference(
    currentDays: Int,
    onDaysChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "数据保留期限",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Text(
            text = "自动删除超过指定天数的通知数据",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(7, 30, 90, 365).forEach { days ->
                FilterChip(
                    onClick = { onDaysChange(days) },
                    label = { Text("${days}天") },
                    selected = currentDays == days
                )
            }
        }
    }
}

@Composable
private fun DataInfoCard(
    notificationCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = "存储的通知数量",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "$notificationCount 条通知",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DataActionButtons(
    onExportClick: () -> Unit,
    onClearClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = onExportClick,
            enabled = !isLoading,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("导出数据")
        }
        
        OutlinedButton(
            onClick = onClearClick,
            enabled = !isLoading,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("清除数据")
        }
    }
}

@Composable
private fun AppInfoCard(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        InfoRow(label = "应用名称", value = "知汇 (NotiMind)")
        InfoRow(label = "版本", value = "1.0.0")
        InfoRow(label = "开发者", value = "Andy Chen")
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun DataClearDialog(
    onDismiss: () -> Unit,
    onClearAll: () -> Unit,
    onClearOld: () -> Unit,
    retentionDays: Int,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("清除数据") },
        text = {
            Text("选择要清除的数据范围：")
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onClearOld) {
                    Text("清除${retentionDays}天前")
                }
                TextButton(
                    onClick = onClearAll,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("清除全部")
                }
            }
        },
        modifier = modifier
    )
}

@Composable
private fun ExportDialog(
    onDismiss: () -> Unit,
    onExportJson: () -> Unit,
    onExportCsv: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("导出数据") },
        text = {
            Text("选择导出格式：")
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onExportJson) {
                    Text("JSON")
                }
                TextButton(onClick = onExportCsv) {
                    Text("CSV")
                }
            }
        },
        modifier = modifier
    )
}

@Preview
@Composable
private fun SettingsScreenPreview() {
    MaterialTheme {
        Surface {
            // Preview would show mock settings screen
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("设置屏幕预览")
            }
        }
    }
}