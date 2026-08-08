package com.sotospeak.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sotospeak.app.components.ErrorMessage
import com.sotospeak.app.components.LoadingIndicator
import com.sotospeak.app.components.SpeakingGate
import com.sotospeak.app.viewmodel.ProfileState
import com.sotospeak.designsystem.theme.LocalSpeakingColors
import kotlinx.coroutines.delay

/**
 * Экран профиля по мокапу frame-profile (Playful Coach v1.1):
 * аватар 88dp с инициалами, имя, email, две stat-карточки, «Выйти» danger-ghost.
 * Гость — GuestProfileStub по frame-profile-guest.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: ProfileState,
    isGuest: Boolean = false,
    submissionsCount: Int = 0,
    topicsCompleted: Int = 0,
    themeMode: com.sotospeak.app.viewmodel.AppThemeMode = com.sotospeak.app.viewmodel.AppThemeMode.SYSTEM,
    onThemeSelected: (com.sotospeak.app.viewmodel.AppThemeMode) -> Unit = {},
    onLoad: () -> Unit,
    onLogout: () -> Unit,
    onRegisterClick: () -> Unit = {},
    onLoginClick: (() -> Unit)? = null,
    /** Версия приложения (debug/qa-сборки); 7 тапов по ней — скрытое debug-меню */
    versionLabel: String? = null,
    onDebugMenuOpen: () -> Unit = {}
) {
    if (!isGuest) {
        LaunchedEffect(Unit) { onLoad() }
    }

    if (isGuest) {
        GuestProfileStub(
            onRegisterClick = onRegisterClick,
            onLoginClick = onLoginClick,
            versionLabel = versionLabel,
            onDebugMenuOpen = onDebugMenuOpen
        )
        return
    }

    if (state.isLoading && state.userProfile == null) {
        LoadingIndicator()
        return
    }

    if (state.error != null && state.userProfile == null) {
        ErrorMessage(
            message = state.error,
            onRetry = onLoad
        )
        return
    }

    val profile = state.userProfile ?: return
    val speaking = LocalSpeakingColors.current

    Scaffold(
        containerColor = speaking.background,
        topBar = {
            ProfileAppBar(subtitle = "Твой прогресс и записи")
        },
        modifier = Modifier.testTag("profile_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // .profile-head
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // .avatar-circle 88dp; tertiary-токена в SpeakingColors нет — secondary
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .background(speaking.secondary, CircleShape)
                        .testTag("profile_avatar"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initialsOf(profile.user.displayName),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = speaking.onPrimary
                    )
                }
                Text(
                    text = profile.user.displayName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = speaking.text,
                    modifier = Modifier.testTag("profile_name")
                )
                Text(
                    text = profile.user.email,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = speaking.textMuted,
                    modifier = Modifier.testTag("profile_email")
                )
            }

            // .stat-row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    number = submissionsCount,
                    label = "записи отправлено",
                    modifier = Modifier
                        .weight(1f)
                        .testTag("profile_stat_submissions")
                )
                StatCard(
                    number = topicsCompleted,
                    label = "темы пройдено",
                    modifier = Modifier
                        .weight(1f)
                        .testTag("profile_stat_topics")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Переключатель темы
            ThemeSelector(
                selected = themeMode,
                onSelected = onThemeSelected,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            // M3 OutlinedButton с error-цветами (бывш. SpeakingDangerGhostButton, C3)
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("profile_logout_button"),
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Выйти")
            }

            // Версия приложения (debug/qa): 7 тапов — скрытое debug-меню
            if (versionLabel != null) {
                DebugVersionFooter(versionLabel = versionLabel, onDebugMenuOpen = onDebugMenuOpen)
            }
        }
    }
}

/** .stat-card: M3 OutlinedCard (A12, border outlineVariant), number primary / label muted. */
@Composable
private fun StatCard(
    number: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    val speaking = LocalSpeakingColors.current

    OutlinedCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.outlinedCardColors(containerColor = speaking.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = number.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = speaking.primary
            )
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = speaking.textMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** Гостевой профиль по frame-profile-guest: 📬 + CTA регистрации/входа. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GuestProfileStub(
    onRegisterClick: () -> Unit,
    onLoginClick: (() -> Unit)?,
    versionLabel: String? = null,
    onDebugMenuOpen: () -> Unit = {}
) {
    val speaking = LocalSpeakingColors.current

    Scaffold(
        containerColor = speaking.background,
        topBar = {
            ProfileAppBar(subtitle = "Гостевой режим")
        },
        modifier = Modifier.testTag("guest_profile_screen")
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            SpeakingGate(
                emoji = "📬",
                title = null,
                text = "Зарегистрируйся, чтобы отправлять записи учителю и видеть оценки",
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onRegisterClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .testTag("guest_profile_register_button"),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Зарегистрироваться")
                }
                onLoginClick?.let {
                    TextButton(
                        onClick = it,
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .align(Alignment.CenterHorizontally)
                            .testTag("guest_profile_login_link"),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row {
                            Text(
                                text = "Уже есть аккаунт? ",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = speaking.textMuted
                            )
                            Text(
                                text = "Войти",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Версия (debug/qa): 7 тапов — debug-меню; показывается и гостю
            if (versionLabel != null) {
                Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                    DebugVersionFooter(versionLabel = versionLabel, onDebugMenuOpen = onDebugMenuOpen)
                }
            }
        }
    }
}

/** Подпись версии внизу профиля; [DEBUG_TAPS_TO_OPEN] тапов — открытие debug-меню. */
@Composable
private fun DebugVersionFooter(
    versionLabel: String,
    onDebugMenuOpen: () -> Unit
) {
    val speaking = LocalSpeakingColors.current
    var taps by remember { mutableIntStateOf(0) }

    LaunchedEffect(taps) {
        when {
            taps >= DEBUG_TAPS_TO_OPEN -> {
                taps = 0
                onDebugMenuOpen()
            }
            taps > 0 -> {
                delay(2_000) // серия тапов — не дольше 2с между тапами
                taps = 0
            }
        }
    }

    Text(
        text = versionLabel,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = speaking.textMuted,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { taps++ }
            .padding(8.dp)
            .testTag("profile_version")
    )
}

private const val DEBUG_TAPS_TO_OPEN = 7

/** .appbar профиля: заголовок «Профиль» + подзаголовок (без back — top-level экран). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileAppBar(subtitle: String) {
    val speaking = LocalSpeakingColors.current

    TopAppBar(
        title = {
            Column {
                Text(
                    text = "Профиль",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = speaking.text
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = speaking.textMuted
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = speaking.background)
    )
}

/** Селектор темы оформления: Системная / Светлая / Тёмная. */
@Composable
private fun ThemeSelector(
    selected: com.sotospeak.app.viewmodel.AppThemeMode,
    onSelected: (com.sotospeak.app.viewmodel.AppThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val speaking = LocalSpeakingColors.current
    val options = listOf(
        com.sotospeak.app.viewmodel.AppThemeMode.SYSTEM to "Системная",
        com.sotospeak.app.viewmodel.AppThemeMode.LIGHT to "Светлая",
        com.sotospeak.app.viewmodel.AppThemeMode.DARK to "Тёмная"
    )

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Оформление",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = speaking.textMuted
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, (mode, label) ->
                SegmentedButton(
                    selected = selected == mode,
                    onClick = { onSelected(mode) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                    icon = {},
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = speaking.primaryContainer,
                        activeContentColor = speaking.onPrimaryContainer,
                        inactiveContainerColor = speaking.surface,
                        inactiveContentColor = speaking.text
                    )
                ) {
                    Text(label, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

/** Инициалы для аватара: первые буквы первых двух слов («Анна Смирнова» → «АС»). */
private fun initialsOf(displayName: String): String =
    displayName.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "?" }
