package com.funnyenglish.app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

private data class OnboardingSlide(
    val emoji: String,
    val title: String,
    val description: String
)

private val slides = listOf(
    OnboardingSlide(
        emoji = "👋",
        title = "Учим английский играючи",
        description = "Короткие тесты с картинками и аудио: слова запоминаются сами собой."
    ),
    OnboardingSlide(
        emoji = "⭐",
        title = "Звёзды, уровни и ачивки",
        description = "Проходи тесты, зарабатывай XP, открывай достижения и соревнуйся в рейтинге."
    )
)

/**
 * Онбординг первого запуска: 2 информационных слайда + экран выбора режима.
 * Выбор режима: регистрация или гость (с пояснением про обезличенные данные).
 */
@Composable
fun OnboardingScreen(
    onRegister: () -> Unit,
    onContinueAsGuest: () -> Unit
) {
    var page by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .testTag("onboarding_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (page < slides.size) {
            val slide = slides[page]
            Text(text = slide.emoji, fontSize = 72.sp)
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = slide.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("onboarding_title")
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = slide.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Индикатор слайдов
                repeat(slides.size + 1) { index ->
                    Text(
                        text = if (index == page) "●" else "○",
                        color = if (index == page) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (index < slides.size) Spacer(modifier = Modifier.width(4.dp))
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { page++ },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_next_button")
            ) {
                Text("Далее")
            }
        } else {
            // Экран выбора режима
            Text(text = "🚀", fontSize = 72.sp)
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Как начнём?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("onboarding_title")
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "С аккаунтом прогресс сохраняется в облаке и открывается вся статистика.\n\n" +
                    "Гостем можно играть без регистрации: прогресс останется на устройстве, " +
                    "а в общую статистику попадут только обезличенные данные (без имени и email).",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onRegister,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_register_button")
            ) {
                Text("Зарегистрироваться")
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onContinueAsGuest,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_guest_button")
            ) {
                Text("Продолжить как гость")
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = onRegister,
                modifier = Modifier.testTag("onboarding_login_link")
            ) {
                Text("У меня уже есть аккаунт")
            }
        }
    }
}
