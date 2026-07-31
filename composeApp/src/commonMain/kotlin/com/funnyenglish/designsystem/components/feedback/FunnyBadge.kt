package com.funnyenglish.designsystem.components.feedback

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.funnyenglish.designsystem.tokens.ChipShape
import com.funnyenglish.designsystem.tokens.SpaceSm

/**
 * FunnyEnglish Badge Component
 * 
 * Types: NUMBER (circular with count), DOT (presence only)
 * Animation: Scale-in on value change
 * Overflow: "99+" for counts > 99
 */

enum class FunnyBadgeType {
    NUMBER,  // Shows count
    DOT      // Presence indicator only
}

enum class FunnyBadgeColor {
    PRIMARY,
    ERROR,
    WARNING,
    SUCCESS
}

/**
 * Number badge with count display
 */
@Composable
fun FunnyBadge(
    count: Int,
    modifier: Modifier = Modifier,
    color: FunnyBadgeColor = FunnyBadgeColor.ERROR,
    maxCount: Int = 99
) {
    val badgeColor = when (color) {
        FunnyBadgeColor.PRIMARY -> MaterialTheme.colorScheme.primary
        FunnyBadgeColor.ERROR -> MaterialTheme.colorScheme.error
        FunnyBadgeColor.WARNING -> MaterialTheme.colorScheme.tertiary
        FunnyBadgeColor.SUCCESS -> MaterialTheme.colorScheme.primary
    }
    
    val contentColor = when (color) {
        FunnyBadgeColor.PRIMARY -> MaterialTheme.colorScheme.onPrimary
        FunnyBadgeColor.ERROR -> MaterialTheme.colorScheme.onError
        FunnyBadgeColor.WARNING -> MaterialTheme.colorScheme.onTertiary
        FunnyBadgeColor.SUCCESS -> MaterialTheme.colorScheme.onPrimary
    }
    
    val displayText = if (count > maxCount) "$maxCount+" else count.toString()
    
    AnimatedVisibility(
        visible = count > 0,
        enter = scaleIn(animationSpec = tween(150)),
        exit = scaleOut(animationSpec = tween(150))
    ) {
        Box(
            modifier = modifier
                .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
                .background(badgeColor, CircleShape)
                .padding(horizontal = 4.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = displayText,
                color = contentColor,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Dot badge (presence indicator)
 */
@Composable
fun FunnyDotBadge(
    visible: Boolean,
    modifier: Modifier = Modifier,
    color: FunnyBadgeColor = FunnyBadgeColor.ERROR,
    pulse: Boolean = false
) {
    val badgeColor = when (color) {
        FunnyBadgeColor.PRIMARY -> MaterialTheme.colorScheme.primary
        FunnyBadgeColor.ERROR -> MaterialTheme.colorScheme.error
        FunnyBadgeColor.WARNING -> MaterialTheme.colorScheme.tertiary
        FunnyBadgeColor.SUCCESS -> MaterialTheme.colorScheme.primary
    }
    
    val scale by animateFloatAsState(
        targetValue = if (pulse && visible) 1.2f else 1f,
        animationSpec = tween(500),
        label = "badge_pulse"
    )
    
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(animationSpec = tween(150)),
        exit = scaleOut(animationSpec = tween(150))
    ) {
        Box(
            modifier = modifier
                .size(8.dp)
                .scale(if (pulse) scale else 1f)
                .background(badgeColor, CircleShape)
        )
    }
}

/**
 * Status badge with text label
 */
@Composable
fun FunnyStatusBadge(
    text: String,
    modifier: Modifier = Modifier,
    color: FunnyBadgeColor = FunnyBadgeColor.PRIMARY
) {
    val badgeColor = when (color) {
        FunnyBadgeColor.PRIMARY -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        FunnyBadgeColor.ERROR -> MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
        FunnyBadgeColor.WARNING -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
        FunnyBadgeColor.SUCCESS -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    }
    
    val contentColor = when (color) {
        FunnyBadgeColor.PRIMARY -> MaterialTheme.colorScheme.primary
        FunnyBadgeColor.ERROR -> MaterialTheme.colorScheme.error
        FunnyBadgeColor.WARNING -> MaterialTheme.colorScheme.tertiary
        FunnyBadgeColor.SUCCESS -> MaterialTheme.colorScheme.primary
    }
    
    Box(
        modifier = modifier
            .background(badgeColor, ChipShape)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = contentColor,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
