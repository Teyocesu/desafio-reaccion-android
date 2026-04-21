package com.uade.desafioreaccion.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Indigo,
    secondary = Cyan,
    tertiary = Emerald,
    background = SurfaceLight,
    surface = androidx.compose.ui.graphics.Color.White,
    error = RedAccent
)

private val DarkColors = darkColorScheme(
    primary = Cyan,
    secondary = Indigo,
    tertiary = Emerald,
    background = SurfaceDark,
    surface = androidx.compose.ui.graphics.Color(0xFF1F2937),
    error = RedAccent
)

@Composable
fun ReactionGameTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
