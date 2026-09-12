package com.sotospeak.app.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sotospeak.app.components.EmptyState
import com.sotospeak.app.components.ErrorMessage
import com.sotospeak.app.localization.LocalAppStrings
import com.sotospeak.app.localization.RussianStrings
import com.sotospeak.app.viewmodel.ContinueTopic
import com.sotospeak.app.viewmodel.LibraryState
import com.sotospeak.designsystem.animations.ListSkeleton
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
    onContinueClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) { onLoad() }

    val speaking = LocalSpeakingColors.current
    val strings = LocalAppStrings.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(speaking.background)
            .testTag("library_screen")
    ) {
        // bd h3l.8: сеть недоступна — показан офлайн-кэш
        if (state.offlineData) {
            Text(
                text = strings.offlineBanner,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .background(MaterialTheme.colorScheme.errorContainer, MaterialTheme.shapes.medium)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("offline_banner")
            )
        }
        when {
            state.isLoading && state.libraries.isEmpty() -> ListSkeleton()
            state.error != null && state.libraries.isEmpty() -> ErrorMessage(
                message = state.error,
                onRetry = onLoad
            )
            state.libraries.isEmpty() -> EmptyState(
                icon = SpeakingIcons.Mic,
                title = strings.libraryEmptyTitle,
                subtitle = strings.libraryEmptySubtitle,
                ctaLabel = strings.ctaRefresh,
                onCtaClick = onLoad,
                modifier = Modifier.testTag("library_empty")
            )
            else -> LibraryList(
                state = state,
                onLibraryClick = onLibraryClick,
                onContinueClick = onContinueClick
            )
        }
    }
}

@Composable
private fun LibraryList(
    state: LibraryState,
    onLibraryClick: (String) -> Unit,
    onContinueClick: () -> Unit
) {
    val speaking = LocalSpeakingColors.current
    val strings = LocalAppStrings.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = 4.dp)) {
                Text(
                    text = strings.libraryTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = speaking.text,
                    modifier = Modifier.testTag("library_title")
                )
                Text(
                    text = strings.librarySubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = speaking.textMuted,
                    modifier = Modifier.testTag("library_subtitle")
                )
            }
        }
        // «Продолжить путь» (редизайн V1): последний активный топик, тап — сразу к вопросам
        state.continueTopic?.let { topic ->
            item(key = "continue_card") {
                ContinuePathCard(
                    topic = topic,
                    onClick = onContinueClick
                )
            }
        }
        // Темы — вертикальный «путь» (редизайн V1): ось с узлами слева от карточек.
        // height(IntrinsicSize.Min) обязателен: TrailAxis использует fillMaxHeight,
        // без интринсиков он растягивается на maxHeight контейнера (весь вьюпорт
        // LazyColumn) — карточки разъезжаются, узел улетает (поймано живым прогоном).
        itemsIndexed(state.libraries, key = { _, library -> library.id }) { index, library ->
            val completed = state.completedTopics[library.id] ?: 0
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                TrailAxis(
                    nodeState = trailNodeState(
                        completed = completed,
                        total = library.topicCount
                    ),
                    isFirst = index == 0,
                    isLast = index == state.libraries.lastIndex
                )
                Spacer(modifier = Modifier.width(12.dp))
                ThemeCard(
                    library = library,
                    completedTopics = completed,
                    modifier = Modifier.weight(1f),
                    onClick = { onLibraryClick(library.id) }
                )
            }
        }
    }
}

/** Состояние узла «пути» темы (редизайн V1): пройдена / в процессе / новая. */
private fun trailNodeState(completed: Int, total: Int): TrailNodeState = when {
    total > 0 && completed >= total -> TrailNodeState.DONE
    completed > 0 -> TrailNodeState.CURRENT
    else -> TrailNodeState.FRESH
}

internal enum class TrailNodeState { DONE, CURRENT, FRESH }

/**
 * Ось «пути»: линия 3dp (outlineVariant) + узел 13dp. Пройдено — success,
 * есть движение — primary, новая тема — record (персиковый маркер «начни здесь»).
 */
@Composable
internal fun TrailAxis(
    nodeState: TrailNodeState,
    isFirst: Boolean,
    isLast: Boolean
) {
    val speaking = LocalSpeakingColors.current
    val nodeColor = when (nodeState) {
        TrailNodeState.DONE -> speaking.statusReviewed
        TrailNodeState.CURRENT -> speaking.primary
        TrailNodeState.FRESH -> speaking.statusNew
    }
    val axisColor = MaterialTheme.colorScheme.outlineVariant
    Box(
        modifier = Modifier
            .width(20.dp)
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        // Линия оси: до и после узла; у первой/последней темы линия обрывается у узла
        Column(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .clip(MaterialTheme.shapes.small)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        axisColor,
                        // верхняя половина у первой темы скрыта
                        RoundedCornerShape(
                            topStartPercent = if (isFirst) 50 else 0,
                            topEndPercent = if (isFirst) 50 else 0
                        )
                    )
                    .alpha(if (isFirst) 0f else 1f)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        axisColor,
                        RoundedCornerShape(
                            bottomStartPercent = if (isLast) 50 else 0,
                            bottomEndPercent = if (isLast) 50 else 0
                        )
                    )
                    .alpha(if (isLast) 0f else 1f)
            )
        }
        Box(
            modifier = Modifier
                .size(13.dp)
                .background(nodeColor, RoundedCornerShape(percent = 50))
                .border(3.dp, speaking.background, RoundedCornerShape(percent = 50))
        )
    }
}

/**
 * Синяя карточка «Продолжить путь» (редизайн V1, DS v3.2.0 B11): primaryStrong-фон,
 * eyebrow, название топика, мета попыток и кольцо прогресса (белая дуга).
 */
@Composable
internal fun ContinuePathCard(
    topic: ContinueTopic,
    onClick: () -> Unit
) {
    val speaking = LocalSpeakingColors.current
    val strings = LocalAppStrings.current
    val progress = if (topic.attemptsDone in 0..2) topic.attemptsDone / 3f else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = SpeakingMotion.tweenMedium(),
        label = "continue_progress"
    )

    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("library_continue_card"),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = speaking.primaryStrong)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strings.continuePathEyebrow,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp,
                    color = speaking.onPrimary.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = topic.topicTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = speaking.onPrimary,
                    modifier = Modifier.testTag("continue_topic_title")
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = strings.continueAttemptsMeta(topic.attemptsDone),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = speaking.onPrimary.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            ProgressRing(
                progress = animatedProgress,
                size = 56.dp,
                strokeWidth = 6.dp,
                color = speaking.onPrimary,
                track = speaking.onPrimary.copy(alpha = 0.25f),
                modifier = Modifier.testTag("continue_progress_ring")
            )
        }
    }
}

/** Кольцо прогресса (редизайн V1): дуга на треке, старт сверху. */
@Composable
internal fun ProgressRing(
    progress: Float,
    size: androidx.compose.ui.unit.Dp,
    strokeWidth: androidx.compose.ui.unit.Dp,
    color: Color,
    track: Color,
    modifier: Modifier = Modifier
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = SpeakingMotion.tweenMedium(),
        label = "progress_ring"
    )
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val inset = stroke / 2
            val diameter = this.size.minDimension - stroke
            drawArc(
                color = track,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(diameter, diameter),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * animated,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(diameter, diameter),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
    }
}

/** Карточка темы (редизайн V1): обложка 54dp, мета-строка, прогресс-бар, мини-кольцо. */
@Composable
private fun ThemeCard(
    library: SpeakingLibrary,
    completedTopics: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val speaking = LocalSpeakingColors.current
    val strings = LocalAppStrings.current
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
        modifier = modifier
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
                        text = strings.topicsCount(library.topicCount),
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
            Spacer(modifier = Modifier.width(12.dp))
            ProgressRing(
                progress = progress,
                size = 40.dp,
                strokeWidth = 5.dp,
                color = speaking.primary,
                track = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.testTag("theme_ring_${library.id}")
            )
        }
    }
}

/** Тайл 54×54 (редизайн V1) с градиентом (хеш по id темы) и инициалами названия. */
@Composable
private fun ThemeCover(title: String, seed: String, modifier: Modifier = Modifier) {
    val gradient = THEME_GRADIENTS[abs(seed.hashCode()) % THEME_GRADIENTS.size]
    Box(
        modifier = modifier
            .size(54.dp)
            .clip(SpeakingShapes.Chip)
            .background(Brush.linearGradient(gradient)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = themeInitials(title),
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

/** Бейдж статуса темы — M3 AssistChip (A5): «N ПРОЙДЕНО» (зелёный) / «НОВАЯ» (оранжевый),
 *  container-фон + тёмный текст (WCAG AA, цвета мокапа). */
@Composable
private fun ThemeStatusChip(completedTopics: Int, libraryId: String) {
    val speaking = LocalSpeakingColors.current
    val strings = LocalAppStrings.current
    val isDone = completedTopics > 0
    AssistChip(
        onClick = {},
        modifier = Modifier.testTag("theme_chip_${libraryId}"),
        label = {
            Text(
                text = if (isDone) strings.topicsCompleted(completedTopics) else strings.themeStatusNew,
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
internal fun topicsCountText(count: Int): String = RussianStrings.topicsCount(count)

/** Градиенты тайлов из мокапа frame-library. */
private val THEME_GRADIENTS = listOf(
    listOf(Color(0xFF4A90D9), Color(0xFF2E6BB0)),
    listOf(Color(0xFF006C4C), Color(0xFF2E8B6A)),
    listOf(Color(0xFF5C6BC0), Color(0xFF3F51B5)),
    listOf(Color(0xFFFB8C00), Color(0xFFE65100))
)

