package com.sotospeak.app.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sotospeak.app.accessibility.AccessibilityUtils.heading
import com.sotospeak.designsystem.icons.SpeakingIcons
import com.sotospeak.designsystem.theme.LocalSpeakingColors

/**
 * Шапка экрана (редизайн «Путь спикера» V1, DS v3.2.0 B8): заголовок +
 * подзаголовок-цепочка («Travel & Holidays · At the airport · 5 вопросов») и
 * опциональная круглая back-кнопка 40dp (surface-фон, рамка outlineVariant,
 * иконка ChevronLeft). На флоу-экранах (Topics/Video/Questions/Training/
 * Practice/Settings) кнопка дублирует семантику системного «назад»
 * (PlatformBackHandler остаётся без изменений); на вкладках — без кнопки.
 */
@Composable
fun SpeakingAppBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val speaking = LocalSpeakingColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            // edge-to-edge: статус-бар прозрачный. На экранах с собственным Scaffold
            // (Video) аппбар стоит у y=0 — без этого пада заголовок уезжает под статус-бар.
            // На экранах внутри AppScaffold верхний инсет уже поглощён consumeWindowInsets —
            // здесь statusBarsPadding() даёт 0, двойного отступа нет.
            .statusBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
            .testTag("app_bar")
    ) {
        if (onBack != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AppBarBackButton(onBack = onBack)
                AppBarTexts(title = title, subtitle = subtitle)
            }
        } else {
            AppBarTexts(title = title, subtitle = subtitle)
        }
    }
}

@Composable
private fun AppBarTexts(title: String, subtitle: String?) {
    val speaking = LocalSpeakingColors.current
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = speaking.text,
            modifier = Modifier.heading()
        )
        if (!subtitle.isNullOrEmpty()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = speaking.textMuted,
                modifier = Modifier.testTag("app_bar_subtitle")
            )
        }
    }
}

/** Круглая кнопка «назад» 40dp по макету (.backbtn) — v3.2.0 B8. */
@Composable
fun AppBarBackButton(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = SpeakingIcons.ChevronLeft,
    contentDescription: String = "Назад",
    testTag: String = "app_bar_back"
) {
    val speaking = LocalSpeakingColors.current
    Surface(
        onClick = onBack,
        shape = RoundedCornerShape(percent = 50),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, speaking.outline),
        modifier = modifier.size(40.dp).testTag(testTag)
    ) {
        androidx.compose.material3.Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = speaking.text,
            modifier = Modifier.padding(8.dp)
        )
    }
}

/** Плюрализация: «1 вопрос», «3 вопроса», «5 вопросов». */
fun questionsCountText(count: Int): String {
    val mod100 = count % 100
    val mod10 = count % 10
    val word = when {
        mod100 in 11..14 -> "вопросов"
        mod10 == 1 -> "вопрос"
        mod10 in 2..4 -> "вопроса"
        else -> "вопросов"
    }
    return "$count $word"
}
