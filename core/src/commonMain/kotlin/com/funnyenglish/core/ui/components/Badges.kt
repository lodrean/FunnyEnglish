package com.funnyenglish.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funnyenglish.core.ui.theme.FunnyColors
import com.funnyenglish.shared.model.Difficulty

@Composable
fun DifficultyBadge(
    difficulty: Difficulty,
    modifier: Modifier = Modifier
) {
    val (color, text) = when (difficulty) {
        Difficulty.EASY -> FunnyColors.DifficultyEasy to "Легко"
        Difficulty.MEDIUM -> FunnyColors.DifficultyMedium to "Средне"
        Difficulty.HARD -> FunnyColors.DifficultyHard to "Сложно"
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.2f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun LevelBadge(
    level: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(FunnyColors.Secondary, FunnyColors.SecondaryDark)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = level.toString(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

@Composable
fun PointsBadge(
    points: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = FunnyColors.Yellow.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = FunnyColors.StarFilled,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = points.toString(),
                color = FunnyColors.SecondaryDark,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun AvatarCircle(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    backgroundColor: Color = FunnyColors.Primary.copy(alpha = 0.2f),
    borderColor: Color = FunnyColors.Primary,
    textColor: Color = FunnyColors.Primary,
    fontSize: Int = 20
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(2.dp, borderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.firstOrNull()?.uppercase() ?: "?",
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
