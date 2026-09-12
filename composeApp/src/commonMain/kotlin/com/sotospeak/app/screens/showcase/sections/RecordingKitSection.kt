package com.sotospeak.app.screens.showcase.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sotospeak.app.components.CheckPopAppear
import com.sotospeak.app.components.PlaybackWaveform
import com.sotospeak.app.components.RecIndicator
import com.sotospeak.app.components.RecordingWaveform
import com.sotospeak.app.components.SpeakingRecordButton
import com.sotospeak.app.components.SpeakingTimerRing
import com.sotospeak.app.screens.showcase.DemoCard
import com.sotospeak.app.screens.showcase.ShowcaseScaffold
import com.sotospeak.app.screens.showcase.ShowcaseSection
import com.sotospeak.designsystem.icons.SpeakingIcons
import com.sotospeak.designsystem.theme.LocalSpeakingColors

/**
 * Демо «Запись (rec kit)»: таймер-кольцо, REC-индикатор, waveform'ы,
 * кнопка записи и checkPop — весь набор тренажёра из SpeakingRecording.kt.
 *
 * Таймер — статичный (42с из 80): значения 1..5 запускают вибро/звук финального
 * отсчёта — в шоукейсе они не нужны. Анимации пульса/waveform живут только здесь,
 * в UI-тестах секция не рендерится (грабля №49).
 */
@Composable
fun RecordingKitSection(onBack: () -> Unit) {
    val speaking = LocalSpeakingColors.current

    ShowcaseScaffold(section = ShowcaseSection.RecordingKit, onBack = onBack) {
        DemoCard(title = "SpeakingTimerRing · уровень 50с (#8A68D6)") {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                SpeakingTimerRing(
                    remainingSeconds = 42,
                    totalSeconds = 80,
                    arcColor = speaking.timerLevel50,
                    timeText = "0:42",
                    caption = "Говорите!"
                )
            }
        }
        DemoCard(title = "RecIndicator + RecordingWaveform") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RecIndicator()
                RecordingWaveform(modifier = Modifier.weight(1f))
            }
        }
        DemoCard(title = "PlaybackWaveform · 60% прослушано") {
            PlaybackWaveform(playedFraction = 0.6f, modifier = Modifier.fillMaxWidth())
        }
        DemoCard(title = "SpeakingRecordButton · idle / recording") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    SpeakingRecordButton(
                        isRecording = false,
                        enabled = true,
                        onClick = {},
                        testTag = "showcase_rec_idle"
                    )
                }
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    SpeakingRecordButton(
                        isRecording = true,
                        enabled = true,
                        onClick = {},
                        testTag = "showcase_rec_active"
                    )
                }
            }
        }
        DemoCard(title = "CheckPopAppear") {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CheckPopAppear {
                    Icon(
                        imageVector = SpeakingIcons.CheckCircle,
                        contentDescription = null,
                        tint = speaking.success,
                        modifier = Modifier.size(48.dp)
                    )
                }
                Text(
                    text = "EasingBounce 500ms (reduce motion — сразу)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = speaking.textMuted,
                    modifier = Modifier.height(16.dp)
                )
            }
        }
    }
}
