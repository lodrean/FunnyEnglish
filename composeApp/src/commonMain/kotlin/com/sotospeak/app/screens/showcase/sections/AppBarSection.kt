package com.sotospeak.app.screens.showcase.sections

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.sotospeak.app.components.SpeakingAppBar
import com.sotospeak.app.components.questionsCountText
import com.sotospeak.app.screens.showcase.DemoCard
import com.sotospeak.app.screens.showcase.ShowcaseScaffold
import com.sotospeak.app.screens.showcase.ShowcaseSection
import com.sotospeak.designsystem.theme.LocalSpeakingColors

/**
 * Демо «Аппбар»: SpeakingAppBar с заголовком и breadcrumb-подзаголовком
 * (мокап .appbar .sub), как на speaking-экранах тренажёра.
 */
@Composable
fun AppBarSection(onBack: () -> Unit) {
    val speaking = LocalSpeakingColors.current

    ShowcaseScaffold(section = ShowcaseSection.AppBar, onBack = onBack) {
        DemoCard(title = "SpeakingAppBar · title + subtitle") {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = speaking.background
            ) {
                SpeakingAppBar(
                    title = "Travel & Holidays",
                    subtitle = "At the airport · ${questionsCountText(5)}"
                )
            }
        }
        DemoCard(title = "SpeakingAppBar · только title") {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = speaking.background
            ) {
                SpeakingAppBar(title = "Библиотека тем")
            }
        }
        DemoCard(title = "SpeakingAppBar · back-кнопка (V1)") {
            // Редизайн V1: круглая back-кнопка 40dp на флоу-экранах
            Surface(
                shape = MaterialTheme.shapes.large,
                color = speaking.background
            ) {
                SpeakingAppBar(
                    title = "Путешествия",
                    subtitle = "8 топиков · выбери и начни говорить",
                    onBack = {}
                )
            }
        }
        DemoCard(title = "Плюрализация вопросов") {
            Text(
                text = listOf(1, 3, 5, 11, 21).joinToString(", ") { questionsCountText(it) },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
