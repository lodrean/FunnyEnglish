package com.funnyenglish.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funnyenglish.core.ui.theme.FunnyColors

/**
 * Section header with optional "View All" action
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    onViewAll: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (onViewAll != null) {
            TextButton(onClick = onViewAll) {
                Text(
                    text = "View All",
                    fontWeight = FontWeight.Bold,
                    color = FunnyColors.Primary
                )
            }
        }
    }
}

/**
 * Stat item for profile screens
 */
@Composable
fun StatItem(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    valueColor: Color = FunnyColors.Primary
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Badge component for categories/achievements
 */
@Composable
fun CategoryEmoji(name: String): String {
    return when {
        name.contains("Animals", ignoreCase = true) ||
        name.contains("Животные", ignoreCase = true) -> "🐾"
        name.contains("Colors", ignoreCase = true) ||
        name.contains("Цвета", ignoreCase = true) -> "🎨"
        name.contains("Numbers", ignoreCase = true) ||
        name.contains("Числа", ignoreCase = true) -> "🔢"
        name.contains("Food", ignoreCase = true) ||
        name.contains("Еда", ignoreCase = true) -> "🍎"
        name.contains("Family", ignoreCase = true) ||
        name.contains("Семья", ignoreCase = true) -> "👨‍👩‍👧"
        name.contains("Clothes", ignoreCase = true) ||
        name.contains("Одежда", ignoreCase = true) -> "👕"
        else -> "📚"
    }
}
