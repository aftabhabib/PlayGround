package com.desimeals.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val GreenPrimary = Color(0xFF1B8C4B)
val GreenDark = Color(0xFF0E5A30)
val GreenLight = Color(0xFFDFF2E5)

private val LightScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = Color.White,
    primaryContainer = GreenLight,
    onPrimaryContainer = GreenDark,
    secondary = Color(0xFF4E7A5B),
    tertiary = Color(0xFFC98A2B),
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    error = Color(0xFFBA1A1A)
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFF5FD08A),
    onPrimary = Color(0xFF062B17),
    primaryContainer = Color(0xFF0E5A30),
    onPrimaryContainer = Color(0xFFDFF2E5),
    secondary = Color(0xFF9CC7A9),
    tertiary = Color(0xFFE0B25F),
    background = Color(0xFF101413),
    surface = Color(0xFF151917)
)

@Composable
fun DesiMealsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        content = content
    )
}
