package com.metrolist.shared.ui.theme

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.metrolist.shared.ui.haptics.MetrolistHapticsProvider

private val LightColors =
    lightColorScheme(
        primary = Color(0xFFFA243C),
        onPrimary = Color.White,
        secondary = Color(0xFFFC3C44),
        onSecondary = Color.White,
        background = Color(0xFFF2F2F7),
        onBackground = Color(0xFF1C1C1E),
        surface = Color.White,
        onSurface = Color(0xFF1C1C1E),
        error = Color(0xFFFF3B30),
        onError = Color.White,
    )

private val DarkColors =
    darkColorScheme(
        primary = Color(0xFFFA243C),
        onPrimary = Color.White,
        secondary = Color(0xFFFC3C44),
        onSecondary = Color.White,
        background = Color(0xFF000000),
        onBackground = Color(0xFFFFFFFF),
        surface = Color(0xFF1C1C1E),
        onSurface = Color(0xFFFFFFFF),
        error = Color(0xFFFF453A),
        onError = Color.White,
    )

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MetrolistTheme(
    useDarkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colors = if (useDarkTheme) DarkColors else LightColors

    val motionScheme = remember { MotionScheme.expressive() }
    MaterialExpressiveTheme(
        colorScheme = animateColorScheme(colors, motionScheme),
        motionScheme = motionScheme,
        typography = Typography(),
        content = { MetrolistHapticsProvider(content) },
    )
}
