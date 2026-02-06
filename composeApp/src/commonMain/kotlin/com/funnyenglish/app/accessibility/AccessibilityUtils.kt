package com.funnyenglish.app.accessibility

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp

/**
 * Accessibility utilities for FunnyEnglish app
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
 * Predefined content descriptions for common UI elements
 * These should be moved to string resources for localization
 */
object AccessibilityDescriptions {
    // Navigation
    const val NAVIGATE_TO_PROFILE = "Navigate to profile"
    const val NAVIGATE_TO_HOME = "Navigate to home"
    const val NAVIGATE_BACK = "Go back"
    const val NAVIGATE_SETTINGS = "Open settings"
    
    // Actions
    const val SUBMIT_ANSWER = "Submit answer"
    const val CONTINUE_LEARNING = "Continue learning"
    const val START_LESSON = "Start lesson"
    const val VIEW_ALL_CATEGORIES = "View all categories"
    
    // UI Elements
    const val USER_AVATAR = "User avatar"
    const val LEVEL_PROGRESS = "Current level progress"
    const val STREAK_COUNT = "Current streak days"
    const val POINTS_COUNT = "Total points earned"
    
    // Test
    const val QUESTION_TEXT = "Question"
    const val ANSWER_OPTION = "Answer option"
    const val CORRECT_ANSWER = "Correct answer"
    const val WRONG_ANSWER = "Wrong answer"
    
    // Categories
    const val CATEGORY_ICON = "Category icon"
    const val TEST_COUNT = "Number of tests"
    
    // Achievements
    const val ACHIEVEMENT_BADGE = "Achievement badge"
    const val ACHIEVEMENT_LOCKED = "Locked achievement"
    const val ACHIEVEMENT_UNLOCKED = "Unlocked achievement"
    
    // Input
    const val EMAIL_INPUT = "Email address"
    const val PASSWORD_INPUT = "Password"
    const val PASSWORD_TOGGLE = "Toggle password visibility"
    const val LOGIN_BUTTON = "Log in"
    const val REGISTER_BUTTON = "Create account"
}
