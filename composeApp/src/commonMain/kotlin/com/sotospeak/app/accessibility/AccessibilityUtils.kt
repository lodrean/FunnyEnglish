package com.sotospeak.app.accessibility

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.sotospeak.composeapp.generated.resources.Res
import com.sotospeak.composeapp.generated.resources.a11y_achievement_badge
import com.sotospeak.composeapp.generated.resources.a11y_achievement_locked
import com.sotospeak.composeapp.generated.resources.a11y_achievement_unlocked
import com.sotospeak.composeapp.generated.resources.a11y_answer_option
import com.sotospeak.composeapp.generated.resources.a11y_category_icon
import com.sotospeak.composeapp.generated.resources.a11y_continue_learning
import com.sotospeak.composeapp.generated.resources.a11y_correct_answer
import com.sotospeak.composeapp.generated.resources.a11y_email_input
import com.sotospeak.composeapp.generated.resources.a11y_level_progress
import com.sotospeak.composeapp.generated.resources.a11y_login_button
import com.sotospeak.composeapp.generated.resources.a11y_navigate_back
import com.sotospeak.composeapp.generated.resources.a11y_navigate_settings
import com.sotospeak.composeapp.generated.resources.a11y_navigate_to_home
import com.sotospeak.composeapp.generated.resources.a11y_navigate_to_profile
import com.sotospeak.composeapp.generated.resources.a11y_password_input
import com.sotospeak.composeapp.generated.resources.a11y_password_toggle
import com.sotospeak.composeapp.generated.resources.a11y_points_count
import com.sotospeak.composeapp.generated.resources.a11y_question_text
import com.sotospeak.composeapp.generated.resources.a11y_register_button
import com.sotospeak.composeapp.generated.resources.a11y_start_lesson
import com.sotospeak.composeapp.generated.resources.a11y_streak_count
import com.sotospeak.composeapp.generated.resources.a11y_submit_answer
import com.sotospeak.composeapp.generated.resources.a11y_test_count
import com.sotospeak.composeapp.generated.resources.a11y_user_avatar
import com.sotospeak.composeapp.generated.resources.a11y_view_all_categories
import com.sotospeak.composeapp.generated.resources.a11y_wrong_answer
import org.jetbrains.compose.resources.stringResource

/**
 * Accessibility utilities for So to Speak app
 * Helps ensure WCAG 2.1 AA compliance
 */
object AccessibilityUtils {

    /**
     * Minimum touch target size for accessibility (48dp)
     */
    const val MIN_TOUCH_TARGET_SIZE = 48

    /**
     * Modifier extension to set content description
     */
    fun Modifier.contentDescription(description: String): Modifier =
        this.semantics {
            this.contentDescription = description
        }

    /**
     * Modifier extension to mark element as heading
     */
    fun Modifier.heading(): Modifier =
        this.semantics {
            heading()
        }

    /**
     * Modifier extension to set state description
     */
    fun Modifier.stateDescription(description: String): Modifier =
        this.semantics {
            stateDescription = description
        }

    /**
     * Modifier extension for selectable items with accessibility
     */
    fun Modifier.selectableWithAccessibility(
        selected: Boolean,
        onClick: () -> Unit,
        label: String
    ): Modifier =
        this
            .selectable(
                selected = selected,
                onClick = onClick
            )
            .semantics {
                contentDescription = label
                if (selected) {
                    stateDescription = "Selected"
                }
            }

    /**
     * Modifier extension for toggleable items with accessibility
     */
    fun Modifier.toggleableWithAccessibility(
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
        label: String
    ): Modifier =
        this
            .toggleable(
                value = checked,
                onValueChange = onCheckedChange
            )
            .semantics {
                contentDescription = label
                stateDescription = if (checked) "Checked" else "Unchecked"
            }

    /**
     * Ensures minimum touch target size of 48dp
     * Adds padding if the element is smaller
     */
    fun Modifier.minimumTouchTarget(size: Int = MIN_TOUCH_TARGET_SIZE): Modifier =
        this.then(
            if (size < MIN_TOUCH_TARGET_SIZE) {
                Modifier.size(MIN_TOUCH_TARGET_SIZE.dp)
            } else {
                Modifier
            }
        )

    /**
     * Groups related elements for screen reader
     */
    fun Modifier.groupWithLabel(label: String): Modifier =
        this.semantics(mergeDescendants = true) {
            contentDescription = label
        }

    /**
     * Makes element invisible to screen reader (for decorative elements)
     */
    @OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
    fun Modifier.invisibleToScreenReader(): Modifier =
        this.semantics {
            invisibleToUser()
        }

    /**
     * Announces changes to screen reader users
     */
    fun Modifier.liveRegion(mode: LiveRegionMode = LiveRegionMode.Polite): Modifier =
        this.semantics {
            liveRegion = mode
        }

    /**
     * Marks element as disabled with proper accessibility
     */
    fun Modifier.disabledWithDescription(description: String? = null): Modifier =
        this.semantics {
            disabled()
            if (description != null) {
                stateDescription = description
            }
        }

    /**
     * Combines multiple accessibility modifiers for common patterns
     */
    fun Modifier.accessibleButton(
        label: String,
        enabled: Boolean = true
    ): Modifier =
        this
            .contentDescription(label)
            .minimumTouchTarget()
            .then(if (!enabled) disabledWithDescription() else Modifier)

    /**
     * Creates accessible list item
     */
    fun Modifier.accessibleListItem(
        index: Int,
        count: Int,
        label: String
    ): Modifier =
        this.semantics {
            contentDescription = "$label, item ${index + 1} of $count"
            collectionItemInfo = CollectionItemInfo(
                rowIndex = index,
                rowSpan = 1,
                columnIndex = 0,
                columnSpan = 1
            )
        }

    /**
     * Image with proper accessibility handling
     */
    fun Modifier.accessibleImage(
        contentDescription: String?,
        isDecorative: Boolean = false
    ): Modifier =
        when {
            isDecorative -> invisibleToScreenReader()
            contentDescription != null -> this.semantics {
                this.contentDescription = contentDescription
            }
            else -> this.semantics {
                // Provide default description for images without one
                this.contentDescription = "Image"
            }
        }
}

/**
 * Описания доступности для типовых UI-элементов.
 * Строки вынесены в composeResources (`values/strings.xml`, язык UI — русский).
 * Геттеры composable, т.к. `stringResource` читается из композиции.
 */
object AccessibilityDescriptions {
    // Навигация
    val navigateToProfile: String @Composable get() = stringResource(Res.string.a11y_navigate_to_profile)
    val navigateToHome: String @Composable get() = stringResource(Res.string.a11y_navigate_to_home)
    val navigateBack: String @Composable get() = stringResource(Res.string.a11y_navigate_back)
    val navigateSettings: String @Composable get() = stringResource(Res.string.a11y_navigate_settings)

    // Действия
    val submitAnswer: String @Composable get() = stringResource(Res.string.a11y_submit_answer)
    val continueLearning: String @Composable get() = stringResource(Res.string.a11y_continue_learning)
    val startLesson: String @Composable get() = stringResource(Res.string.a11y_start_lesson)
    val viewAllCategories: String @Composable get() = stringResource(Res.string.a11y_view_all_categories)

    // Элементы UI
    val userAvatar: String @Composable get() = stringResource(Res.string.a11y_user_avatar)
    val levelProgress: String @Composable get() = stringResource(Res.string.a11y_level_progress)
    val streakCount: String @Composable get() = stringResource(Res.string.a11y_streak_count)
    val pointsCount: String @Composable get() = stringResource(Res.string.a11y_points_count)

    // Тест
    val questionText: String @Composable get() = stringResource(Res.string.a11y_question_text)
    val answerOption: String @Composable get() = stringResource(Res.string.a11y_answer_option)
    val correctAnswer: String @Composable get() = stringResource(Res.string.a11y_correct_answer)
    val wrongAnswer: String @Composable get() = stringResource(Res.string.a11y_wrong_answer)

    // Категории
    val categoryIcon: String @Composable get() = stringResource(Res.string.a11y_category_icon)
    val testCount: String @Composable get() = stringResource(Res.string.a11y_test_count)

    // Достижения
    val achievementBadge: String @Composable get() = stringResource(Res.string.a11y_achievement_badge)
    val achievementLocked: String @Composable get() = stringResource(Res.string.a11y_achievement_locked)
    val achievementUnlocked: String @Composable get() = stringResource(Res.string.a11y_achievement_unlocked)

    // Ввод
    val emailInput: String @Composable get() = stringResource(Res.string.a11y_email_input)
    val passwordInput: String @Composable get() = stringResource(Res.string.a11y_password_input)
    val passwordToggle: String @Composable get() = stringResource(Res.string.a11y_password_toggle)
    val loginButton: String @Composable get() = stringResource(Res.string.a11y_login_button)
    val registerButton: String @Composable get() = stringResource(Res.string.a11y_register_button)
}
