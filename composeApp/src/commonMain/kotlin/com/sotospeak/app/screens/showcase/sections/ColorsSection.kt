package com.sotospeak.app.screens.showcase.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sotospeak.app.screens.showcase.DemoCard
import com.sotospeak.app.screens.showcase.ShowcaseScaffold
import com.sotospeak.app.screens.showcase.ShowcaseSection
import com.sotospeak.app.screens.showcase.SwatchGrid
import com.sotospeak.designsystem.theme.LocalSpeakingColors

/**
 * Демо «Цвета»: M3-схема FunnyTheme + speaking-токены (record/timer/status)
 * и пары «фон + корректный цвет текста» (WCAG-правила из tokens.json).
 */
@Composable
fun ColorsSection(onBack: () -> Unit) {
    val speaking = LocalSpeakingColors.current
    val m3 = MaterialTheme.colorScheme

    ShowcaseScaffold(section = ShowcaseSection.Colors, onBack = onBack) {
        DemoCard(title = "Material 3 (FunnyTheme)") {
            SwatchGrid(
                listOf(
                    "primary" to m3.primary,
                    "onPrimary" to m3.onPrimary,
                    "primaryContainer" to m3.primaryContainer,
                    "secondary" to m3.secondary,
                    "onSecondary" to m3.onSecondary,
                    "secondaryContainer" to m3.secondaryContainer,
                    "background" to m3.background,
                    "surface" to m3.surface,
                    "surfaceVariant" to m3.surfaceVariant,
                    "outline" to m3.outline,
                    "outlineVariant" to m3.outlineVariant,
                    "error" to m3.error
                )
            )
        }
        DemoCard(title = "Speaking-токены (tokens v1.3.2)") {
            SwatchGrid(
                listOf(
                    "primary" to speaking.primary,
                    "primaryStrong" to speaking.primaryStrong,
                    "secondary" to speaking.secondary,
                    "background" to speaking.background,
                    "surface" to speaking.surface,
                    "surfaceVariant" to speaking.surfaceVariant,
                    "text" to speaking.text,
                    "textMuted" to speaking.textMuted,
                    "outline" to speaking.outline,
                    "record" to speaking.record,
                    "recordActive" to speaking.recordActive,
                    "recordShadow" to speaking.recordShadow,
                    "recordContainer" to speaking.recordContainer,
                    "onRecordContainer" to speaking.onRecordContainer,
                    "waveformPlayback" to speaking.waveformPlayback,
                    "timerLevel80" to speaking.timerLevel80,
                    "timerLevel50" to speaking.timerLevel50,
                    "timerLevel30" to speaking.timerLevel30,
                    "statusNew" to speaking.statusNew,
                    "statusNewContainer" to speaking.statusNewContainer,
                    "statusReviewed" to speaking.statusReviewed,
                    "statusReviewedContainer" to speaking.statusReviewedContainer,
                    "success" to speaking.success,
                    "error" to speaking.error,
                    "errorText" to speaking.errorText,
                    "videoPlayerBg" to speaking.videoPlayerBg,
                    "videoAccent" to speaking.videoAccent,
                    "gradientBlue" to speaking.gradientBlue,
                    "gradientBlueDeep" to speaking.gradientBlueDeep,
                    "gradientGreen" to speaking.gradientGreen,
                    "gradientGreenDeep" to speaking.gradientGreenDeep,
                    "gradientIndigo" to speaking.gradientIndigo,
                    "gradientIndigoDeep" to speaking.gradientIndigoDeep,
                    "gradientOrange" to speaking.gradientOrange,
                    "gradientOrangeDeep" to speaking.gradientOrangeDeep
                )
            )
        }
        DemoCard(title = "Текст на бренд-подложках (WCAG)") {
            OnColorSample(
                label = "onRecord на record",
                background = speaking.record,
                contentColor = speaking.onRecord
            )
            OnColorSample(
                label = "onRecordContainer на recordContainer",
                background = speaking.recordContainer,
                contentColor = speaking.onRecordContainer
            )
            OnColorSample(
                label = "statusNew на statusNewContainer",
                background = speaking.statusNewContainer,
                contentColor = speaking.statusNew
            )
            OnColorSample(
                label = "statusReviewed на statusReviewedContainer",
                background = speaking.statusReviewedContainer,
                contentColor = speaking.statusReviewed
            )
        }
    }
}

@Composable
private fun OnColorSample(label: String, background: Color, contentColor: Color) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(background)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = contentColor
        )
    }
}
