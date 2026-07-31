package com.funnyenglish.app.components.questions

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.platform.testTag
import coil3.compose.AsyncImage
import coil3.compose.SubcomposeAsyncImage
import com.funnyenglish.app.components.questions.animations.PulseAnimation
import com.funnyenglish.app.util.Logger
import com.funnyenglish.designsystem.theme.funnyColors
import androidx.compose.material3.MaterialTheme
import com.funnyenglish.shared.model.*
import kotlin.math.roundToInt

/**
 * Fallback изображение когда не удалось загрузить картинку
 */
@Composable
private fun ImageFallback(
    onSizeAvailable: (Size) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
    ) {
        // Текстовая метка что это fallback
        Text(
            text = "⚠️ Изображение не загружено",
            color = Color.Red,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.TopCenter).padding(8.dp)
        )
        
        // Имитируем "комнату" с предметами мебели
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            
            // Стены (светло-бежевый)
            drawRect(color = Color(0xFFF5F5DC), size = size)
            
            // Пол (темнее)
            drawRect(
                color = Color(0xFFE8DCC8),
                topLeft = Offset(0f, h * 0.7f),
                size = Size(w, h * 0.3f)
            )
            
            // Холодильник (слева)
            drawRect(
                color = Color(0xFFB0C4DE),
                topLeft = Offset(w * 0.05f, h * 0.15f),
                size = Size(w * 0.2f, h * 0.5f)
            )
            // Ручка холодильника
            drawRect(
                color = Color(0xFF808080),
                topLeft = Offset(w * 0.22f, h * 0.35f),
                size = Size(w * 0.02f, h * 0.08f)
            )
            
            // Стол (посередине)
            drawRect(
                color = Color(0xFFDEB887),
                topLeft = Offset(w * 0.35f, h * 0.45f),
                size = Size(w * 0.4f, h * 0.3f)
            )
            // Ножки стола
            drawRect(color = Color(0xFF8B4513), topLeft = Offset(w * 0.38f, h * 0.75f), size = Size(w * 0.03f, h * 0.15f))
            drawRect(color = Color(0xFF8B4513), topLeft = Offset(w * 0.69f, h * 0.75f), size = Size(w * 0.03f, h * 0.15f))
            
            // Нож и ложка (справа на столе)
            drawRect(
                color = Color(0xFFC0C0C0),
                topLeft = Offset(w * 0.78f, h * 0.55f),
                size = Size(w * 0.15f, h * 0.02f)
            )
            drawRect(
                color = Color(0xFFD4AF37),
                topLeft = Offset(w * 0.78f, h * 0.65f),
                size = Size(w * 0.12f, h * 0.03f)
            )
        }
        
        // Уведомляем о размере
        LaunchedEffect(Unit) {
            onSizeAvailable(Size(800f, 600f))
        }
    }
}

/**
 * Компонент для вопроса типа IMAGE_WORD_MATCH
 * Пользователь перетаскивает слова к областям на изображении
 * 
 * @param content Контент вопроса (изображение, слова, hotspots)
 * @param currentMatches Текущие сопоставления wordId -> hotspotId (из ViewModel)
 * @param onMatch Callback при сопоставлении слова с hotspot (wordId, hotspotId)
 * @param onUnmatch Callback при отвязывании слова от hotspot (wordId)
 * @param modifier Modifier для кастомизации
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImageWordMatchQuestion(
    content: ImageWordMatchContent,
    currentMatches: Map<String, String> = emptyMap(),
    onMatch: (String, String) -> Unit,  // wordId, hotspotId
    onUnmatch: ((String) -> Unit)? = null,  // wordId (optional)
    modifier: Modifier = Modifier
) {
    Logger.d("ImageWordMatch", "words=${content.words.size}, hotspots=${content.hotspots.size}, imageUrl=${content.imageUrl}")
    // Состояние сопоставлений: wordId -> hotspotId (синхронизируется с ViewModel)
    var matchedWords by remember { mutableStateOf(currentMatches) }
    
    // Синхронизация с внешним состоянием
    LaunchedEffect(currentMatches) {
        matchedWords = currentMatches
    }
    
    // Состояние drag & drop
    var draggedWord by remember { mutableStateOf<WordData?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }
    
    // Границы изображения для расчета относительных координат
    var imageBounds by remember { mutableStateOf(Rect.Zero) }
    var imageSize by remember { mutableStateOf(Size.Zero) }
    var originalImageSize by remember { mutableStateOf(Size.Zero) }
    
    // Масштаб и смещение для ContentScale.Fit
    var imageScale by remember { mutableStateOf(1f) }
    var imageOffsetX by remember { mutableStateOf(0f) }
    var imageOffsetY by remember { mutableStateOf(0f) }
    
    // Позиция контейнера изображения (для корректного overlay)
    var containerPosition by remember { mutableStateOf(Offset.Zero) }
    
    // Прогресс
    // Защита от деления на ноль: пустой контент (0 слов) → прогресс 0, не NaN
    val progress = if (content.words.isNotEmpty()) {
        matchedWords.size.toFloat() / content.words.size
    } else {
        0f
    }
    
    // Корневой Box для overlay поверх всего контента
    Box(modifier = modifier.fillMaxWidth().wrapContentHeight()) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(16.dp)
    ) {
        // Инструкция
        Text(
            text = content.instruction,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("iwm_instruction")
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Прогресс-бар
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("iwm_progress_bar"),
            color = MaterialTheme.funnyColors.success,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        
        Text(
            text = "${matchedWords.size} / ${content.words.size} words matched",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(top = 4.dp)
                .testTag("iwm_progress_text")
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Контейнер для изображения и hotspot'ов
        // Используем BoxWithConstraints для адаптивной высоты
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { containerPosition = it.positionInRoot() },
            contentAlignment = Alignment.Center
        ) {
            // Адаптивная максимальная высота в зависимости от ширины экрана
            val maxHeight = when {
                maxWidth < 600.dp -> 250.dp  // Компактный (телефон)
                maxWidth < 840.dp -> 350.dp  // Средний (планшет)
                else -> 400.dp               // Расширенный (десктоп)
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f/3f)
                    .heightIn(max = maxHeight),
                contentAlignment = Alignment.Center
            ) {
                // Общий контейнер для изображения и hotspot'ов
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Изображение на фоне
                Logger.d("ImageWordMatch", "Loading image with Coil: ${content.imageUrl}")
                SubcomposeAsyncImage(
                    model = content.imageUrl,
                    contentDescription = "Question image",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("iwm_image")
                        .onGloballyPositioned { coordinates ->
                            val position = coordinates.positionInRoot()
                            val size = coordinates.size.toSize()
                            imageBounds = Rect(position, size)
                            imageSize = size
                            Logger.d("ImageWordMatch", "Image layout size: $size")
                        },
                    loading = {
                        Logger.d("ImageWordMatch", "Image loading state for: ${content.imageUrl}")
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    },
                    onError = { state ->
                        Logger.e("ImageWordMatch", "Failed to load image: ${content.imageUrl}", state.result.throwable)
                    },
                    onSuccess = { state ->
                        val imgWidth = state.result.image.width
                        val imgHeight = state.result.image.height
                        originalImageSize = Size(imgWidth.toFloat(), imgHeight.toFloat())
                        Logger.d("ImageWordMatch", "Image loaded successfully: ${content.imageUrl}, originalSize=${imgWidth}x${imgHeight}")
                    },
                    error = {
                        Logger.e("ImageWordMatch", "Image error state for: ${content.imageUrl}")
                        ImageFallback(
                            onSizeAvailable = { size ->
                                imageSize = size
                                originalImageSize = size
                            }
                        )
                    }
                )
                
                // Hotspot'ы поверх изображения
                // Вычисляем масштаб и смещение для ContentScale.Fit
                val containerWidth = imageSize.width
                val containerHeight = imageSize.height
                val imgWidth = originalImageSize.width
                val imgHeight = originalImageSize.height
                
                val (scale, offsetX, offsetY) = if (containerWidth > 0 && containerHeight > 0 && imgWidth > 0 && imgHeight > 0) {
                    val scaleX = containerWidth / imgWidth
                    val scaleY = containerHeight / imgHeight
                    val fitScale = minOf(scaleX, scaleY)
                    val dx = (containerWidth - imgWidth * fitScale) / 2f
                    val dy = (containerHeight - imgHeight * fitScale) / 2f
                    // Обновляем состояние для использования в drag&drop
                    imageScale = fitScale
                    imageOffsetX = dx
                    imageOffsetY = dy
                    Triple(fitScale, dx, dy)
                } else {
                    imageScale = 1f
                    imageOffsetX = 0f
                    imageOffsetY = 0f
                    Triple(1f, 0f, 0f)
                }
                
                // Размер отображаемого изображения с учетом масштаба.
                // Если оригинальный размер неизвестен (картинка ещё грузится или
                // недоступна — в т.ч. в тестах без сети), рендерим hotspot'ы
                // относительно контейнера, а не с нулевым размером.
                val displayedImageSize = if (imgWidth > 0 && imgHeight > 0) {
                    Size(imgWidth * scale, imgHeight * scale)
                } else {
                    Size(containerWidth, containerHeight)
                }
                
                Logger.d("ImageWordMatch", "Hotspot rendering: container=${containerWidth}x${containerHeight}, original=${imgWidth}x${imgHeight}, scale=$scale, offset=($offsetX, $offsetY)")
                
                content.hotspots.forEachIndexed { index, hotspot ->
                    val isMatched = matchedWords.containsValue(hotspot.id)
                    val isTarget = isDragging && draggedWord != null && !matchedWords.containsValue(hotspot.id)
                    
                    HotspotOverlay(
                        hotspot = hotspot,
                        hotspotIndex = index,
                        imageSize = displayedImageSize,
                        offsetX = offsetX,
                        offsetY = offsetY,
                        isMatched = isMatched,
                        isTarget = isTarget,
                        matchedWord = content.words.find { 
                            it.id == matchedWords.entries.find { it.value == hotspot.id }?.key 
                        },
                        onClick = {
                            if (isMatched) {
                                val wordId = matchedWords.entries.find { it.value == hotspot.id }?.key
                                matchedWords = matchedWords.filter { it.value != hotspot.id }
                                wordId?.let { onUnmatch?.invoke(it) }
                            }
                        }
                    )
                }  // Конец внутреннего Box с изображением
            }  // Конец внутреннего Box (aspectRatio)
        }  // Конец BoxWithConstraints
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Банк слов
        WordBank(
            words = content.words,
            matchedWords = matchedWords,
            onDragStart = { word ->
                draggedWord = word
                isDragging = true
            },
            onDrag = { absolutePosition ->
                dragOffset = absolutePosition  // Сохраняем абсолютную позицию
            },
            onDragEnd = { finalPosition ->
                isDragging = false
                
                Logger.d("ImageWordMatch", "Drag ended at $finalPosition, imageBounds=$imageBounds, scale=$imageScale, offset=($imageOffsetX, $imageOffsetY)")
                
                // Рассчитываем координаты относительно отображаемого изображения с учетом смещения
                val localX = finalPosition.x - imageBounds.left - imageOffsetX
                val localY = finalPosition.y - imageBounds.top - imageOffsetY
                
                // Размер отображаемого изображения
                val displayedWidth = originalImageSize.width * imageScale
                val displayedHeight = originalImageSize.height * imageScale
                
                // Относительные координаты внутри изображения (0..1)
                val relativeX = if (displayedWidth > 0) localX / displayedWidth else -1f
                val relativeY = if (displayedHeight > 0) localY / displayedHeight else -1f
                
                Logger.d("ImageWordMatch", "Local coords: ($localX, $localY), displayed: ${displayedWidth}x${displayedHeight}, relative: ($relativeX, $relativeY)")
                
                // Проверяем попадание в границы отображаемого изображения
                if (relativeX in 0f..1f && relativeY in 0f..1f) {
                    val targetHotspot = content.hotspots.find { 
                        val contains = it.contains(relativeX, relativeY)
                        val notMatched = !matchedWords.containsValue(it.id)
                        Logger.d("ImageWordMatch", "Checking hotspot ${it.id}: contains=$contains, notMatched=$notMatched, bounds=(${it.x},${it.y},${it.width},${it.height})")
                        contains && notMatched
                    }
                    
                    if (targetHotspot != null && draggedWord != null) {
                        Logger.d("ImageWordMatch", "Dropped word ${draggedWord!!.text} on hotspot ${targetHotspot.id}")
                        onMatch(draggedWord!!.id, targetHotspot.id)
                    } else {
                        Logger.d("ImageWordMatch", "No matching hotspot found for drop at ($relativeX, $relativeY)")
                    }
                } else {
                    Logger.d("ImageWordMatch", "Drop outside image bounds: local=($localX, $localY), relative=($relativeX, $relativeY)")
                }
                
                draggedWord = null
                dragOffset = Offset.Zero
            }
        )
    }  // Конец Column
    
    // Overlay перетаскиваемого слова поверх всего контента
    // Используем absoluteOffset для позиционирования относительно окна
    if (isDragging && draggedWord != null) {
        DraggedWordOverlay(
            word = draggedWord!!,
            position = dragOffset
        )
    }
}  // Конец Box
}
}

/**
 * Overlay hotspot'а поверх изображения
 */
@Composable
private fun HotspotOverlay(
    hotspot: HotspotData,
    hotspotIndex: Int,
    imageSize: Size,
    offsetX: Float = 0f,
    offsetY: Float = 0f,
    isMatched: Boolean,
    isTarget: Boolean,
    matchedWord: WordData?,
    onClick: () -> Unit
) {
    val density = LocalDensity.current
    
    // Конвертируем относительные координаты в пиксели + добавляем смещение
    val x = with(density) { (offsetX + hotspot.x * imageSize.width).toDp() }
    val y = with(density) { (offsetY + hotspot.y * imageSize.height).toDp() }
    val width = with(density) { (hotspot.width * imageSize.width).toDp() }
    val height = with(density) { (hotspot.height * imageSize.height).toDp() }
    
    val shape = if (hotspot.shape == HotspotShape.CIRCLE) CircleShape else RoundedCornerShape(8.dp)
    
    Box(
        modifier = Modifier
            .offset(x, y)
            .size(width, height)
            .clip(shape)
            .border(
                width = if (isMatched) 3.dp else 2.dp,
                color = when {
                    isMatched -> MaterialTheme.funnyColors.success
                    isTarget -> MaterialTheme.colorScheme.primary
                    else -> Color.Red.copy(alpha = 0.5f) // Visible border for inactive hotspots
                },
                shape = shape
            )
            .background(
                when {
                    isMatched -> MaterialTheme.funnyColors.success.copy(alpha = 0.3f)
                    isTarget -> MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                    else -> Color.Red.copy(alpha = 0.1f) // Visible background for inactive hotspots
                }
            )
            .then(if (isMatched) Modifier.clickable(onClick = onClick) else Modifier)
            .testTag("iwm_hotspot_${hotspotIndex}")
    ) {
        // Pulse анимация для доступных hotspot'ов
        if (isTarget) {
            PulseAnimation {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(2.dp, MaterialTheme.colorScheme.primary, shape)
                )
            }
        }
        
        // Метка сопоставленного слова
        if (isMatched && matchedWord != null) {
            MatchedWordLabel(word = matchedWord)
        }
    }
}

/**
 * Метка сопоставленного слова
 */
@Composable
private fun MatchedWordLabel(word: WordData) {
    val scale = animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "labelScale"
    ).value
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = MaterialTheme.funnyColors.success,
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.scale(scale)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = word.text,
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Банк слов для перетаскивания
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WordBank(
    words: List<WordData>,
    matchedWords: Map<String, String>,
    onDragStart: (WordData) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: (Offset) -> Unit
) {
    // Тег на Column (всегда имеет размер благодаря label), а не на FlowRow —
    // пустой FlowRow (0 слов) схлопывается в 0x0 и не проходит isDisplayed
    Column(modifier = Modifier.fillMaxWidth().testTag("iwm_word_bank")) {
        Text(
            text = "Drag words to the image:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.testTag("iwm_word_bank_label")
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            words.forEachIndexed { index, word ->
                DraggableWordCard(
                    word = word,
                    wordIndex = index,
                    isMatched = matchedWords.containsKey(word.id),
                    onDragStart = { onDragStart(word) },
                    onDrag = onDrag,
                    onDragEnd = onDragEnd
                )
            }
        }
    }
}

/**
 * Карточка слова с поддержкой drag & drop
 * При перетаскивании передает абсолютные координаты (positionInRoot)
 */
@Composable
private fun DraggableWordCard(
    word: WordData,
    wordIndex: Int,
    isMatched: Boolean,
    onDragStart: () -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: (Offset) -> Unit
) {
    var isDragging by remember { mutableStateOf(false) }
    var cardPosition by remember { mutableStateOf(Offset.Zero) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    
    val scale = animateFloatAsState(
        targetValue = if (isDragging) 1.1f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "cardScale"
    ).value
    
    val alpha = animateFloatAsState(
        targetValue = if (isMatched) 0.5f else 1f,
        label = "cardAlpha"
    ).value
    
    Surface(
        modifier = Modifier
            .scale(scale)
            .alpha(alpha)
            .onGloballyPositioned { 
                // Запоминаем позицию карточки
                cardPosition = it.positionInRoot()
            }
            .testTag("iwm_word_${wordIndex}")
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        if (!isMatched) {
                            isDragging = true
                            dragOffset = Offset.Zero
                            onDragStart()
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffset += dragAmount
                        // Передаем абсолютную позицию (позиция карточки + смещение)
                        onDrag(cardPosition + dragOffset)
                    },
                    onDragEnd = {
                        isDragging = false
                        onDragEnd(cardPosition + dragOffset)
                        dragOffset = Offset.Zero
                    },
                    onDragCancel = {
                        isDragging = false
                        dragOffset = Offset.Zero
                    }
                )
            },
        shape = RoundedCornerShape(12.dp),
        color = when {
            isMatched -> MaterialTheme.funnyColors.success.copy(alpha = 0.1f)
            isDragging -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            2.dp,
            when {
                isMatched -> MaterialTheme.funnyColors.success
                isDragging -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            }
        ),
        shadowElevation = if (isDragging) 8.dp else 2.dp
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = word.text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (isMatched) MaterialTheme.funnyColors.success else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Перетаскиваемое слово (overlay)
 * Отображается поверх всего контента в позиции курсора/пальца
 * Использует graphicsLayer с translation для плавного следования за мышью без recomposition
 * @param position Абсолютная позиция относительно окна
 */
@Composable
private fun DraggedWordOverlay(
    word: WordData,
    position: Offset
) {
    Box(
        modifier = Modifier
            .graphicsLayer {
                // Используем translation для позиционирования - это самый быстрый способ
                translationX = position.x
                translationY = position.y
                scaleX = 1.1f
                scaleY = 1.1f
                shadowElevation = 20f
                alpha = 0.95f
            }
            .zIndex(100f)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
            shadowElevation = 8.dp
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = word.text,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}


// Note: FlowRow доступен в androidx.compose.foundation.layout (Compose 1.4+)

