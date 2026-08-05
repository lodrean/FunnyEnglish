package com.sotospeak.app.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.sotospeak.composeapp.generated.resources.Res
import com.sotospeak.composeapp.generated.resources.splash_logo_dark
import com.sotospeak.composeapp.generated.resources.splash_logo_light
import com.sotospeak.designsystem.theme.LocalSpeakingColors
import org.jetbrains.compose.resources.painterResource

@Composable
fun SplashScreen(
    isLoading: Boolean,
    isDarkTheme: Boolean
) {
    val speaking = LocalSpeakingColors.current
    val isDark = isDarkTheme
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(speaking.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Image(
                painter = painterResource(
                    if (isDark) Res.drawable.splash_logo_dark else Res.drawable.splash_logo_light
                ),
                contentDescription = "So to speak",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (isLoading) {
                CircularProgressIndicator(
                    color = speaking.primary,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
