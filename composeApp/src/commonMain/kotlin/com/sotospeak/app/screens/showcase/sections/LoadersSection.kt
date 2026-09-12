package com.sotospeak.app.screens.showcase.sections

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sotospeak.app.components.LoadingIndicator
import com.sotospeak.app.screens.showcase.DemoCard
import com.sotospeak.app.screens.showcase.ShowcaseScaffold
import com.sotospeak.app.screens.showcase.ShowcaseSection
import com.sotospeak.designsystem.animations.ListSkeleton
import com.sotospeak.designsystem.animations.SkeletonCard
import com.sotospeak.designsystem.theme.LocalSpeakingColors

/**
 * Демо «Загрузка и скелетоны»: LoadingIndicator и shimmer-скелетоны
 * (SkeletonCard / ListSkeleton). Shimmer — infinite-анимация, в UI-тестах
 * секция не рендерится (грабля №49).
 */
@Composable
fun LoadersSection(onBack: () -> Unit) {
    val speaking = LocalSpeakingColors.current

    ShowcaseScaffold(section = ShowcaseSection.Loaders, onBack = onBack) {
        DemoCard(title = "LoadingIndicator") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                LoadingIndicator()
            }
        }
        DemoCard(title = "SkeletonCard (shimmer)") {
            SkeletonCard()
        }
        DemoCard(title = "ListSkeleton · 3 строки") {
            // ListSkeleton внутри своей Column fillMaxSize — ограничиваем высотой
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(168.dp)
            ) {
                ListSkeleton(itemCount = 3)
            }
        }
        Text(
            text = "SkeletonText/Title/Circle/QuestCard/HomeScreenSkeleton — см. LoadingSkeleton.kt",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = speaking.textMuted
        )
    }
}
