package com.funnyenglish.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.funnyenglish.designsystem.components.cards.FunnyCard
import com.funnyenglish.designsystem.tokens.SpaceMd
import com.funnyenglish.designsystem.tokens.SpaceSm

/**
 * FunnyEnglish Profile Screen
 * 
 * User settings and preferences:
 * - Profile info
 * - Theme settings
 * - Accessibility options
 * - Notifications
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit = {}
) {
    var darkMode by remember { mutableStateOf(false) }
    var reduceMotion by remember { mutableStateOf(false) }
    var notifications by remember { mutableStateOf(true) }
    var highContrast by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(SpaceMd),
            verticalArrangement = Arrangement.spacedBy(SpaceMd)
        ) {
            // Profile Card
            item {
                FunnyCard {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(SpaceMd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.padding(SpaceMd),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = "Ученик",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Уровень 5 • Знаток",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Appearance Section
            item {
                Text(
                    text = "Внешний вид",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = SpaceMd)
                )
                Spacer(modifier = Modifier.height(SpaceSm))
                
                FunnyCard {
                    SettingRow(
                        icon = Icons.Default.DarkMode,
                        title = "Тёмная тема",
                        checked = darkMode,
                        onCheckedChange = { darkMode = it }
                    )
                }
            }
            
            // Accessibility Section
            item {
                Text(
                    text = "Доступность",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = SpaceMd)
                )
                Spacer(modifier = Modifier.height(SpaceSm))
                
                FunnyCard {
                    Column {
                        SettingRow(
                            icon = Icons.Default.Accessibility,
                            title = "Уменьшить анимацию",
                            subtitle = "Для пользователей с СДВГ",
                            checked = reduceMotion,
                            onCheckedChange = { reduceMotion = it }
                        )
                        
                        SettingRow(
                            icon = Icons.Default.Accessibility,
                            title = "Высокий контраст",
                            checked = highContrast,
                            onCheckedChange = { highContrast = it }
                        )
                    }
                }
            }
            
            // Notifications Section
            item {
                Text(
                    text = "Уведомления",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = SpaceMd)
                )
                Spacer(modifier = Modifier.height(SpaceSm))
                
                FunnyCard {
                    SettingRow(
                        icon = Icons.Default.Notifications,
                        title = "Push-уведомления",
                        subtitle = "Напоминания о занятиях",
                        checked = notifications,
                        onCheckedChange = { notifications = it }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(SpaceMd),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpaceMd)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
