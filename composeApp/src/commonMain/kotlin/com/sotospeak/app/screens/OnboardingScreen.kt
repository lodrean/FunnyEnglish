package com.sotospeak.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import com.sotospeak.designsystem.icons.SpeakingIcons
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import com.sotospeak.designsystem.theme.LocalSpeakingColors
import com.sotospeak.designsystem.animations.speakingPressable
import com.sotospeak.designsystem.theme.SpeakingShapes

private data class OnboardingSlide(
    val icon: ImageVector,
    val title: String,
    val description: String
)

/** 3 слайда value-prop по мокапу frame-onboarding (Playful Coach v1.1). */
private val slides = listOf(
    OnboardingSlide(
        icon = SpeakingIcons.Play,
        title = "Смотри видео",
        description = "Короткие видео с субтитрами на живые темы — от знакомства до уверенного small talk"
    ),
    OnboardingSlide(
        icon = SpeakingIcons.Mic,
        title = "Тренируйся вслух",
        description = "Отвечай на вопросы голосом — три попытки, записи остаются только на твоём устройстве"
    ),
    OnboardingSlide(
        icon = SpeakingIcons.Send,
        title = "Отправь учителю",
        description = "Лучшие записи отправляй учителю и получай оценки с разбором ошибок"
    )
)

/**
 * Онбординг первого запуска (frame-onboarding): 3 слайда + CTA «Начать».
 * Регистрации на онбординге НЕТ — гость сразу попадает в библиотеку ([onFinish]).
 */
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val speaking = LocalSpeakingColors.current
    var page by remember { mutableIntStateOf(0) }
    val slide = slides[page]
    val isLast = page == slides.lastIndex

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(speaking.background)
            // edge-to-edge: CTA и заголовок не должны уходить под системные бары
            .windowInsetsPadding(WindowInsets.safeContent)
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp)
            .testTag("onboarding_screen")
    ) {
        // .onb-illu: карточка 180dp на primaryContainer, radius-card-large
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .background(speaking.primaryContainer, SpeakingShapes.CardLarge)
                    .testTag("onboarding_illustration_card"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = slide.icon,
                    contentDescription = slide.title,
                    modifier = Modifier.size(88.dp),
                    tint = speaking.primary
                )
            }
        }

        // .onb-dots: активная точка — пилюля 24dp primary
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 24.dp)
                .testTag("onboarding_dots"),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(slides.size) { index ->
                val active = index == page
                Box(
                    modifier = Modifier
                        .width(if (active) 24.dp else 8.dp)
                        .height(8.dp)
                        .background(
                            // M3 page indicator (A2): активная точка primary, неактивные outlineVariant
                            color = if (active) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant,
                            shape = CircleShape
                        )
                )
                if (index < slides.lastIndex) Spacer(modifier = Modifier.width(8.dp))
            }
        }

        // .onb-copy
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = slide.title,
                fontSize = 31.sp,
                fontWeight = FontWeight.ExtraBold,
                color = speaking.text,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("onboarding_title")
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = slide.description,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = speaking.textMuted,
                textAlign = TextAlign.Center,
                lineHeight = 21.sp
            )
        }

        val nextIsrc = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
        Button(
            onClick = {
                if (isLast) onFinish() else page++
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .padding(bottom = 24.dp)
                .speakingPressable(nextIsrc)
                .testTag("onboarding_next_button"),
            shape = MaterialTheme.shapes.medium,
            interactionSource = nextIsrc
        ) {
            Text(if (isLast) "Начать" else "Далее")
        }
    }
}
