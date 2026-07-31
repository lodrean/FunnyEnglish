package com.funnyenglish.app.di

import androidx.compose.runtime.Composable

/**
 * Web (WASM) specific Coil initialization.
 * Note: Coil is not fully supported on WASM target.
 * Images should be loaded using Compose Resources or custom implementation.
 */
@Composable
fun InitializeCoil() {
    // Coil is not supported on WASM yet
    // Use compose.imageviewer or custom image loading for web
}
