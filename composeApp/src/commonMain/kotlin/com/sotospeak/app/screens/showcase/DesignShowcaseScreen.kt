package com.sotospeak.app.screens.showcase

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sotospeak.app.components.PlatformBackHandler
import com.sotospeak.app.screens.showcase.sections.AppBarSection
import com.sotospeak.app.screens.showcase.sections.ButtonsSection
import com.sotospeak.app.screens.showcase.sections.ColorsSection
import com.sotospeak.app.screens.showcase.sections.DialogsSection
import com.sotospeak.app.screens.showcase.sections.FieldsSection
import com.sotospeak.app.screens.showcase.sections.IconsSection
import com.sotospeak.app.screens.showcase.sections.LoadersSection
import com.sotospeak.app.screens.showcase.sections.NavigationSection
import com.sotospeak.app.screens.showcase.sections.RecordingKitSection
import com.sotospeak.app.screens.showcase.sections.ShapesSection
import com.sotospeak.app.screens.showcase.sections.SpacingSection
import com.sotospeak.app.screens.showcase.sections.StatesSection
import com.sotospeak.app.screens.showcase.sections.TypographySection
import com.sotospeak.designsystem.icons.SpeakingIcons
import com.sotospeak.designsystem.theme.FunnyTheme
import com.sotospeak.designsystem.theme.LocalSpeakingColors

/**
 * Шоукейс дизайн-системы (по образцу design-system-showcase tele2-selfcare-android):
 * индекс-экран с поиском и переключателем темы + демо-экран на секцию.
 *
 * Доступен только в debug/qa-сборках: вход — из Debug-меню (гейт AppConfig.debugToolsEnabled,
 * ветка MainNavHost дополнительно не рендерит экран при выключенном флаге).
 *
 * Выбранная секция и режим темы хранятся строками: rememberSaveable-сейвинг enum'ов
 * на desktop/wasm ненадёжен, строки безопасны.
 */

/** Режим темы шоукейса (аналог NightMode SYSTEM/DAY/NIGHT из tele2). */
enum class ShowcaseThemeMode(val label: String) {
    System("Системная"),
    Light("Светлая"),
    Dark("Тёмная");

    fun next(): ShowcaseThemeMode = entries[(ordinal + 1) % entries.size]
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesignShowcaseScreen(onBack: () -> Unit) {
    var themeModeName by rememberSaveable { mutableStateOf(ShowcaseThemeMode.System.name) }
    var selectedName by rememberSaveable { mutableStateOf<String?>(null) }
    var query by rememberSaveable { mutableStateOf("") }

    val themeMode = ShowcaseThemeMode.valueOf(themeModeName)
    val darkTheme = when (themeMode) {
        ShowcaseThemeMode.System -> isSystemInDarkTheme()
        ShowcaseThemeMode.Light -> false
        ShowcaseThemeMode.Dark -> true
    }
    val selected = selectedName?.let { name ->
        ShowcaseSection.entries.firstOrNull { it.name == name }
    }

    // Внутренний back из демо-экрана на индекс (системный «назад»); на desktop — no-op
    if (selected != null) {
        PlatformBackHandler { selectedName = null }
    }

    // Локальная тема шоукейса: перекрывает системную для превью обеих палитр (как T2Theme в tele2)
    FunnyTheme(darkTheme = darkTheme) {
        val speaking = LocalSpeakingColors.current
        if (selected == null) {
            Scaffold(
                containerColor = speaking.background,
                topBar = {
                    TopAppBar(
                        title = { Text("Дизайн-система", fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(SpeakingIcons.ChevronLeft, contentDescription = "Назад")
                            }
                        },
                        actions = {
                            IconButton(onClick = { themeModeName = themeMode.next().name }) {
                                // WASM-правило (грабля №75): только SpeakingIcons; режим —
                                // в contentDescription, как строка темы в SettingsScreen V1
                                val desc = when (themeMode) {
                                    ShowcaseThemeMode.System -> "Тема: системная"
                                    ShowcaseThemeMode.Light -> "Тема: светлая"
                                    ShowcaseThemeMode.Dark -> "Тема: тёмная"
                                }
                                Icon(SpeakingIcons.Moon, contentDescription = desc)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = speaking.background)
                    )
                },
                modifier = Modifier.testTag("showcase_index_screen")
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text("Поиск") },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("showcase_search_input")
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        val visible = ShowcaseSection.entries.filter { section ->
                            section.title.contains(query.trim(), ignoreCase = true)
                        }
                        visible.forEach { section ->
                            ListItem(
                                headlineContent = {
                                    Text(section.title, fontWeight = FontWeight.SemiBold)
                                },
                                trailingContent = null,
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                modifier = Modifier
                                    .clickable { selectedName = section.name }
                                    .testTag("showcase_section_${section.name}")
                            )
                        }
                        if (visible.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp)
                            ) {
                                Text(
                                    text = "Ничего не найдено",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = speaking.textMuted,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            ShowcaseSectionScreen(section = selected, onBack = { selectedName = null })
        }
    }
}

/** Диспетчер «секция → демо-экран» (exhaustive when — новая секция не соберётся без демо). */
@Composable
private fun ShowcaseSectionScreen(section: ShowcaseSection, onBack: () -> Unit) {
    when (section) {
        ShowcaseSection.Colors -> ColorsSection(onBack = onBack)
        ShowcaseSection.Typography -> TypographySection(onBack = onBack)
        ShowcaseSection.Shapes -> ShapesSection(onBack = onBack)
        ShowcaseSection.Spacing -> SpacingSection(onBack = onBack)
        ShowcaseSection.Icons -> IconsSection(onBack = onBack)
        ShowcaseSection.Buttons -> ButtonsSection(onBack = onBack)
        ShowcaseSection.Fields -> FieldsSection(onBack = onBack)
        ShowcaseSection.AppBar -> AppBarSection(onBack = onBack)
        ShowcaseSection.Navigation -> NavigationSection(onBack = onBack)
        ShowcaseSection.RecordingKit -> RecordingKitSection(onBack = onBack)
        ShowcaseSection.Loaders -> LoadersSection(onBack = onBack)
        ShowcaseSection.Dialogs -> DialogsSection(onBack = onBack)
        ShowcaseSection.States -> StatesSection(onBack = onBack)
    }
}
