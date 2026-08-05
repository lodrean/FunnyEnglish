package com.funnyenglish.design.components.gamification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funnyenglish.design.theme.FunnyEnglishTheme

@Composable
fun Badge(
    count: Int,
    modifier: Modifier = Modifier,
    maxCount: Int = 99
) {
    val displayCount = if (count > maxCount) "$maxCount+" else count.toString()

    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.error),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayCount,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            ),
            color = MaterialTheme.colorScheme.onError,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun BadgedIcon(
    icon: @Composable () -> Unit,
    badgeCount: Int,
    modifier: Modifier = Modifier,
    maxCount: Int = 99
) {
    BadgedBox(
        badge = {
            if (badgeCount > 0) {
                Badge(count = badgeCount, maxCount = maxCount)
            }
        },
        modifier = modifier
    ) {
        icon()
    }
}

@Preview(showBackground = true)
@Composable
private fun BadgeLightPreview() {
    FunnyEnglishTheme(darkTheme = false) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
        ) {
            Badge(count = 5)
            Badge(count = 99)
            Badge(count = 150)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BadgeDarkPreview() {
    FunnyEnglishTheme(darkTheme = true) {
        BadgedIcon(
            icon = {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    modifier = Modifier.size(32.dp)
                )
            },
            badgeCount = 7
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BadgeMaxCountPreview() {
    FunnyEnglishTheme {
        Badge(count = 150, maxCount = 9)
    }
}
