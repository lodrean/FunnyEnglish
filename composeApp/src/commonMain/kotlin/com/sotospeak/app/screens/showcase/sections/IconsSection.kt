package com.sotospeak.app.screens.showcase.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sotospeak.app.screens.showcase.DemoCard
import com.sotospeak.app.screens.showcase.ShowcaseScaffold
import com.sotospeak.app.screens.showcase.ShowcaseSection
import com.sotospeak.designsystem.icons.SpeakingIcons
import com.sotospeak.designsystem.theme.LocalSpeakingColors

/**
 * Демо «Иконки»: грид кастомных ImageVector из SpeakingIcons
 * (на WASM-canvas рендерятся корректно, в отличие от material-иконок — грабля №75).
 */
@Composable
fun IconsSection(onBack: () -> Unit) {
    val speaking = LocalSpeakingColors.current

    ShowcaseScaffold(section = ShowcaseSection.Icons, onBack = onBack) {
        DemoCard(title = "SpeakingIcons (24dp)") {
            val icons: List<Pair<String, ImageVector>> = listOf(
                "Mic" to SpeakingIcons.Mic,
                "MicOff" to SpeakingIcons.MicOff,
                "Play" to SpeakingIcons.Play,
                "Pause" to SpeakingIcons.Pause,
                "Stop" to SpeakingIcons.Stop,
                "Delete" to SpeakingIcons.Delete,
                "Refresh" to SpeakingIcons.Refresh,
                "ClosedCaptions" to SpeakingIcons.ClosedCaptions,
                "Lock" to SpeakingIcons.Lock,
                "Upload" to SpeakingIcons.Upload,
                "CheckCircle" to SpeakingIcons.CheckCircle,
                "Waveform" to SpeakingIcons.Waveform,
                "Clock" to SpeakingIcons.Clock,
                "ChevronRight" to SpeakingIcons.ChevronRight,
                "ChevronLeft" to SpeakingIcons.ChevronLeft,
                "Gear" to SpeakingIcons.Gear,
                "Bell" to SpeakingIcons.Bell,
                "Volume" to SpeakingIcons.Volume,
                "Vibrate" to SpeakingIcons.Vibrate,
                "Globe" to SpeakingIcons.Globe,
                "Moon" to SpeakingIcons.Moon,
                "Shield" to SpeakingIcons.Shield,
                "Home" to SpeakingIcons.Home,
                "Send" to SpeakingIcons.Send,
                "User" to SpeakingIcons.User,
                "Fullscreen" to SpeakingIcons.Fullscreen,
                "FullscreenExit" to SpeakingIcons.FullscreenExit
            )
            icons.chunked(4).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { (label, icon) ->
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = speaking.text,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = speaking.textMuted
                            )
                        }
                    }
                    repeat(4 - row.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
