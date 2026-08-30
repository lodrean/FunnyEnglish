package com.sotospeak.app.navigation

import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.sotospeak.designsystem.icons.SpeakingIcons

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
    onNavigate: (AppScreen) -> Unit
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
                icon = { Icon(item.icon, contentDescription = item.label) },
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
    onNavigate: (AppScreen) -> Unit
) {
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Spacer(modifier = Modifier.weight(1f))
        mainNavItems.forEach { item ->
            NavigationRailItem(
                selected = currentScreen == item.screen,
                onClick = { onNavigate(item.screen) },
                icon = { Icon(item.icon, contentDescription = item.label) },
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
