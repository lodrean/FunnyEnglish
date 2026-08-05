package com.funnyenglish.design.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf

@Stable
private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = TertiaryLight,
    onTertiary = OnTertiaryLight,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    scrim = ScrimLight,
    inverseSurface = InverseSurfaceLight,
    inverseOnSurface = InverseOnSurfaceLight,
    inversePrimary = InversePrimaryLight,
    surfaceTint = SurfaceTintLight
)

@Stable
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    scrim = ScrimDark,
    inverseSurface = InverseSurfaceDark,
    inverseOnSurface = InverseOnSurfaceDark,
    inversePrimary = InversePrimaryDark,
    surfaceTint = SurfaceTintDark
)

@Immutable
data class FunnyEnglishColorScheme(
    val success: androidx.compose.ui.graphics.Color,
    val onSuccess: androidx.compose.ui.graphics.Color,
    val successContainer: androidx.compose.ui.graphics.Color,
    val onSuccessContainer: androidx.compose.ui.graphics.Color,
    val warning: androidx.compose.ui.graphics.Color,
    val onWarning: androidx.compose.ui.graphics.Color,
    val warningContainer: androidx.compose.ui.graphics.Color,
    val onWarningContainer: androidx.compose.ui.graphics.Color,
    val info: androidx.compose.ui.graphics.Color,
    val onInfo: androidx.compose.ui.graphics.Color,
    val infoContainer: androidx.compose.ui.graphics.Color,
    val onInfoContainer: androidx.compose.ui.graphics.Color,
    val gold: androidx.compose.ui.graphics.Color,
    val onGold: androidx.compose.ui.graphics.Color,
    val goldContainer: androidx.compose.ui.graphics.Color,
    val onGoldContainer: androidx.compose.ui.graphics.Color,
    val silver: androidx.compose.ui.graphics.Color,
    val onSilver: androidx.compose.ui.graphics.Color,
    val silverContainer: androidx.compose.ui.graphics.Color,
    val onSilverContainer: androidx.compose.ui.graphics.Color,
    val bronze: androidx.compose.ui.graphics.Color,
    val onBronze: androidx.compose.ui.graphics.Color,
    val bronzeContainer: androidx.compose.ui.graphics.Color,
    val onBronzeContainer: androidx.compose.ui.graphics.Color,
    val flame: androidx.compose.ui.graphics.Color,
    val onFlame: androidx.compose.ui.graphics.Color,
    val flameContainer: androidx.compose.ui.graphics.Color,
    val onFlameContainer: androidx.compose.ui.graphics.Color,
    val xp: androidx.compose.ui.graphics.Color,
    val onXp: androidx.compose.ui.graphics.Color,
    val xpContainer: androidx.compose.ui.graphics.Color,
    val onXpContainer: androidx.compose.ui.graphics.Color
)

@Stable
private val LightExtendedColorScheme = FunnyEnglishColorScheme(
    success = SuccessLight,
    onSuccess = OnSuccessLight,
    successContainer = SuccessContainerLight,
    onSuccessContainer = OnSuccessContainerLight,
    warning = WarningLight,
    onWarning = OnWarningLight,
    warningContainer = WarningContainerLight,
    onWarningContainer = OnWarningContainerLight,
    info = InfoLight,
    onInfo = OnInfoLight,
    infoContainer = InfoContainerLight,
    onInfoContainer = OnInfoContainerLight,
    gold = GoldLight,
    onGold = OnGoldLight,
    goldContainer = GoldContainerLight,
    onGoldContainer = OnGoldContainerLight,
    silver = SilverLight,
    onSilver = OnSilverLight,
    silverContainer = SilverContainerLight,
    onSilverContainer = OnSilverContainerLight,
    bronze = BronzeLight,
    onBronze = OnBronzeLight,
    bronzeContainer = BronzeContainerLight,
    onBronzeContainer = OnBronzeContainerLight,
    flame = FlameLight,
    onFlame = OnFlameLight,
    flameContainer = FlameContainerLight,
    onFlameContainer = OnFlameContainerLight,
    xp = XpLight,
    onXp = OnXpLight,
    xpContainer = XpContainerLight,
    onXpContainer = OnXpContainerLight
)

@Stable
private val DarkExtendedColorScheme = FunnyEnglishColorScheme(
    success = SuccessDark,
    onSuccess = OnSuccessDark,
    successContainer = SuccessContainerDark,
    onSuccessContainer = OnSuccessContainerDark,
    warning = WarningDark,
    onWarning = OnWarningDark,
    warningContainer = WarningContainerDark,
    onWarningContainer = OnWarningContainerDark,
    info = InfoDark,
    onInfo = OnInfoDark,
    infoContainer = InfoContainerDark,
    onInfoContainer = OnInfoContainerDark,
    gold = GoldDark,
    onGold = OnGoldDark,
    goldContainer = GoldContainerDark,
    onGoldContainer = OnGoldContainerDark,
    silver = SilverDark,
    onSilver = OnSilverDark,
    silverContainer = SilverContainerDark,
    onSilverContainer = OnSilverContainerDark,
    bronze = BronzeDark,
    onBronze = OnBronzeDark,
    bronzeContainer = BronzeContainerDark,
    onBronzeContainer = OnBronzeContainerDark,
    flame = FlameDark,
    onFlame = OnFlameDark,
    flameContainer = FlameContainerDark,
    onFlameContainer = OnFlameContainerDark,
    xp = XpDark,
    onXp = OnXpDark,
    xpContainer = XpContainerDark,
    onXpContainer = OnXpContainerDark
)

val LocalFunnyEnglishColorScheme = staticCompositionLocalOf { LightExtendedColorScheme }

@Composable
fun FunnyEnglishTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val extendedColorScheme = when {
        darkTheme -> DarkExtendedColorScheme
        else -> LightExtendedColorScheme
    }

    CompositionLocalProvider(
        LocalFunnyEnglishColorScheme provides extendedColorScheme
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}

object FunnyEnglishTheme {
    val colors: FunnyEnglishColorScheme
        @Composable
        get() = LocalFunnyEnglishColorScheme.current
}
