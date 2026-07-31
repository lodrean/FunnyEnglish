package com.funnyenglish.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Groups
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * FunnyEnglish Screens
 * 
 * Navigation destinations for the app
 */

enum class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    HOME(
        route = "home",
        title = "Главная",
        icon = Icons.Default.Home
    ),
    LESSONS(
        route = "lessons",
        title = "Уроки",
        icon = Icons.AutoMirrored.Filled.MenuBook
    ),
    GROUPS(
        route = "groups",
        title = "Мои группы",
        icon = Icons.Default.Groups
    ),
    ACHIEVEMENTS(
        route = "achievements",
        title = "Достижения",
        icon = Icons.Default.EmojiEvents
    ),
    PROFILE(
        route = "profile",
        title = "Профиль",
        icon = Icons.Default.Person
    ),
    ADAPTIVE_LESSON(
        route = "adaptive_lesson",
        title = "Урок",
        icon = Icons.AutoMirrored.Filled.MenuBook
    ),
    GROUP_DETAIL(
        route = "group_detail/{groupId}",
        title = "Группа",
        icon = Icons.Default.Groups
    )
}
