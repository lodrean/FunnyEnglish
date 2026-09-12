package com.sotospeak.app.screens.showcase.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sotospeak.app.navigation.BadgedNavIcon
import com.sotospeak.app.screens.ContinuePathCard
import com.sotospeak.app.screens.ProgressRing
import com.sotospeak.app.screens.SettingsEntryRow
import com.sotospeak.app.screens.SettingsChevronRow
import com.sotospeak.app.screens.SettingsToggleRow
import com.sotospeak.app.screens.TrailAxis
import com.sotospeak.app.screens.TrailNodeState
import com.sotospeak.app.screens.showcase.DemoCard
import com.sotospeak.app.screens.showcase.ShowcaseScaffold
import com.sotospeak.app.screens.showcase.ShowcaseSection
import com.sotospeak.app.viewmodel.ContinueTopic
import com.sotospeak.designsystem.icons.SpeakingIcons
import com.sotospeak.designsystem.theme.LocalSpeakingColors

/**
 * Демо «Навигация» (редизайн V1 «Путь спикера», DS v3.2.0 B10/B11/B13):
 * нав-бейдж «Отправки», continue-карточка и trail библиотеки, setrow настроек.
 */
@Composable
fun NavigationSection(onBack: () -> Unit) {
    val speaking = LocalSpeakingColors.current

    ShowcaseScaffold(section = ShowcaseSection.Navigation, onBack = onBack) {
        DemoCard(title = "Нав-бейдж «Отправки» (B10)") {
            // Бейдж тёмным текстом на statusNew: белый на #FB8C00 запрещён DS §6 (WCAG)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    BadgedNavIcon(icon = SpeakingIcons.Send, label = "Отправки", badgeCount = 0)
                    Text("0 · скрыт", style = MaterialTheme.typography.labelSmall, color = speaking.textMuted)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    BadgedNavIcon(icon = SpeakingIcons.Send, label = "Отправки", badgeCount = 1)
                    Text("1", style = MaterialTheme.typography.labelSmall, color = speaking.textMuted)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    BadgedNavIcon(icon = SpeakingIcons.Send, label = "Отправки", badgeCount = 12)
                    Text("12 → «9+»", style = MaterialTheme.typography.labelSmall, color = speaking.textMuted)
                }
            }
        }

        DemoCard(title = "ContinuePathCard «Продолжить путь» (B11)") {
            ContinuePathCard(
                topic = ContinueTopic(
                    topicId = "demo",
                    libraryId = "demo-lib",
                    libraryTitle = "Знакомство",
                    topicTitle = "My best friend",
                    attemptsDone = 1
                ),
                onClick = {}
            )
        }

        DemoCard(title = "Мини-кольцо прогресса темы") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                listOf(0f, 0.4f, 1f).forEach { progress ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ProgressRing(
                            progress = progress,
                            size = 40.dp,
                            strokeWidth = 5.dp,
                            color = speaking.primary,
                            track = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = speaking.textMuted
                        )
                    }
                }
            }
        }

        DemoCard(title = "Trail «пути» тем (узлы)") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TrailAxis(nodeState = TrailNodeState.DONE, isFirst = true, isLast = false)
                TrailAxis(nodeState = TrailNodeState.CURRENT, isFirst = false, isLast = false)
                TrailAxis(nodeState = TrailNodeState.FRESH, isFirst = false, isLast = true)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("пройдено · в процессе · новая", style = MaterialTheme.typography.bodySmall)
                    Text(
                        "ось — outlineVariant, узлы — success/primary/statusNew",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = speaking.textMuted
                    )
                }
            }
        }

        DemoCard(title = "Setrow настроек (B13)") {
            Column(modifier = Modifier.fillMaxWidth()) {
                var notifications by remember { mutableStateOf(true) }
                var sound by remember { mutableStateOf(false) }
                SettingsToggleRow(
                    icon = SpeakingIcons.Bell,
                    title = "Уведомления",
                    subtitle = "Новая оценка, напоминания",
                    checked = notifications,
                    onCheckedChange = { notifications = it },
                    testTag = "showcase_setrow_toggle"
                )
                SettingsChevronRow(
                    icon = SpeakingIcons.Globe,
                    title = "Язык интерфейса",
                    subtitle = "Русский",
                    onClick = {},
                    testTag = "showcase_setrow_chevron"
                )
            }
        }

        DemoCard(title = "Вход в настройки (профиль)") {
            SettingsEntryRow(onClick = {}, modifier = Modifier.fillMaxWidth())
        }
    }
}
