package com.sotospeak.app.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sotospeak.app.error.UiText
import com.sotospeak.app.error.asString
import com.sotospeak.designsystem.tokens.IconSizeXLarge
import com.sotospeak.designsystem.tokens.SpaceMd
import com.sotospeak.designsystem.tokens.SpaceSm
import com.sotospeak.designsystem.tokens.SpaceXl

@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp
        )
    }
}

/**
 * Единый empty-state (аудит D-10): иконка + заголовок + опциональные подпись и CTA,
 * по образцу [ErrorMessage]. Иконку передавать из `SpeakingIcons` (кастомные
 * ImageVector) — material-иконки в WASM-canvas не рендерятся (грабля №75).
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    ctaLabel: String? = null,
    onCtaClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(SpaceXl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(IconSizeXLarge)
        )
        Spacer(modifier = Modifier.height(SpaceMd))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(SpaceSm))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        if (ctaLabel != null && onCtaClick != null) {
            Spacer(modifier = Modifier.height(SpaceMd))
            Button(onClick = onCtaClick) {
                Text(ctaLabel)
            }
        }
    }
}

/**
 * Экран ошибки. Принимает типизированный [UiText] — маппинг технических сообщений
 * делается заранее (`Throwable.toUiText`, app/error/UiText.kt), компонент строки не переводит.
 */
@Composable
fun ErrorMessage(
    message: UiText,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Упс!",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message.asString(),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Попробовать снова")
            }
        }
    }
}
