package com.funnyenglish.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.funnyenglish.designsystem.components.buttons.FunnyButton
import com.funnyenglish.designsystem.components.buttons.FunnyButtonSize
import com.funnyenglish.designsystem.components.buttons.FunnyButtonType
import com.funnyenglish.designsystem.components.cards.FunnyCard
import com.funnyenglish.designsystem.tokens.SpaceMd

/**
 * FunnyEnglish Lessons Screen
 * 
 * List of available lessons and categories
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonsScreen(
    onBack: () -> Unit = {},
    onStartLesson: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Уроки") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(SpaceMd),
            verticalArrangement = Arrangement.spacedBy(SpaceMd)
        ) {
            item {
                Text(
                    text = "Адаптивные уроки",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            
            item {
                FunnyCard {
                    Column {
                        Text(
                            text = "Быстрая тренировка",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "7 минут • Подборка заданий",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FunnyButton(
                            text = "Начать",
                            onClick = onStartLesson,
                            type = FunnyButtonType.PRIMARY,
                            size = FunnyButtonSize.SMALL
                        )
                    }
                }
            }
            
            item {
                FunnyCard {
                    Column {
                        Text(
                            text = "Грамматика",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Времена, артикли, предлоги",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FunnyButton(
                            text = "Начать",
                            onClick = onStartLesson,
                            type = FunnyButtonType.SECONDARY,
                            size = FunnyButtonSize.SMALL
                        )
                    }
                }
            }
            
            item {
                FunnyCard {
                    Column {
                        Text(
                            text = "Словарный запас",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Новые слова и фразы",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FunnyButton(
                            text = "Начать",
                            onClick = onStartLesson,
                            type = FunnyButtonType.SECONDARY,
                            size = FunnyButtonSize.SMALL
                        )
                    }
                }
            }
        }
    }
}
