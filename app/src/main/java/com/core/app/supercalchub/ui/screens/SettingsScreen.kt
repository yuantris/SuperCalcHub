package com.core.app.supercalchub.ui.screens

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.core.app.supercalchub.getEncryptedPrefs
import com.core.app.supercalchub.ui.navigation.NavigationUtils

/**
 * 设置界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    onThemeChanged: (String) -> Unit = {},
    onDarkModeChanged: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = remember { getEncryptedPrefs(context) }

    // 主题设置
    var selectedTheme by remember { mutableStateOf(prefs.getString("theme", "blue") ?: "blue") }
    var isDarkMode by remember { mutableStateOf(prefs.getBoolean("dark_mode", false)) }
    var isPrivacyEnabled by remember { mutableStateOf(prefs.getBoolean("privacy_enabled", false)) }
    var privacyPassword by remember { mutableStateOf("") }
    var showPasswordDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = { NavigationUtils.safePopBack(navController) }) {
                        Text("←")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // 主题设置
            SettingsSection(title = "主题设置") {
                // 颜色主题选择
                Text(
                    text = "主题颜色",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                val themes = listOf(
                    ThemeOption("blue", "蓝色", Color(0xFF1976D2)),
                    ThemeOption("green", "绿色", Color(0xFF388E3C)),
                    ThemeOption("red", "红色", Color(0xFFD32F2F)),
                    ThemeOption("purple", "紫色", Color(0xFF7B1FA2)),
                    ThemeOption("orange", "橙色", Color(0xFFF57C00)),
                    ThemeOption("teal", "青色", Color(0xFF00897B))
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(themes) { theme ->
                        ThemeColorItem(
                            theme = theme,
                            isSelected = selectedTheme == theme.id,
                            onClick = {
                                selectedTheme = theme.id
                                prefs.edit().putString("theme", theme.id).apply()
                                onThemeChanged(theme.id)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 深色模式开关
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "深色模式",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "使用深色主题",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = {
                            isDarkMode = it
                            prefs.edit().putBoolean("dark_mode", it).apply()
                            onDarkModeChanged(it)
                        }
                    )
                }
            }

            // 隐私设置
            SettingsSection(title = "隐私设置") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "隐私加密",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "加密存储计算历史",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isPrivacyEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                showPasswordDialog = true
                            } else {
                                isPrivacyEnabled = false
                                prefs.edit().putBoolean("privacy_enabled", false).apply()
                            }
                        }
                    )
                }

                if (isPrivacyEnabled) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "已启用加密保护",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // 关于
            SettingsSection(title = "关于") {
                SettingsItem(
                    title = "版本",
                    subtitle = "1.0.0"
                )
                SettingsItem(
                    title = "开发者",
                    subtitle = "FFGreatKing"
                )
            }
        }

        // 密码设置对话框
        if (showPasswordDialog) {
            AlertDialog(
                onDismissRequest = { showPasswordDialog = false },
                title = { Text("设置隐私密码") },
                text = {
                    Column {
                        Text("请输入用于加密的密码：")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = privacyPassword,
                            onValueChange = { privacyPassword = it },
                            label = { Text("密码") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (privacyPassword.isNotEmpty()) {
                                isPrivacyEnabled = true
                                prefs.edit()
                                    .putBoolean("privacy_enabled", true)
                                    .putString("privacy_password", privacyPassword)
                                    .apply()
                                showPasswordDialog = false
                                privacyPassword = ""
                            }
                        }
                    ) {
                        Text("确认")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPasswordDialog = false }) {
                        Text("取消")
                    }
                }
            )
        }
    }
}

/**
 * 主题选项数据类
 */
data class ThemeOption(
    val id: String,
    val name: String,
    val color: Color
)

/**
 * 主题颜色选择项
 */
@Composable
fun ThemeColorItem(
    theme: ThemeOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(theme.color)
                .border(
                    width = if (isSelected) 3.dp else 0.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                    shape = CircleShape
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "已选择",
                    tint = Color.White
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = theme.name,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

/**
 * 设置分组
 */
@Preview
@Composable
fun SettingsSection(
    title: String = "设置",
    content: @Composable ColumnScope.() -> Unit = {}
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                content = content
            )
        }
    }
}

/**
 * 设置项
 */
@Composable
fun SettingsItem(
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}