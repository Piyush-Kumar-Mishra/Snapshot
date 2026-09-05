package com.example.snapshot.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SnapshotColorScheme = lightColorScheme(
    primary = Black900,
    onPrimary = Color.White,
    primaryContainer = YellowCard,
    onPrimaryContainer = Black900,
    secondary = Black700,
    onSecondary = Color.White,
    background = Color.White,
    onBackground = Black900,
    surface = Color.White,
    onSurface = Black900,
    surfaceVariant = YellowCard,
    onSurfaceVariant = Black700,
    outline = Gray300
)

@Composable
fun SnapshotTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SnapshotColorScheme,
        typography = Typography,
        content = content
    )
}