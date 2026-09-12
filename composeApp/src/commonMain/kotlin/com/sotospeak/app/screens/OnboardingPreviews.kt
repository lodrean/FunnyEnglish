package com.sotospeak.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sotospeak.app.components.RecordingWaveform
import com.sotospeak.designsystem.icons.SpeakingIcons
import com.sotospeak.designsystem.theme.LocalSpeakingColors
import com.sotospeak.designsystem.theme.SpeakingShapes

/**
 * Живые превью онбординга (редизайн V2, DS v3.3.0 B15): слайды показывают продукт
 * через мини-версии настоящих компонентов (мини-плеер / мини-Training / мини-оценка),
 * собранные ТОЛЬКО из токенов v1.3.1 (маппинг §1.1). Всё содержимое — декоративные
 * демо-данные (не локализуемые строки UI), интерактивных элементов нет.
 */

/** Слайд 1 «Смотри видео»: кадр плеера + строка транскрипта с mark-подсветкой. */
@Composable
internal fun VideoPreview(modifier: Modifier = Modifier) {
    val speaking = LocalSpeakingColors.current
    Column(
        modifier = modifier
            .clip(SpeakingShapes.CardLarge)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest),
    ) {
        // Кадр «видео» — чёрный, как реальный SurfaceView (не токен темы)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .background(Color.Black)
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color.White.copy(alpha = 0.16f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Text(
                    text = "CC",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(40.dp)
                    .background(Color.White, RoundedCornerShape(percent = 50)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = SpeakingIcons.Play,
                    contentDescription = null,
                    tint = speaking.primaryStrong,
                    modifier = Modifier
                        .padding(start = 3.dp)
                        .size(20.dp)
                )
            }
            LinearProgressIndicator(
                progress = { 0.35f },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(3.dp),
                color = speaking.record,
                trackColor = Color.White.copy(alpha = 0.25f)
            )
        }
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            // Транскрипт с mark-подсветкой ключевой фразы — как на VideoScreen
            Row {
                Text(
                    text = "Hello!",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = speaking.onRecordContainer,
                    modifier = Modifier
                        .background(speaking.recordContainer, RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Nice to meet you.",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = speaking.text
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "My name is Alex. What is your name?",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = speaking.textMuted
            )
        }
    }
}

/** Слайд 2 «Тренируйся вслух»: чип уровня, таймер-кольцо, rec-кнопка, waveform. */
@Composable
internal fun TrainingPreview(modifier: Modifier = Modifier) {
    val speaking = LocalSpeakingColors.current
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Попытка 1 из 3",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = speaking.timerLevel80,
                modifier = Modifier
                    .background(speaking.timerLevel80.copy(alpha = 0.14f), RoundedCornerShape(999.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
        Box(contentAlignment = Alignment.Center) {
            ProgressRing(
                progress = 0.72f,
                size = 96.dp,
                strokeWidth = 8.dp,
                color = speaking.timerLevel80,
                track = MaterialTheme.colorScheme.surfaceContainerHigh
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "01:20",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    color = speaking.text
                )
                Text(
                    text = "говори на все вопросы",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = speaking.textMuted
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RecordingWaveform(modifier = Modifier.width(90.dp), barCount = 12)
            // Декоративная копия rec-кнопки B1 (squircle record + «жёсткая» тень):
            // в превью нет действия, SpeakingRecordButton не используем (a11y)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(speaking.record),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = SpeakingIcons.Mic,
                    contentDescription = null,
                    tint = speaking.onRecordContainer,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

/** Слайд 3 «Отправь учителю»: мини-карточка оценки — score, 4 бара, зелёная плашка. */
@Composable
internal fun GradePreview(modifier: Modifier = Modifier) {
    val speaking = LocalSpeakingColors.current
    Column(
        modifier = modifier
            .clip(SpeakingShapes.CardLarge)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "8.5",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = speaking.primaryStrong
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "из 10",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = speaking.textMuted,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "ОЦЕНКА ПОЛУЧЕНА",
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                color = speaking.statusReviewed,
                modifier = Modifier
                    .background(speaking.statusReviewedContainer, RoundedCornerShape(999.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
        gradeBar("Грамматика", 8, speaking)
        gradeBar("Лексика", 9, speaking)
        gradeBar("Произношение", 8, speaking)
        gradeBar("Беглость", 9, speaking)
    }
}

@Composable
private fun gradeBar(label: String, score: Int, speaking: com.sotospeak.designsystem.theme.SpeakingColors) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = speaking.textMuted,
            modifier = Modifier.width(84.dp)
        )
        LinearProgressIndicator(
            progress = { score / 10f },
            modifier = Modifier
                .weight(1f)
                .height(5.dp)
                .clip(RoundedCornerShape(999.dp)),
            color = speaking.primary,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
        Text(
            text = score.toString(),
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = speaking.text
        )
    }
}
