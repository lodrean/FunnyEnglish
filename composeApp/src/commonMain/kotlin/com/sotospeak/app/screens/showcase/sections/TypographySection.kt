package com.sotospeak.app.screens.showcase.sections

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.sotospeak.app.screens.showcase.DemoCard
import com.sotospeak.app.screens.showcase.ShowcaseScaffold
import com.sotospeak.app.screens.showcase.ShowcaseSection
import com.sotospeak.designsystem.theme.LocalSpeakingColors
import com.sotospeak.designsystem.theme.SpeakingTextStyles
import com.sotospeak.designsystem.tokens.BodyLarge
import com.sotospeak.designsystem.tokens.BodyMedium
import com.sotospeak.designsystem.tokens.BodySmall
import com.sotospeak.designsystem.tokens.DisplayLarge
import com.sotospeak.designsystem.tokens.DisplayMedium
import com.sotospeak.designsystem.tokens.DisplaySmall
import com.sotospeak.designsystem.tokens.HeadlineLarge
import com.sotospeak.designsystem.tokens.HeadlineMedium
import com.sotospeak.designsystem.tokens.HeadlineSmall
import com.sotospeak.designsystem.tokens.LabelLarge
import com.sotospeak.designsystem.tokens.LabelMedium
import com.sotospeak.designsystem.tokens.LabelSmall
import com.sotospeak.designsystem.tokens.TitleLarge
import com.sotospeak.designsystem.tokens.TitleMedium
import com.sotospeak.designsystem.tokens.TitleSmall

/**
 * Демо «Типографика»: шкала DS-2.0 (Nunito, Major Third) из FunnyTypography
 * + специальные стили SpeakingTextStyles (вопрос / таймер tnum / субтитры).
 */
@Composable
fun TypographySection(onBack: () -> Unit) {
    val speaking = LocalSpeakingColors.current

    ShowcaseScaffold(section = ShowcaseSection.Typography, onBack = onBack) {
        DemoCard(title = "Шкала DS-2.0 (FunnyTypography)") {
            val styles: List<Pair<String, TextStyle>> = listOf(
                "DisplayLarge · 57sp Bold" to DisplayLarge,
                "DisplayMedium · 45sp Bold" to DisplayMedium,
                "DisplaySmall · 36sp Bold" to DisplaySmall,
                "HeadlineLarge · 32sp SemiBold" to HeadlineLarge,
                "HeadlineMedium · 28sp SemiBold" to HeadlineMedium,
                "HeadlineSmall · 24sp SemiBold" to HeadlineSmall,
                "TitleLarge · 22sp Medium" to TitleLarge,
                "TitleMedium · 18sp Medium" to TitleMedium,
                "TitleSmall · 14sp Medium" to TitleSmall,
                "BodyLarge · 16sp / 24sp" to BodyLarge,
                "BodyMedium · 14sp / 20sp" to BodyMedium,
                "BodySmall · 12sp / 16sp" to BodySmall,
                "LabelLarge · 14sp Medium" to LabelLarge,
                "LabelMedium · 12sp Medium" to LabelMedium,
                "LabelSmall · 11sp Medium" to LabelSmall
            )
            styles.forEach { (label, style) ->
                ColumnItem(label) {
                    Text(
                        text = "So to speak — Aa Яя",
                        style = style,
                        color = speaking.text
                    )
                }
            }
        }
        DemoCard(title = "SpeakingTextStyles") {
            ColumnItem("QuestionText · 25sp SemiBold · вопросы") {
                Text(
                    text = "Do you like travelling?",
                    style = SpeakingTextStyles.QuestionText,
                    color = speaking.text
                )
            }
            ColumnItem("TimerDisplay · 64sp mono tnum") {
                Text(
                    text = "0:42",
                    style = SpeakingTextStyles.TimerDisplay,
                    color = speaking.text
                )
            }
            ColumnItem("SubtitleText · 17sp · субтитры") {
                Text(
                    text = "At the airport · 5 вопросов",
                    style = SpeakingTextStyles.SubtitleText,
                    color = speaking.text
                )
            }
        }
    }
}

@Composable
private fun ColumnItem(label: String, content: @Composable () -> Unit) {
    val speaking = LocalSpeakingColors.current
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = speaking.textMuted
    )
    content()
}
