package com.sotospeak.app.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sotospeak.app.viewmodel.AppLanguage
import com.sotospeak.app.viewmodel.AppThemeMode
import com.sotospeak.app.viewmodel.SettingsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsState,
    onBack: () -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onToggleSound: (Boolean) -> Unit,
    onToggleHaptics: (Boolean) -> Unit,
    onToggleAutoPlay: (Boolean) -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    onThemeSelected: (AppThemeMode) -> Unit,
    onLogout: () -> Unit
) {
    var languageExpanded by remember { mutableStateOf(false) }
    var themeExpanded by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Настройки",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SettingsSection(title = "Общие") {
                    ThemeRow(
                        themeMode = state.themeMode,
                        expanded = themeExpanded,
                        onExpandedChange = { themeExpanded = it },
                        onThemeSelected = onThemeSelected
                    )
                    HorizontalDivider()
                    LanguageRow(
                        language = state.language,
                        expanded = languageExpanded,
                        onExpandedChange = { languageExpanded = it },
                        onLanguageSelected = onLanguageSelected
                    )
                }
            }

            item {
                SettingsSection(title = "Уведомления") {
                    SettingsToggleRow(
                        title = "Push-уведомления",
                        subtitle = "Напоминания о тренировках",
                        checked = state.notificationsEnabled,
                        onCheckedChange = onToggleNotifications,
                        icon = Icons.Default.Notifications
                    )
                }
            }

            item {
                SettingsSection(title = "Звук и вибрация") {
                    SettingsToggleRow(
                        title = "Звуковые эффекты",
                        subtitle = "Звук в упражнениях и интерфейсе",
                        checked = state.soundEnabled,
                        onCheckedChange = onToggleSound,
                        icon = Icons.Default.MusicNote
                    )
                    HorizontalDivider()
                    SettingsToggleRow(
                        title = "Вибрация",
                        subtitle = "Тактильная отдача при ответах",
                        checked = state.hapticsEnabled,
                        onCheckedChange = onToggleHaptics,
                        icon = Icons.Default.TouchApp
                    )
                    HorizontalDivider()
                    SettingsToggleRow(
                        title = "Автопроигрывание аудио",
                        subtitle = "Воспроизводить звук без нажатия",
                        checked = state.autoPlayAudio,
                        onCheckedChange = onToggleAutoPlay,
                        icon = Icons.Default.PlayArrow
                    )
                }
            }

            item {
                SettingsSection(title = "О приложении") {
                    ListItem(
                        headlineContent = {
                            Text(text = "Версия")
                        },
                        supportingContent = {
                            Text(text = "1.0.0")
                        }
                    )
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = onLogout,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Logout,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Выйти")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    ListItem(
        headlineContent = { Text(text = title) },
        supportingContent = { Text(text = subtitle) },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    )
}

@Composable
private fun LanguageRow(
    language: AppLanguage,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit
) {
    val label = when (language) {
        AppLanguage.RU -> "Русский"
        AppLanguage.EN -> "English"
    }

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandedChange(true) }
        ) {
            ListItem(
                headlineContent = { Text(text = "Язык") },
                supportingContent = { Text(text = "Язык интерфейса") },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null
                    )
                },
                trailingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = label)
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null
                        )
                    }
                }
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) }
            ) {
                DropdownMenuItem(
                    text = { Text("Русский") },
                    onClick = {
                        onLanguageSelected(AppLanguage.RU)
                        onExpandedChange(false)
                    }
                )
                DropdownMenuItem(
                    text = { Text("English") },
                    onClick = {
                        onLanguageSelected(AppLanguage.EN)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}

@Composable
private fun ThemeRow(
    themeMode: AppThemeMode,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onThemeSelected: (AppThemeMode) -> Unit
) {
    val label = when (themeMode) {
        AppThemeMode.SYSTEM -> "Системная"
        AppThemeMode.LIGHT -> "Светлая"
        AppThemeMode.DARK -> "Темная"
    }

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandedChange(true) }
        ) {
            ListItem(
                headlineContent = { Text(text = "Тема") },
                supportingContent = { Text(text = "Светлая, тёмная или системная") },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Brightness4,
                        contentDescription = null
                    )
                },
                trailingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = label)
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null
                        )
                    }
                }
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) }
            ) {
                DropdownMenuItem(
                    text = { Text("Системная") },
                    onClick = {
                        onThemeSelected(AppThemeMode.SYSTEM)
                        onExpandedChange(false)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Светлая") },
                    onClick = {
                        onThemeSelected(AppThemeMode.LIGHT)
                        onExpandedChange(false)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Темная") },
                    onClick = {
                        onThemeSelected(AppThemeMode.DARK)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}
