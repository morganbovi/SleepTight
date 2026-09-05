package com.apkrocket.sleeptight.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NightColorScheme = darkColorScheme(
    primary = MoonlightIndigo,
    onPrimary = MidnightBlue,
    secondary = StarlightLavender,
    tertiary = DuskRose,
    background = MidnightBlue,
    onBackground = CloudWhite,
    surface = NightSurface,
    onSurface = CloudWhite,
    surfaceVariant = NightSurfaceVariant,
    onSurfaceVariant = MutedSlate,
)

/** Always dark: this is a bedtime app, so we skip light/dynamic theming entirely. */
@Composable
fun SleepTightTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NightColorScheme,
        typography = Typography,
        content = content
    )
}
