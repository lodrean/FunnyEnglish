package com.sotospeak.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sotospeak.app.components.SpeakingAppBar
import com.sotospeak.app.viewmodel.AppLanguage
import com.sotospeak.app.viewmodel.AppThemeMode
import com.sotospeak.app.viewmodel.SettingsState
import com.sotospeak.designsystem.icons.SpeakingIcons
import com.sotospeak.designsystem.theme.LocalSpeakingColors

/**
 * Экран настроек по V1-макету «Путь спикера» (DS v3.2.0 B13 setrow, bd d1e.5):
 * строки — иконка в квадратике primaryContainer r11 + заголовок/подзаголовок +
 * Switch/chevron; аппбар с back-кнопкой. Экран ранее не имел входа в UI —
 * вход добавлен из профиля (OpenSpec app-navigation).
 */
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
    val speaking = LocalSpeakingColors.current

    Scaffold(
        containerColor = speaking.background,
        topBar = {
            SpeakingAppBar(
                title = "Настройки",
                subtitle = "Профиль",
                onBack = onBack
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                SettingsToggleRow(
                    icon = SpeakingIcons.Bell,
                    title = "Уведомления",
                    subtitle = "Новая оценка, напоминания",
                    checked = state.notificationsEnabled,
                    onCheckedChange = onToggleNotifications,
                    testTag = "settings_notifications"
                )
            }
            item {
                SettingsToggleRow(
                    icon = SpeakingIcons.Volume,
                    title = "Звук",
                    subtitle = "Эффекты и подтверждения",
                    checked = state.soundEnabled,
                    onCheckedChange = onToggleSound,
                    testTag = "settings_sound"
                )
            }
            item {
                SettingsToggleRow(
                    icon = SpeakingIcons.Vibrate,
                    title = "Вибрация",
                    subtitle = "Отклик кнопки записи",
                    checked = state.hapticsEnabled,
                    onCheckedChange = onToggleHaptics,
                    testTag = "settings_haptics"
                )
            }
            item {
                SettingsToggleRow(
                    icon = SpeakingIcons.Play,
                    title = "Автопроигрывание аудио",
                    subtitle = "Воспроизводить звук без нажатия",
                    checked = state.autoPlayAudio,
                    onCheckedChange = onToggleAutoPlay,
                    testTag = "settings_autoplay"
                )
            }
            item {
                Box {
                    SettingsChevronRow(
                        icon = SpeakingIcons.Globe,
                        title = "Язык интерфейса",
                        subtitle = languageLabel(state.language),
                        onClick = { languageExpanded = true },
                        testTag = "settings_language"
                    )
                    DropdownMenu(
                        expanded = languageExpanded,
                        onDismissRequest = { languageExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Русский") },
                            onClick = {
                                onLanguageSelected(AppLanguage.RU)
                                languageExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("English") },
                            onClick = {
                                onLanguageSelected(AppLanguage.EN)
                                languageExpanded = false
                            }
                        )
                    }
                }
            }
            item {
                Box {
                    SettingsChevronRow(
                        icon = SpeakingIcons.Moon,
                        title = "Оформление",
                        subtitle = themeLabel(state.themeMode),
                        onClick = { themeExpanded = true },
                        testTag = "settings_theme"
                    )
                    DropdownMenu(
                        expanded = themeExpanded,
                        onDismissRequest = { themeExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Системная") },
                            onClick = {
                                onThemeSelected(AppThemeMode.SYSTEM)
                                themeExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Светлая") },
                            onClick = {
                                onThemeSelected(AppThemeMode.LIGHT)
                                themeExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Тёмная") },
                            onClick = {
                                onThemeSelected(AppThemeMode.DARK)
                                themeExpanded = false
                            }
                        )
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // V1 .logout: прозрачная кнопка с красной рамкой (1.5dp)
                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("settings_logout"),
                    shape = MaterialTheme.shapes.medium,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.error
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(text = "Выйти", fontWeight = FontWeight.ExtraBold)
                }
                Text(
                    text = "So to speak · v1.4",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = speaking.textMuted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .testTag("settings_version")
                )
            }
        }
    }
}

@Composable
private fun languageLabel(language: AppLanguage): String = when (language) {
    AppLanguage.RU -> "Русский"
    AppLanguage.EN -> "English"
}

@Composable
private fun themeLabel(themeMode: AppThemeMode): String = when (themeMode) {
    AppThemeMode.SYSTEM -> "Системная"
    AppThemeMode.LIGHT -> "Светлая"
    AppThemeMode.DARK -> "Тёмная"
}

/** Строка-переключатель (DS v3.2.0 B13 setrow). */
@Composable
internal fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    val speaking = LocalSpeakingColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 4.dp, vertical = 8.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SettingsRowIcon(icon = icon)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = speaking.text
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = speaking.textMuted
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = speaking.primaryStrong,
                uncheckedTrackColor = speaking.textMuted
            ),
            modifier = Modifier.testTag("${testTag}_switch")
        )
    }
}

/** Строка-переход (DS v3.2.0 B13 setrow): иконка + заголовок/подзаголовок + chevron. */
@Composable
internal fun SettingsChevronRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    val speaking = LocalSpeakingColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 8.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SettingsRowIcon(icon = icon)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = speaking.text
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = speaking.textMuted
            )
        }
        Icon(
            imageVector = SpeakingIcons.ChevronRight,
            contentDescription = null,
            tint = speaking.textMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}

/** Иконка строки в квадратике 34dp primaryContainer r11 (V1 .sico). */
@Composable
private fun SettingsRowIcon(icon: ImageVector) {
    val speaking = LocalSpeakingColors.current
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(speaking.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = speaking.onPrimaryContainer,
            modifier = Modifier.size(18.dp)
        )
    }
}
