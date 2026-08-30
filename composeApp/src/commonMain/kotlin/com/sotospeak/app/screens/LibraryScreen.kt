package com.sotospeak.app.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sotospeak.app.components.ErrorMessage
import com.sotospeak.app.components.LoadingIndicator
import com.sotospeak.app.viewmodel.LibraryState
import com.sotospeak.designsystem.animations.speakingPressable
import com.sotospeak.designsystem.theme.LocalSpeakingColors
import com.sotospeak.designsystem.theme.SpeakingMotion
import com.sotospeak.designsystem.theme.SpeakingShapes
import com.sotospeak.designsystem.icons.SpeakingIcons
import com.sotospeak.shared.contracts.SpeakingLibrary
import kotlin.math.abs

/**
 * Экран «Библиотека тем» — список тем Speaking Trainer (спека Part 2 §2.1).
 * Стартовый экран приложения после пивота.
 *
 * DC-2 (мокап frame-library): цветные тайлы с инициалами, бейджи
 * «N ПРОЙДЕНО»/«НОВАЯ», прогресс-бар темы, подзаголовок, «N топиков» текстом.
 */
@Composable
fun LibraryScreen(
    state: LibraryState,
    onLoad: () -> Unit,
    onLibraryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) { onLoad() }

    val speaking = LocalSpeakingColors.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(speaking.background)
            .testTag("library_screen")
    ) {
        when {
            state.isLoading && state.libraries.isEmpty() -> LoadingIndicator()
            state.error != null && state.libraries.isEmpty() -> ErrorMessage(
                message = state.error,
                onRetry = onLoad
            )
            state.libraries.isEmpty() -> LibraryEmptyState()
            else -> LibraryList(
                state = state,
                onLibraryClick = onLibraryClick
            )
        }
    }
}

@Composable
private fun LibraryList(
    state: LibraryState,
    onLibraryClick: (String) -> Unit
) {
    val speaking = LocalSpeakingColors.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = 4.dp)) {
                Text(
                    text = "Библиотека тем",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = speaking.text,
                    modifier = Modifier.testTag("library_title")
                )
                Text(
                    text = "Выбери тему и начни говорить",
                    style = MaterialTheme.typography.bodyMedium,
                    color = speaking.textMuted,
                    modifier = Modifier.testTag("library_subtitle")
                )
            }
        }
        items(state.libraries, key = { it.id }) { library ->
            ThemeCard(
                library = library,
                completedTopics = state.completedTopics[library.id] ?: 0,
                onClick = { onLibraryClick(library.id) }
            )
        }
    }
}

/** Карточка темы по мокапу: тайл 64dp с инициалами, мета-строка, прогресс-бар, chevron. */
@Composable
private fun ThemeCard(
    library: SpeakingLibrary,
    completedTopics: Int,
    onClick: () -> Unit
) {
    val speaking = LocalSpeakingColors.current
    val interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val progress = if (library.topicCount > 0)
        completedTopics.coerceAtMost(library.topicCount).toFloat() / library.topicCount
    else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = SpeakingMotion.tweenMedium(),
        label = "theme_progress"
    )

    ElevatedCard(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .speakingPressable(interactionSource, pressedScale = 0.98f)   // H: :active scale(.98) из мокапа
            .testTag("library_card_${library.id}"),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ThemeCover(
                title = library.title,
                seed = library.id,
                modifier = Modifier.testTag("theme_cover_${library.id}")
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                // Fade-обрезка вместо ellipsis (мокап/ДС: перенос по словам, ≤3 строк)
                com.sotospeak.app.components.FadingEdgeText(
                    text = library.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = speaking.text,
                    maxLines = 3,
                    fadeColor = MaterialTheme.colorScheme.surfaceContainerLowest
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = topicsCountText(library.topicCount),
                        style = MaterialTheme.typography.labelSmall,
                        color = speaking.textMuted,
                        modifier = Modifier.testTag("theme_count_${library.id}")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    ThemeStatusChip(
                        completedTopics = completedTopics,
                        libraryId = library.id
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                ThemeProgressBar(
                    progress = animatedProgress,
                    modifier = Modifier.testTag("theme_progress_${library.id}")
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = SpeakingIcons.ChevronRight,
                contentDescription = null,
                tint = speaking.textMuted,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/** Тайл 64×64 с градиентом (хеш по id темы) и инициалами названия — как в мокапе. */
@Composable
private fun ThemeCover(title: String, seed: String, modifier: Modifier = Modifier) {
    val gradient = THEME_GRADIENTS[abs(seed.hashCode()) % THEME_GRADIENTS.size]
    Box(
        modifier = modifier
            .size(64.dp)
            .clip(SpeakingShapes.Chip)
            .background(Brush.linearGradient(gradient)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = themeInitials(title),
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

/** Бейдж статуса темы — M3 AssistChip (A5): «N ПРОЙДЕНО» (зелёный) / «НОВАЯ» (оранжевый),
 *  container-фон + тёмный текст (WCAG AA, цвета мокапа). */
@Composable
private fun ThemeStatusChip(completedTopics: Int, libraryId: String) {
    val speaking = LocalSpeakingColors.current
    val isDone = completedTopics > 0
    AssistChip(
        onClick = {},
        modifier = Modifier.testTag("theme_chip_${libraryId}"),
        label = {
            Text(
                text = if (isDone) "$completedTopics ПРОЙДЕНО" else "НОВАЯ",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.4.sp
            )
        },
        shape = MaterialTheme.shapes.small,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (isDone) speaking.statusReviewedContainer else speaking.statusNewContainer,
            // Токены status* имеют dark-варианты (hardcoded #256629/#8A5200 были нечитаемы
            // на тёмных контейнерах в dark theme)
            labelColor = if (isDone) speaking.statusReviewed else speaking.statusNew
        ),
        border = null
    )
}

/** Прогресс-бар темы — M3 LinearProgressIndicator 4dp (A5, трек surfaceContainerHighest). */
@Composable
private fun ThemeProgressBar(progress: Float, modifier: Modifier = Modifier) {
    LinearProgressIndicator(
        progress = { progress.coerceIn(0f, 1f) },
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp),
        color = LocalSpeakingColors.current.primary,
        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
    )
}

/**
 * Инициалы тайла — как аватар без фото: первые буквы первых двух значимых слов
 * («Разговорный английский» → «РА», «Travel & Holidays» → «TH»).
 * Одно слово → первые 2 буквы («Знакомство» → «Зн»).
 */
internal fun themeInitials(title: String): String {
    val words = title.trim().split(Regex("\\s+"))
        .filter { it.firstOrNull()?.isLetterOrDigit() == true }
    return when {
        words.size >= 2 -> (words[0].first().toString() + words[1].first().toString()).uppercase()
        words.size == 1 -> words[0].take(2).replaceFirstChar { it.uppercase() }
        else -> "?"
    }
}

/** Плюрализация: «1 топик», «3 топика», «6 топиков». */
internal fun topicsCountText(count: Int): String {
    val mod100 = count % 100
    val mod10 = count % 10
    val word = when {
        mod100 in 11..14 -> "топиков"
        mod10 == 1 -> "топик"
        mod10 in 2..4 -> "топика"
        else -> "топиков"
    }
    return "$count $word"
}

/** Градиенты тайлов из мокапа frame-library. */
private val THEME_GRADIENTS = listOf(
    listOf(Color(0xFF4A90D9), Color(0xFF2E6BB0)),
    listOf(Color(0xFF006C4C), Color(0xFF2E8B6A)),
    listOf(Color(0xFF5C6BC0), Color(0xFF3F51B5)),
    listOf(Color(0xFFFB8C00), Color(0xFFE65100))
)

@Composable
private fun LibraryEmptyState() {
    val speaking = LocalSpeakingColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .testTag("library_empty"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Пока нет доступных тем",
            style = MaterialTheme.typography.titleMedium,
            color = speaking.textMuted
        )
    }
}
