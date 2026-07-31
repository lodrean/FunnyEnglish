package com.funnyenglish.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.funnyenglish.app.components.ErrorMessage
import com.funnyenglish.app.components.LoadingIndicator
import com.funnyenglish.app.viewmodel.LibraryState
import com.funnyenglish.designsystem.theme.LocalSpeakingColors
import com.funnyenglish.designsystem.theme.SpeakingShapes

/**
 * Экран «Библиотека» — список тем Speaking Trainer (спека Part 2 §2.1).
 * Стартовый экран приложения после пивота.
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
            Text(
                text = "Библиотека",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = speaking.text,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        items(state.libraries, key = { it.id }) { library ->
            Card(
                onClick = { onLibraryClick(library.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("library_card_${library.id}"),
                shape = SpeakingShapes.Card,
                colors = CardDefaults.cardColors(containerColor = speaking.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = speaking.primary,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = library.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = speaking.text
                        )
                        library.description?.let { desc ->
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodyMedium,
                                color = speaking.textMuted
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = SpeakingShapes.StatusPill,
                        color = speaking.primaryContainer
                    ) {
                        Text(
                            text = "${library.topicCount}",
                            style = MaterialTheme.typography.labelMedium,
                            color = speaking.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

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
