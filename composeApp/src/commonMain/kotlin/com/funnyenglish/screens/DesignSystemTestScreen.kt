package com.funnyenglish.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.funnyenglish.design.animation.BounceAnimation
import com.funnyenglish.design.animation.ConfettiEffect
import com.funnyenglish.design.animation.DotsTypingIndicator
import com.funnyenglish.design.animation.PulseAnimation
import com.funnyenglish.design.animation.ShakeAnimation
import com.funnyenglish.design.animation.SkeletonLoader
import com.funnyenglish.design.components.buttons.GhostButton
import com.funnyenglish.design.components.buttons.IconButton
import com.funnyenglish.design.components.buttons.PrimaryButton
import com.funnyenglish.design.components.buttons.SecondaryButton
import com.funnyenglish.design.components.cards.ClickableCard
import com.funnyenglish.design.components.cards.ElevatedCard
import com.funnyenglish.design.components.cards.FilledCard
import com.funnyenglish.design.components.cards.OutlinedCard
import com.funnyenglish.design.components.feedback.AppDialog
import com.funnyenglish.design.components.gamification.Badge
import com.funnyenglish.design.components.gamification.LevelIndicator
import com.funnyenglish.design.components.gamification.ProgressRing
import com.funnyenglish.design.components.gamification.StarRating
import com.funnyenglish.design.components.gamification.StreakFlame
import com.funnyenglish.design.components.inputs.AppTextField
import com.funnyenglish.design.components.inputs.Chip
import com.funnyenglish.design.icons.CustomIcons
import com.funnyenglish.design.theme.FunnyEnglishTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DesignSystemTestScreen(
    onBackClick: () -> Unit = {}
) {
    var showConfetti by remember { mutableStateOf(false) }
    var shake by remember { mutableStateOf(false) }
    var bounce by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var textValue by remember { mutableStateOf("") }
    var selectedChip by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Design System 2.0 Test") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // SECTION: Buttons
            SectionTitle("Buttons")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PrimaryButton(
                    text = "Primary",
                    onClick = { showConfetti = true },
                    icon = Icons.Default.Check
                )
                SecondaryButton(
                    text = "Secondary",
                    onClick = { showDialog = true }
                )
                GhostButton(
                    text = "Ghost",
                    onClick = { shake = true }
                )
                IconButton(
                    icon = Icons.Default.Settings,
                    onClick = { bounce = true }
                )
            }

            // SECTION: Cards
            SectionTitle("Cards")
            ElevatedCard {
                Text(
                    "Elevated Card",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
            OutlinedCard {
                Text(
                    "Outlined Card",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
            FilledCard {
                Text(
                    "Filled Card",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
            ClickableCard(
                onClick = { showConfetti = true }
            ) {
                Text(
                    "Clickable Card - Tap me!",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // SECTION: Inputs
            SectionTitle("Inputs")
            AppTextField(
                value = textValue,
                onValueChange = { textValue = it },
                label = "Enter text",
                placeholder = "Type something...",
                leadingIcon = Icons.Default.Search,
                modifier = Modifier.fillMaxWidth()
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Easy", "Medium", "Hard").forEachIndexed { index, label ->
                    Chip(
                        label = label,
                        selected = selectedChip == index,
                        onClick = { selectedChip = index }
                    )
                }
            }

            // SECTION: Gamification
            SectionTitle("Gamification")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StarRating(rating = 2, maxStars = 3)
                StreakFlame(streakCount = 7)
                Badge(count = 5)
            }
            Spacer(modifier = Modifier.height(8.dp))
            ProgressRing(
                progress = 0.75f,
                modifier = Modifier.size(100.dp),
                showPercentage = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            LevelIndicator(
                level = 5,
                currentXp = 750,
                xpToNextLevel = 1000,
                modifier = Modifier.fillMaxWidth()
            )

            // SECTION: Animations
            SectionTitle("Animations")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ShakeAnimation(shake = shake) {
                    IconButton(
                        icon = Icons.Default.Close,
                        onClick = { shake = !shake }
                    )
                }
                BounceAnimation(bounce = bounce) {
                    IconButton(
                        icon = Icons.Default.Star,
                        onClick = { bounce = !bounce }
                    )
                }
                PulseAnimation {
                    Icon(
                        imageVector = CustomIcons.LightningBoltFilled,
                        contentDescription = null,
                        tint = FunnyEnglishTheme.colors.gold
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            DotsTypingIndicator()

            // SECTION: Loading
            SectionTitle("Loading")
            SkeletonLoader(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            )

            // SECTION: Custom Icons
            SectionTitle("Custom Icons")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(CustomIcons.AudioWaveformFilled, contentDescription = null)
                Icon(CustomIcons.MicrophoneFilled, contentDescription = null)
                Icon(CustomIcons.BookOpenFilled, contentDescription = null)
                Icon(CustomIcons.PencilEditFilled, contentDescription = null)
                Icon(CustomIcons.TrophyStarFilled, contentDescription = null)
                Icon(CustomIcons.LightningBoltFilled, contentDescription = null)
                Icon(CustomIcons.BrainFilled, contentDescription = null)
                Icon(CustomIcons.TargetFilled, contentDescription = null)
                Icon(CustomIcons.StreakFlameFilled, contentDescription = null)
                Icon(CustomIcons.StarFilled, contentDescription = null)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Effects
    if (showConfetti) {
        ConfettiEffect(active = true)
    }

    if (showDialog) {
        AppDialog(
            title = "Test Dialog",
            text = "This is a test of the new Design System 2.0 dialog component!",
            confirmButtonText = "Awesome!",
            dismissButtonText = "Close",
            onConfirm = { showDialog = false },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Preview
@Composable
private fun DesignSystemTestScreenPreview() {
    FunnyEnglishTheme {
        DesignSystemTestScreen()
    }
}
