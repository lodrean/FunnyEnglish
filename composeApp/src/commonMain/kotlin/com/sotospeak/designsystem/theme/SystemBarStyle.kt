package com.sotospeak.designsystem.theme

import androidx.compose.runtime.Composable

/**
 * Синхронизирует цвет иконок системных баров (status/navigation) с темой приложения.
 * Нужен, т.к. тема приложения может отличаться от системной (ручной тоггл SYSTEM/DARK/LIGHT),
 * а дефолтный enableEdgeToEdge() следит только за системной.
 * На не-Android платформах — no-op.
 */
@Composable
expect fun ApplySystemBarStyle(darkTheme: Boolean)
