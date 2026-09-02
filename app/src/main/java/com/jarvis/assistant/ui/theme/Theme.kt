package com.jarvis.assistant.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.TextStyle

private val JarvisColorScheme = darkColorScheme(
    primary = JarvisBlue,
    onPrimary = JarvisTextPrimary,
    secondary = JarvisBlueBright,
    background = JarvisBlack,
    surface = JarvisPanel,
    onBackground = JarvisTextPrimary,
    onSurface = JarvisTextPrimary,
    error = JarvisRed
)

val MonoFont = FontFamily.Monospace

@Composable
fun JarvisTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = JarvisColorScheme,
        typography = MaterialTheme.typography.copy(
            bodyLarge = TextStyle(fontFamily = MonoFont, color = JarvisTextPrimary),
            bodyMedium = TextStyle(fontFamily = MonoFont, color = JarvisTextPrimary),
            labelSmall = TextStyle(fontFamily = MonoFont, color = JarvisTextSecondary)
        ),
        content = content
    )
}
