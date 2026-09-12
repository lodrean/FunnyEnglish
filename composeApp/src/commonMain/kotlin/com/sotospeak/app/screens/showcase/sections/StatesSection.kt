package com.sotospeak.app.screens.showcase.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sotospeak.app.components.EmptyState
import com.sotospeak.app.components.ErrorMessage
import com.sotospeak.app.components.LockedFeature
import com.sotospeak.app.error.UiText
import com.sotospeak.app.screens.showcase.DemoCard
import com.sotospeak.app.screens.showcase.ShowcaseScaffold
import com.sotospeak.app.screens.showcase.ShowcaseSection
import com.sotospeak.designsystem.icons.SpeakingIcons

/**
 * Демо «Состояния»: EmptyState, ErrorMessage (UiText), LockedFeature —
 * стейты списков и гостевых зон приложения.
 */
@Composable
fun StatesSection(onBack: () -> Unit) {
    ShowcaseScaffold(section = ShowcaseSection.States, onBack = onBack) {
        DemoCard(title = "EmptyState") {
            StateBox {
                EmptyState(
                    icon = SpeakingIcons.Waveform,
                    title = "Здесь пока пусто",
                    subtitle = "Записей ещё нет — произнесите первую фразу",
                    ctaLabel = "К практике",
                    onCtaClick = {}
                )
            }
        }
        DemoCard(title = "ErrorMessage") {
            StateBox {
                ErrorMessage(
                    message = UiText.ServerUnavailable,
                    onRetry = {}
                )
            }
        }
        DemoCard(title = "LockedFeature (гостевой гейт)") {
            StateBox {
                LockedFeature(
                    title = "Оценки учителя",
                    description = "Зарегистрируйтесь, чтобы отправлять записи на проверку",
                    onRegisterClick = {}
                )
            }
        }
        DemoCard(title = "Живые превью онбординга (V2, B15)") {
            // Мини-версии настоящих компонентов: плеер / Training / оценка
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                com.sotospeak.app.screens.VideoPreview(Modifier.fillMaxWidth())
                com.sotospeak.app.screens.TrainingPreview(Modifier.fillMaxWidth())
                com.sotospeak.app.screens.GradePreview(Modifier.fillMaxWidth())
            }
        }
    }
}

/** Компоненты состояний рассчитаны на весь экран — в демо живёт в рамке 260dp. */
@Composable
private fun StateBox(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
    ) {
        content()
    }
}
