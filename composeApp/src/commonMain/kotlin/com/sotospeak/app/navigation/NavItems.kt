package com.sotospeak.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sotospeak.designsystem.icons.SpeakingIcons
import com.sotospeak.designsystem.theme.LocalSpeakingColors

private data class MainNavItem(
    val screen: AppScreen,
    val label: String,
    val icon: ImageVector
)

// Лейблы и иконки по мокапу Playful Coach v1.1 (bottomnav: home/send/user, аудит 2026-08-01)
private val mainNavItems = listOf(
    MainNavItem(AppScreen.Library, "Темы", SpeakingIcons.Home),
    MainNavItem(AppScreen.MySubmissions, "Отправки", SpeakingIcons.Send),
    MainNavItem(AppScreen.Profile, "Профиль", SpeakingIcons.User)
)

@Composable
internal fun BottomNavigationBar(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    submissionsBadgeCount: Int = 0
) {
    // M3-дефолты (DSM-5 §4 «Навигация»): container surfaceContainer, pill-индикатор
    // primaryContainer, selected icon onPrimaryContainer, текст активного onSurface,
    // неактивные — onSurfaceVariant
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        mainNavItems.forEach { item ->
            NavigationBarItem(
                selected = currentScreen == item.screen,
                onClick = { onNavigate(item.screen) },
                icon = {
                    BadgedNavIcon(
                        icon = item.icon,
                        label = item.label,
                        badgeCount = if (item.screen == AppScreen.MySubmissions) submissionsBadgeCount else 0
                    )
                },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

/** M3 NavigationRail для wide/desktop (medium/expanded) — те же пункты, что у bottom nav */
@Composable
internal fun SpeakingNavigationRail(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    submissionsBadgeCount: Int = 0
) {
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Spacer(modifier = Modifier.weight(1f))
        mainNavItems.forEach { item ->
            NavigationRailItem(
                selected = currentScreen == item.screen,
                onClick = { onNavigate(item.screen) },
                icon = {
                    BadgedNavIcon(
                        icon = item.icon,
                        label = item.label,
                        badgeCount = if (item.screen == AppScreen.MySubmissions) submissionsBadgeCount else 0
                    )
                },
                label = { Text(item.label) },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

/**
 * Иконка пункта навигации с опциональным бейджем новых оценок (DS v3.2.0 B10,
 * редизайн V1 «Путь спикера»): оранжевый круг (statusNew) с числом, обводка цветом
 * фона навбара. Тёмный текст вместо белого из макета — WCAG: белый на #FB8C00
 * 2.37:1 FAIL (DS §6 запрещает), speaking.text на нём ≈4.97:1 AA.
 */
@Composable
internal fun BadgedNavIcon(
    icon: ImageVector,
    label: String,
    badgeCount: Int
) {
    val speaking = LocalSpeakingColors.current
    Box {
        Icon(icon, contentDescription = label)
        if (badgeCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 7.dp, y = (-3).dp)
                    .size(18.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(speaking.statusNew)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(percent = 50)
                    )
                    .testTag("nav_badge_submissions"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (badgeCount > 9) "9+" else badgeCount.toString(),
                    // Тёмный ink в обеих темах (в dark бейдж #FFB74D): WCAG AA,
                    // белый на оранжевом запрещён DS §6
                    color = speaking.text,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 10.sp
                )
            }
        }
    }
}
