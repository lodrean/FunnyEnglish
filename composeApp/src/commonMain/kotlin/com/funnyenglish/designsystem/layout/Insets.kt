package com.funnyenglish.designsystem.layout

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

/**
 * FunnyEnglish Window Insets utilities
 *
 * Provides standard insets for edge-to-edge layouts and IME handling.
 * Use these instead of hard-coded system bars padding.
 */

/**
 * Standard safe content insets: status bars + navigation bars.
 */
val safeContentInsets: WindowInsets
    @Composable
    get() = WindowInsets.systemBars

/**
 * Padding values for safe content (system bars).
 */
val safeContentPadding: PaddingValues
    @Composable
    get() = WindowInsets.systemBars.asPaddingValues()

/**
 * Padding values that include IME (keyboard) in addition to system bars.
 * Use this for screens with text input to avoid the keyboard covering fields.
 */
val safeContentWithImePadding: PaddingValues
    @Composable
    get() = WindowInsets.systemBars.add(WindowInsets.ime).asPaddingValues()

/**
 * Standard bottom padding including navigation bar and IME.
 * Useful for LazyColumn contentPadding bottom values.
 */
val bottomInsetWithIme: androidx.compose.ui.unit.Dp
    @Composable
    get() {
        val bars = WindowInsets.navigationBars.asPaddingValues()
        val ime = WindowInsets.ime.asPaddingValues()
        return maxOf(bars.calculateBottomPadding(), ime.calculateBottomPadding())
    }

/**
 * Top inset (status bar height).
 */
val statusBarInset: androidx.compose.ui.unit.Dp
    @Composable
    get() = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

/**
 * Bottom inset (navigation bar height).
 */
val navigationBarInset: androidx.compose.ui.unit.Dp
    @Composable
    get() = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

/**
 * Recommended bottom content padding for screens with bottom navigation.
 * Combines navigation bar height with a standard spacing token.
 */
val bottomNavContentPadding: PaddingValues
    @Composable
    get() = PaddingValues(bottom = navigationBarInset + 80.dp)
