package com.sotospeak.app.screens.showcase.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sotospeak.app.screens.showcase.DemoCard
import com.sotospeak.app.screens.showcase.ShowcaseScaffold
import com.sotospeak.app.screens.showcase.ShowcaseSection
import com.sotospeak.designsystem.tokens.ButtonHeightLarge
import com.sotospeak.designsystem.tokens.SpaceBase
import com.sotospeak.designsystem.tokens.SpaceLg
import com.sotospeak.designsystem.tokens.SpaceMd
import com.sotospeak.designsystem.tokens.SpaceSm
import com.sotospeak.designsystem.tokens.SpaceXl
import com.sotospeak.designsystem.tokens.SpaceXs
import com.sotospeak.designsystem.tokens.SpaceXxl
import com.sotospeak.designsystem.tokens.SpaceXxxl
import com.sotospeak.designsystem.tokens.TouchTargetMin
import com.sotospeak.designsystem.tokens.TouchTargetRecommended
import com.sotospeak.designsystem.theme.LocalSpeakingColors

/**
 * Демо «Отступы»: шкала 4dp-сетки FunnySpacing + touch-target'ы
 * и высоты кнопок (WCAG / детский моторный размер).
 */
@Composable
fun SpacingSection(onBack: () -> Unit) {
    val speaking = LocalSpeakingColors.current

    ShowcaseScaffold(section = ShowcaseSection.Spacing, onBack = onBack) {
        DemoCard(title = "Шкала 4dp (FunnySpacing)") {
            val tokens: List<Pair<String, androidx.compose.ui.unit.Dp>> = listOf(
                "SpaceXs · 4dp" to SpaceXs,
                "SpaceSm · 8dp" to SpaceSm,
                "SpaceMd · 16dp" to SpaceMd,
                "SpaceLg · 24dp" to SpaceLg,
                "SpaceXl · 32dp" to SpaceXl,
                "SpaceXxl · 48dp" to SpaceXxl,
                "SpaceXxxl · 64dp" to SpaceXxxl
            )
            tokens.forEach { (label, value) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = speaking.text,
                        modifier = Modifier.fillMaxWidth(0.4f)
                    )
                    Box(
                        modifier = Modifier
                            .size(width = value, height = 16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(speaking.primary)
                    )
                }
            }
        }
        DemoCard(title = "Touch targets и компоненты") {
            val sizes: List<Pair<String, androidx.compose.ui.unit.Dp>> = listOf(
                "TouchTargetMin · 48dp" to TouchTargetMin,
                "TouchTargetRecommended · 56dp" to TouchTargetRecommended,
                "ButtonHeightLarge · 56dp" to ButtonHeightLarge,
                "SpaceBase · 4dp" to SpaceBase
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                sizes.forEach { (label, value) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(value)
                                .clip(RoundedCornerShape(8.dp))
                                .background(speaking.secondaryContainer)
                        )
                        Text(
                            text = label.substringBefore(" ·"),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = speaking.textMuted
                        )
                    }
                }
            }
        }
    }
}
